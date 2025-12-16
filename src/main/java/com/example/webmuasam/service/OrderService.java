package com.example.webmuasam.service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import com.example.webmuasam.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webmuasam.Specification.OrderSpecfication;
import com.example.webmuasam.dto.Request.CartItemRequest;
import com.example.webmuasam.dto.Request.OrderRequest;
import com.example.webmuasam.dto.Request.OrderRequestByCash;
import com.example.webmuasam.dto.Response.DashboardResponse;
import com.example.webmuasam.dto.Response.OrderResponse;
import com.example.webmuasam.dto.Response.OrderResponseDetail;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.*;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.*;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.constant.PaymentMethod;
import com.example.webmuasam.util.constant.StatusOrder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    private final RedisTemplate<String, Object> redisTemplate;


    @PostConstruct
    public void initStockToRedis() {
        log.info("Initializing product stock to Redis...");

        List<ProductVariant> productVariants;

        try {
            productVariants = productVariantRepository.findAll();
        } catch (Exception e) {
            log.error("Cannot load product variants from database", e);
            return;
        }

        for (ProductVariant productVariant : productVariants) {
            String key = "stock:" + productVariant.getId();

            try {
                Object value = redisTemplate.opsForValue(). get(key);

                boolean needSet = false;

                if (value == null) {
                    needSet = true; // key chưa tồn tại
                } else {
                    try {
                        Long.parseLong(value.toString());
                    } catch (NumberFormatException e) {
                        needSet = true; // giá trị sai kiểu
                    }
                }

                if (needSet) {
                    redisTemplate.opsForValue()
                            .set(key, (long) productVariant.getStockQuantity());

                    log.debug("Set stock for key {} = {}", key, productVariant.getStockQuantity());
                }

            } catch (DataAccessException e) {
                log.warn("Redis error for key {}. Skip initializing this stock", key, e);
            }
        }

        log.info("Redis stock initialization completed");
    }

    @Transactional
    public OrderResponseDetail createOrderByCash(OrderRequestByCash orderRequest) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new AppException("Invalid token"));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("Email not found"));

        Cart cart = cartRepository
                .findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new AppException("cart not found"));

        List<CartItemRequest> items = cart.getCartItems().stream()
                .map(ci -> new CartItemRequest(ci.getProductVariant().getId(), ci.getQuantity()))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new AppException("cart is empty");
        }
        List<Long> variants =
                items.stream().map(CartItemRequest::getVariantId).distinct().toList();
        Map<Long, ProductVariant> variantMap = this.productVariantRepository.findAllById(variants).stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        Map<String, Integer> decrementStock = new HashMap<>();
        try {
            for (CartItemRequest cartItemDTO : items) {
                String key = "stock:" + cartItemDTO.getVariantId();
                ProductVariant variant = variantMap.get(cartItemDTO.getVariantId());
                if (variant == null) {
                    throw new AppException("The product does not exist: " + cartItemDTO.getVariantId());
                }
                if (!redisTemplate.hasKey(key)) {
                    redisTemplate.opsForValue().set(key, (long) (variant.getStockQuantity()), Duration.ofDays(7));
                }

                Long stockLeft = redisTemplate.opsForValue().decrement(key, cartItemDTO.getQuantity());
                if (stockLeft == null || stockLeft < 0) {
                    for (Map.Entry<String, Integer> entry : decrementStock.entrySet()) {
                        redisTemplate.opsForValue().increment(entry.getKey(), entry.getValue());
                    }
                    redisTemplate.opsForValue().increment(key, cartItemDTO.getQuantity());
                    throw new AppException("product" + cartItemDTO.getVariantId() + " Insufficient stock");
                }
                decrementStock.put(key, (int) cartItemDTO.getQuantity());
            }
            double total = items.stream()
                    .mapToDouble(item -> {
                        ProductVariant variant = variantMap.get(item.getVariantId());
                        return variant.getProduct().getPrice() * item.getQuantity();
                    })
                    .sum();

            // Tạo đơn hàng
            Order order = new Order();
            if (orderRequest.getVoucherCode() != null
                    && !orderRequest.getVoucherCode().isEmpty()) {
                Voucher voucher = this.voucherService.applyVoucher(orderRequest.getVoucherCode(), total);
                if (voucher != null) {
                    voucher.setUsedCount(voucher.getUsedCount() + 1);
                    this.voucherRepository.save(voucher);
                    if (voucher.getDiscountAmount() > 0) {
                        total -= voucher.getDiscountAmount();
                    } else if (voucher.getDiscountPercent() > 0) {
                        total = total - total * voucher.getDiscountPercent() / 100.0;
                    }
                    order.setVoucher(voucher);
                }
            }
            if (total < 0) {
                total = 0;
            }
            order.setTotal_price(total);
            order.setUser(user);
            order.setFullName(orderRequest.getFullName());
            order.setPhoneNumber(orderRequest.getPhoneNumber());
            order.setEmail(orderRequest.getEmail());
            order.setAddress(orderRequest.getAddress());
            order.setStatus(StatusOrder.PLACED);
            order.setPaymentMethod(PaymentMethod.COD);
            order = orderRepository.save(order); // lưu để lấy ID

            // Tạo OrderDetail và trừ tồn kho
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (CartItemRequest item : items) {
                ProductVariant variant = variantMap.get(item.getVariantId());
                Object stockObj = redisTemplate.opsForValue().get("stock:" + item.getVariantId());
                if (stockObj != null) {
                    variant.setStockQuantity(((Number) stockObj).intValue());
                }

                productVariantRepository.save(variant);

                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProductVariant(variant);
                detail.setQuantity(item.getQuantity());
                detail.setPrice(variant.getProduct().getPrice());

                orderDetails.add(detail);
            }

            orderDetailRepository.saveAll(orderDetails);

            // Xóa giỏ hàng
            cartService.clearCartByUserId(user.getId());

            log.info("Order successfully created: orderId={}, user={}", order.getId(), user.getEmail());
            return convertOrderToOrderResponseDetail(order);
        } catch (Exception e) {
            for (Map.Entry<String, Integer> entry : decrementStock.entrySet()) {
                redisTemplate.opsForValue().increment(entry.getKey(), entry.getValue());
            }
            throw e;
        }
    }

    @Transactional
    public OrderResponse checkoutWithVnPay(OrderRequest orderRequest) throws AppException {
        // 1. Lấy user hiện tại từ token (qua email)
        String email =
                SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new PersistenceException("Unable to authenticate user"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("user not found"));

        // 2. Lấy giỏ hàng
        Cart cart = cartRepository
                .findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new AppException("cart not found"));
        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems()); // <-- Quan trọng!
        if (cartItems.isEmpty()) {
            throw new AppException("cart is empty");
        }

        // 3. Lock các biến thể sản phẩm
        List<Long> variantIds =
                cartItems.stream().map(item -> item.getProductVariant().getId()).toList();
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(variantIds);

        // 4. Kiểm tra tồn kho
        for (CartItem item : cartItems) {
            ProductVariant variant =
                    findVariant(variants, item.getProductVariant().getId());
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException("product " + variant.getProduct().getName() + " insufficient quantity");
            }
        }

        // 5. Tính tổng tiền
        double total = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getProduct().getPrice() * item.getQuantity())
                .sum();

        Order order = new Order();

        if (orderRequest.getVoucherCode() != null
                && !orderRequest.getVoucherCode().isEmpty()) {
            Voucher voucher = this.voucherService.applyVoucher(orderRequest.getVoucherCode(), total);
            if (voucher != null) {
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                this.voucherRepository.save(voucher);
                if (voucher.getDiscountAmount() != null) {
                    total -= voucher.getDiscountAmount();
                } else if (voucher.getDiscountPercent() != null) {
                    total = total - total / 100 * voucher.getDiscountPercent();
                }
                order.setVoucher(voucher);
            }
        }
        if (total < 0) {
            total = 0;
        }
        order.setUser(user);
        order.setStatus(StatusOrder.PENDING);
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setEmail(orderRequest.getEmail());
        order.setAddress(orderRequest.getAddress());
        order.setFullName(orderRequest.getFullName());
        order.setTotal_price(total);
        orderRepository.save(order);

        List<OrderDetail> orderDetails = cartItems.stream()
                .map(item -> {
                    ProductVariant variant = null;
                    try {
                        variant = findVariant(variants, item.getProductVariant().getId());
                    } catch (AppException e) {
                        throw new RuntimeException(e);
                    }

                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(order);
                    detail.setProductVariant(variant);
                    detail.setQuantity(item.getQuantity());

                    return detail;
                })
                .toList();
        orderDetailRepository.saveAll(orderDetails);

        return new OrderResponse(
                order.getId(),
                order.getTotal_price(),
                order.getStatus(),
                order.getFullName(),
                order.getPhoneNumber(),
                order.getEmail(),
                order.getAddress());
    }

    private ProductVariant findVariant(List<ProductVariant> list, Long id) throws AppException {
        return list.stream()
                .filter(pv -> pv.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AppException("product variants not found."));
    }


    @Transactional
    public void cancelOrder(Long userId, Long orderId) throws AppException {
        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if (order.getStatus() != StatusOrder.PENDING) {
            throw new RuntimeException("Only orders that are pending payment can be canceled");
        }

        if (order.getVoucher() != null) {
            Voucher voucher = this.voucherRepository
                    .findById(order.getVoucher().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
            voucher.setUsedCount(order.getVoucher().getUsedCount() - 1);
            voucherRepository.save(voucher);
        }
        orderDetailRepository.deleteByOrderId(orderId);
        orderRepository.delete(order);
    }

    @Transactional
    public void confirmVnPayPayment(long txnRef, boolean success) throws AppException {
        Order order = orderRepository.findById(txnRef).orElseThrow(() -> new ResourceNotFoundException("order not found."));

        if (!order.getStatus().equals(StatusOrder.PENDING)) return;

        if (success) {
            // Trừ hàng
            List<OrderDetail> details = orderDetailRepository.findByOrderId(txnRef);
            for (OrderDetail detail : details) {
                ProductVariant variant = detail.getProductVariant();
                variant.setStockQuantity(variant.getStockQuantity() - detail.getQuantity());
                productVariantRepository.save(variant);
            }

            Long userId = order.getUser().getId();
            this.cartService.clearCartByUserId(userId);
            order.setStatus(StatusOrder.PAID);
        } else {
            order.setStatus(StatusOrder.PAYMENT_FAILED);
        }
        orderRepository.save(order);
    }

    public ResultPaginationDTO getAllOrderByUser(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(orders.getTotalElements());
        meta.setPages(orders.getTotalPages());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setMeta(meta);
        List<OrderResponseDetail> listOrder = orders.getContent().stream()
                .map(this::convertOrderToOrderResponseDetail)
                .collect(Collectors.toList());
        resultPaginationDTO.setResult(listOrder);

        return resultPaginationDTO;
    }

    public Order getOrderStatus(Long orderId) throws AppException {
        Order order;
        order = this.orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("The order does not exist."));
        return order;
    }

    public ResultPaginationDTO getAllOrder(String status, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecfication.hasStatus(status));
        Page<Order> orders = this.orderRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(orders.getTotalElements());
        meta.setPages(orders.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        List<OrderResponseDetail> listOrder = orders.getContent().stream()
                .map(this::convertOrderToOrderResponseDetail)
                .collect(Collectors.toList());
        resultPaginationDTO.setResult(listOrder);
        return resultPaginationDTO;
    }

    public OrderResponseDetail convertOrderToOrderResponseDetail(Order order) {
        OrderResponseDetail orderResponseDetail = new OrderResponseDetail();
        orderResponseDetail.setId(order.getId());
        orderResponseDetail.setAddress(order.getAddress());
        orderResponseDetail.setEmail(order.getEmail());
        orderResponseDetail.setTotal_price(order.getTotal_price());
        orderResponseDetail.setFullName(order.getFullName());
        orderResponseDetail.setCreatedAt(order.getCreatedAt());
        orderResponseDetail.setCreatedBy(order.getCreatedBy());
        orderResponseDetail.setPhoneNumber(order.getPhoneNumber());
        orderResponseDetail.setUpdatedAt(order.getUpdatedAt());
        orderResponseDetail.setUpdatedBy(order.getUpdatedBy());
        orderResponseDetail.setStatus(order.getStatus());
        orderResponseDetail.setPaymentMethod(order.getPaymentMethod());
        OrderResponseDetail.UserOrder userOrder = new OrderResponseDetail.UserOrder();
        userOrder.setId(order.getUser().getId());
        userOrder.setUsername(order.getUser().getUsername());
        orderResponseDetail.setUser(userOrder);
        List<OrderResponseDetail.OrderDetailUser> orderDetailUser = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                OrderResponseDetail.OrderDetailUser userOrderDetail = new OrderResponseDetail.OrderDetailUser();
                userOrderDetail.setId(orderDetail.getId());
                userOrderDetail.setPrice(orderDetail.getPrice());
                userOrderDetail.setProductId(
                        orderDetail.getProductVariant().getProduct().getId());
                userOrderDetail.setQuantity(orderDetail.getQuantity());
                OrderResponseDetail.ProductVariantOrder productVariant = new OrderResponseDetail.ProductVariantOrder();
                productVariant.setId(orderDetail.getProductVariant().getId());
                productVariant.setColor(orderDetail.getProductVariant().getColor());
                productVariant.setSize(orderDetail.getProductVariant().getSize());
                productVariant.setStockQuantity(orderDetail.getProductVariant().getStockQuantity());
                productVariant.setName(
                        orderDetail.getProductVariant().getProduct().getName());
                if (orderDetail.getProductVariant().getProduct() != null
                        && orderDetail.getProductVariant().getProduct().getImages() != null
                        && !orderDetail
                                .getProductVariant()
                                .getProduct()
                                .getImages()
                                .isEmpty()) {

                    byte[] imageBytes = orderDetail
                            .getProductVariant()
                            .getProduct()
                            .getImages()
                            .getFirst()
                            .getBaseImage();
                    if (imageBytes != null) {
                        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                        productVariant.setImage(imageBase64);
                    }
                }

                userOrderDetail.setProductVariant(productVariant);
                orderDetailUser.add(userOrderDetail);
            }
        }
        orderResponseDetail.setOrderDetails(orderDetailUser);
        return orderResponseDetail;
    }

    public OrderResponseDetail getOrderDetail(Long orderId) throws AppException {
        Order order = this.orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("The order does not exist."));
        return convertOrderToOrderResponseDetail(order);
    }

    public OrderResponseDetail changeStatusOrder(Long orderId, StatusOrder status) throws AppException {
        Order order = this.orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("The order does not exist"));
        if (status.equals(order.getStatus())) {
            throw new AppException("Trùng status");
        } else {
            order.setStatus(status);
            this.orderRepository.save(order);
        }
        return convertOrderToOrderResponseDetail(order);
    }

    public OrderResponseDetail changeStatusOrderShipperAndUser(Long orderId, StatusOrder status) throws AppException {
        Order order = this.orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("The order does not exist"));
        if (status.equals(order.getStatus())) {
            throw new AppException("Duplicate status");
        } else {
            if (status.equals(StatusOrder.COMPLETED)
                    || status.equals(StatusOrder.SHIPPING)
                    || status.equals(StatusOrder.DELIVERED)
                    || status.equals(StatusOrder.FAILED_DELIVERY)) {
                order.setStatus(status);
                this.orderRepository.save(order);
            } else {
                throw new AppException("You do not have permission to edit this status");
            }
        }
        return convertOrderToOrderResponseDetail(order);
    }

    public List<DashboardResponse> getStatsByDay(Instant start, Instant end) {
        return orderRepository.statsByDay(start, end);
    }

    public List<DashboardResponse> getStatsByMonth(int month, int year) {
        return orderRepository.statsByDayInMonth(month, year);
    }

    public List<DashboardResponse> getStatsByYear(int year) {
        return orderRepository.statsByMonth(year);
    }


    public DashboardResponse getTotalStats(Instant start, Instant end) {
        return orderRepository.totalStats(start, end);
    }
}
