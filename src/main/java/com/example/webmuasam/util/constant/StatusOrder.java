package com.example.webmuasam.util.constant;

public enum StatusOrder {
    PENDING,           // Tạo đơn
    PAYMENT_PENDING,   // Chờ thanh toán (MoMo)
    PLACED,            // Đặt hàng thành công bằng COD
    PAID,              // Đã thanh toán
    CONFIRMED,         // Shop xác nhận
    PACKED,            // Đóng gói
    SHIPPING,          // Đang giao
    DELIVERED,         // Shipper giao thành công
    CUSTOMER_CONFIRMED,// Khách xác nhận đã nhận hàng
    COMPLETED,         // Hoàn tất (gộp trạng thái trên nếu muốn)
    CANCELLED,         // Hủy đơn
    PAYMENT_FAILED,    // Thanh toán thất bại
    REFUNDED,          // Hoàn tiền
    FAILED_DELIVERY    // Giao thất bại (COD)
}
