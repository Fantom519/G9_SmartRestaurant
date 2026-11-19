package com.example.g9_smartrestaurant.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "ORDER_NEW",       // ORDER_NEW | ORDER_STATUS | PROMO | SURVEY | INTERNAL
    val targetRole: String = "STAFF",     // CUSTOMER | STAFF | KITCHEN | MANAGER | ALL
    val targetUserId: String? = null,     // UID cụ thể nếu gửi riêng 1 khách; null nếu gửi cả nhóm
    val orderId: String? = null,          // ID đơn hàng liên quan (nếu có)
    val promoCode: String? = null,        // Mã giảm giá đính kèm (nếu là PROMO)
    val readBy: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)