package com.example.webmuasam.service;

import com.example.webmuasam.Specification.OrderSpecfication;
import com.example.webmuasam.dto.Request.CartItemDTO;
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
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.metamodel.mapping.ordering.ast.OrderingSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Builder
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final VoucherRepository voucherRepository;
    private final VoucherService voucherService;
    public OrderService (OrderRepository orderRepository,ProductVariantRepository productVariantRepository,OrderDetailRepository orderDetailRepository,CartRepository cartRepository,CartItemRepository cartItemRepository,UserRepository userRepository,CartService cartService,VoucherRepository voucherRepository,VoucherService voucherService) {
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.voucherRepository = voucherRepository;
        this.voucherService = voucherService;
    }
    @Transactional
    public OrderResponseDetail createOrderByCash(OrderRequestByCash orderRequest) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Token không hợp lệ"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Email không tồn tại"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));

        // Copy list cartItem tránh lỗi khi xóa sau đó dùng lại
        List<CartItemDTO> items = cart.getCartItems().stream().map(ci -> new CartItemDTO(ci.getProductVariant().getId(),ci.getQuantity())).collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new AppException("Giỏ hàng đang trống");
        }

        // Lấy danh sách variant để kiểm kho
        List<Long> variantIds = items.stream()
                .map(CartItemDTO::getVariantId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(variantIds);

        // Kiểm tra tồn kho từng sản phẩm
        for (CartItemDTO item : items) {
            ProductVariant variant = findVariant(variants, item.getVariantId());
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException("Sản phẩm " + variant.getProduct().getName() + " không đủ hàng");
            }
        }
        double total = items.stream()
                .mapToDouble(item -> {
                    try {
                        ProductVariant productVariant = this.productVariantRepository.findById(item.getVariantId()).orElseThrow(()-> new AppException("productVariant khong ton tai"));
                        return productVariant.getProduct().getPrice() * item.getQuantity();
                    } catch (AppException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sum();

        // Tạo đơn hàng
        Order order = new Order();
        if(orderRequest.getVoucherCode()!=null && !orderRequest.getVoucherCode().isEmpty()){
            Voucher voucher = this.voucherService.applyVoucher(orderRequest.getVoucherCode(),total);
            if(voucher!=null){
                voucher.setUsedCount(voucher.getUsedCount()+1);
                this.voucherRepository.save(voucher);
                if(voucher.getDiscountAmount()>0){
                    total-=voucher.getDiscountAmount();
                }else if(voucher.getDiscountPercent()>0){
                    total=total - total * voucher.getDiscountPercent() /100.0;
                }
                order.setVoucher(voucher);
            }
        }
        if(total<0){
            total=0;
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
        for (CartItemDTO item : items) {
            ProductVariant variant = findVariant(variants, item.getVariantId());

            // Trừ tồn kho
            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
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

        log.info("Tạo đơn hàng thành công: orderId={}, user={}", order.getId(), user.getEmail());
        return convertOrderToOrderResponseDetail(order);
    }




    @Transactional
    public OrderResponse checkoutWithVnPay(OrderRequest orderRequest)throws AppException {
        // 1. Lấy user hiện tại từ token (qua email)
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Không thể xác thực người dùng"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));

        // 2. Lấy giỏ hàng
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));
        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems()); // <-- Quan trọng!
        if (cartItems.isEmpty()) {
            throw new AppException("Giỏ hàng trống");
        }

        // 3. Lock các biến thể sản phẩm
        List<Long> variantIds = cartItems.stream()
                .map(item -> item.getProductVariant().getId())
                .toList();
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(variantIds);

        // 4. Kiểm tra tồn kho
        for (CartItem item : cartItems) {
            ProductVariant variant = findVariant(variants, item.getProductVariant().getId());
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException("Sản phẩm " + variant.getProduct().getName() + " không đủ số lượng");
            }
        }

        // 5. Tính tổng tiền
        double total = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getProduct().getPrice() * item.getQuantity())
                .sum();

        // 6. Tạo đơn hàng (chưa trừ tồn kho vội)
        Order order = new Order();

        if(orderRequest.getVoucherCode()!=null && !orderRequest.getVoucherCode().isEmpty()){
            Voucher voucher = this.voucherService.applyVoucher(orderRequest.getVoucherCode(),total);
            if(voucher!=null){
                voucher.setUsedCount(voucher.getUsedCount()+1);
                this.voucherRepository.save(voucher);
                if(voucher.getDiscountAmount()!=null){
                    total-=voucher.getDiscountAmount();
                }else if(voucher.getDiscountPercent()!=null){
                    total=total - total/100*voucher.getDiscountPercent();
                }
                order.setVoucher(voucher);
            }
        }
        if(total<0){
            total=0;
        }
        order.setUser(user);
        order.setStatus(StatusOrder.PENDING); // chờ thanh toán
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setEmail(orderRequest.getEmail());
        order.setAddress(orderRequest.getAddress());
        order.setFullName(orderRequest.getFullName());
        order.setTotal_price(total);
        orderRepository.save(order);

        // 7. Tạo danh sách OrderDetail
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
                }).toList();
        orderDetailRepository.saveAll(orderDetails);



        // 9. Trả về OrderResponse (FE dùng redirect đến VNPay)
        return new OrderResponse(
                order.getId(),
                order.getTotal_price(),
                order.getStatus(),
                order.getFullName(),
                order.getPhoneNumber(),
                order.getEmail(),
                order.getAddress()
        );
    }

    private ProductVariant findVariant(List<ProductVariant> list, Long id) throws AppException{
        return list.stream()
                .filter(pv -> pv.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AppException("Không tìm thấy biến thể sản phẩm"));
    }

