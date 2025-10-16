package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.OrderRequestByCash;
import com.example.webmuasam.dto.Response.DashboardResponse;
import com.example.webmuasam.dto.Response.OrderResponseDetail;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Order;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.OrderService;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.annotation.ApiMessage;
import com.example.webmuasam.util.constant.StatusOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {


    private final OrderService orderService;
    private final UserRepository UserRepository;


    @PostMapping("/cash")
    public ResponseEntity<OrderResponseDetail> createOrderByCash(@Valid @RequestBody OrderRequestByCash orderRequest)throws AppException {
        return ResponseEntity.ok().body(this.orderService.createOrderByCash(orderRequest));
    }
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Không xác thực được người dùng"));
        User user = this.UserRepository.findByEmail(email).orElseThrow(()->new AppException("user not found"));
        orderService.cancelOrder(user.getId(), orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<ResultPaginationDTO> getOrdersByCurrentUser(Pageable pageable) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Không xác thực được người dùng"));
        User user = this.UserRepository.findByEmail(email).orElseThrow(()->new AppException("user not found"));
        ResultPaginationDTO response = orderService.getAllOrderByUser(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}/status")
    public ResponseEntity<Order> getOrdersByIdStatus(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok().body(this.orderService.getOrderStatus(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDetail> getOrderById(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok().body(this.orderService.getOrderDetail(id));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getOrders(@RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(this.orderService.getAllOrder(status, pageable));
    }


    @PutMapping("/status-admin")
    public ResponseEntity<OrderResponseDetail> changeStatusOrder(@RequestParam Long orderId, @RequestParam StatusOrder status)throws AppException{
        return ResponseEntity.ok().body(this.orderService.changeStatusOrder(orderId, status));
    }

    @PutMapping("/status")
    public ResponseEntity<OrderResponseDetail> changeStatusOrderShipperAndUser(@RequestParam Long orderId, @RequestParam StatusOrder status)throws AppException{
        return ResponseEntity.ok().body(this.orderService.changeStatusOrderShipperAndUser(orderId, status));
    }


    @GetMapping("/day")
    public ResponseEntity<List<DashboardResponse>> getStatsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        return ResponseEntity.ok(this.orderService.getStatsByDay(start, end));
    }

    /** Thống kê theo tháng: year */
    @GetMapping("/month")
    public ResponseEntity<List<DashboardResponse>> getStatsByMonth(@RequestParam int month,@RequestParam int year) {
        return ResponseEntity.ok(this.orderService.getStatsByMonth(month,year));
    }

    /** Thống kê theo năm */
    @GetMapping("/year")
    public ResponseEntity<List<DashboardResponse>> getStatsByYear(@RequestParam int year) {
        return ResponseEntity.ok(this.orderService.getStatsByYear(year));
    }

}