//    @Scheduled(fixedRate = 60000) // mỗi 1 phút
//    @Transactional
//    public void cancelOrder() {
//        Instant now = Instant.now();
//
//        List<Order> orders = orderRepository.findByStatusAndCreatedAtBefore(
//                StatusOrder.PENDING.toString(),
//                now.minusSeconds(900) // timeout 15 phút
//        );
//
//        for (Order order : orders) {
//            order.setStatus(StatusOrder.CANCELLED);
//
//            // Nếu có voucher thì hoàn lại
//            if (order.getVoucher() != null) {
//                Voucher voucher = order.getVoucher();
//                if (voucher.getUsedCount() > 0) {
//                    voucher.setUsedCount(voucher.getUsedCount() - 1);
//                    voucherRepository.save(voucher);
//                }
//            }
//        }
//    }


    @Transactional
    public void cancelOrder(Long userId, Long orderId) throws AppException{
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != StatusOrder.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ thanh toán");
        }

        if(order.getVoucher() != null) {
            Voucher voucher = this.voucherRepository.findById(order.getVoucher().getId()).orElseThrow(()-> new AppException("Không tìm thấy voucher"));
            voucher.setUsedCount(order.getVoucher().getUsedCount() - 1);
            voucherRepository.save(voucher);
        }
        // Xóa đơn hàng và chi tiết
        orderDetailRepository.deleteByOrderId(orderId);
        orderRepository.delete(order);
    }


    @Transactional
    public void confirmVnPayPayment(long txnRef, boolean success)throws AppException {
        Order order = orderRepository.findById(txnRef)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng"));

        if (!order.getStatus().equals(StatusOrder.PENDING)) return;

        if (success) {
            // Trừ hàng
            List<OrderDetail> details = orderDetailRepository.findByOrderId(txnRef);
            for (OrderDetail detail : details) {
                ProductVariant variant = detail.getProductVariant();
                variant.setStockQuantity(variant.getStockQuantity() - detail.getQuantity());
                productVariantRepository.save(variant);
            }


            Long userId= order.getUser().getId();
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
        List<OrderResponseDetail> listOrder = orders.getContent().stream().map(this::convertOrderToOrderResponseDetail).collect(Collectors.toList());
        resultPaginationDTO.setResult(listOrder);

        return resultPaginationDTO;
    }


    public Order getOrderStatus(Long orderId) throws AppException {
        Order order;
        order = this.orderRepository.findById(orderId).orElseThrow(()->new AppException("order khong ton tai"));
        return order;
    }

    public ResultPaginationDTO getAllOrder(String status, Pageable pageable){
        Specification<Order> spec = Specification.where(OrderSpecfication.hasStatus(status));
        Page<Order> orders = this.orderRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());

        meta.setTotal(orders.getTotalElements());
        meta.setPages(orders.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        List<OrderResponseDetail> listOrder = orders.getContent().stream().map(this::convertOrderToOrderResponseDetail).collect(Collectors.toList());
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
                userOrderDetail.setProductId(orderDetail.getProductVariant().getProduct().getId());
                userOrderDetail.setQuantity(orderDetail.getQuantity());
                OrderResponseDetail.ProductVariantOrder productVariant = new OrderResponseDetail.ProductVariantOrder();
                productVariant.setId(orderDetail.getProductVariant().getId());
                productVariant.setColor(orderDetail.getProductVariant().getColor());
                productVariant.setSize(orderDetail.getProductVariant().getSize());
                productVariant.setStockQuantity(orderDetail.getProductVariant().getStockQuantity());
                productVariant.setName(orderDetail.getProductVariant().getProduct().getName());
                if (orderDetail.getProductVariant().getProduct() != null
                        && orderDetail.getProductVariant().getProduct().getImages() != null
                        && !orderDetail.getProductVariant().getProduct().getImages().isEmpty()) {

                    byte[] imageBytes = orderDetail.getProductVariant()
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
        Order order = this.orderRepository.findById(orderId).orElseThrow(()->new AppException("order khong ton tai"));
        return convertOrderToOrderResponseDetail(order);
    }

    public OrderResponseDetail changeStatusOrder(Long orderId,StatusOrder status) throws AppException {
        Order order = this.orderRepository.findById(orderId).orElseThrow(()-> new AppException("order khong ton tai"));
        if(status.equals(order.getStatus())) {
            throw new AppException("Trùng status");
        }else{
            order.setStatus(status);
            this.orderRepository.save(order);
        }
        return convertOrderToOrderResponseDetail(order);
    }
    public OrderResponseDetail changeStatusOrderShipperAndUser(Long orderId,StatusOrder status) throws AppException {
        Order order = this.orderRepository.findById(orderId).orElseThrow(()-> new AppException("order khong ton tai"));
        if(status.equals(order.getStatus())) {
            throw new AppException("Trùng status");
        }else{
            if(status.equals(StatusOrder.COMPLETED) || status.equals(StatusOrder.SHIPPING) || status.equals(StatusOrder.DELIVERED) || status.equals(StatusOrder.FAILED_DELIVERY)) {
                order.setStatus(status);
                this.orderRepository.save(order);
            }else{
                throw new AppException("Bạn không có quyền sửa trạng thái này");
            }
        }
        return convertOrderToOrderResponseDetail(order);
    }

    public List<DashboardResponse> getStatsByDay(Instant start, Instant end) {
        List<Order> orders = orderRepository.findAll(OrderSpecfication.createdAtBetween(start, end));

        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    LocalDate date = list.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                                    long userCount = list.stream().map(o -> o.getUser().getId()).distinct().count();
                                    long orderCount = list.size();
                                    double revenue = list.stream().mapToDouble(Order::getTotal_price).sum();
                                    return new DashboardResponse(date.toString(), userCount, orderCount, revenue);
                                }
                        )
                ))
                .values().stream()
                .sorted((a,b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());
    }

    /** Lọc thống kê theo tháng (tháng + năm) */
    public List<DashboardResponse> getStatsByMonth(int month, int year) {
        // Tạo ngày bắt đầu và kết thúc của tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Lọc đơn hàng trong tháng đó
        List<Order> orders = orderRepository.findAll(OrderSpecfication.createdAtBetween(start, end));

        // Gom nhóm theo từng ngày có đơn hàng
        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    LocalDate date = list.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                                    long userCount = list.stream().map(o -> o.getUser().getId()).distinct().count();
                                    long orderCount = list.size();
                                    double revenue = list.stream().mapToDouble(Order::getTotal_price).sum();
                                    return new DashboardResponse(date.toString(), userCount, orderCount, revenue);
                                }
                        )
                ))
                .values().stream()
                .sorted(Comparator.comparing(DashboardResponse::getLabel)) // Sắp xếp theo ngày
                .collect(Collectors.toList());
    }


    /** Lọc thống kê theo năm */
    public List<DashboardResponse> getStatsByYear(int year) {
        List<Order> orders = orderRepository.findAll(OrderSpecfication.createdAtYear(year));

        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> YearMonth.from(o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    YearMonth ym = YearMonth.from(list.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate());
                                    long userCount = list.stream().map(o -> o.getUser().getId()).distinct().count();
                                    long orderCount = list.size();
                                    double revenue = list.stream().mapToDouble(Order::getTotal_price).sum();
                                    return new DashboardResponse(ym.toString(), userCount, orderCount, revenue);
                                }
                        )
                ))
                .values().stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());
    }
}
