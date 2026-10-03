package com.example.g9_smartrestaurant.model
data class Order(
    val id: String = "",
    val customerId: String = "",
    val status: String = "PLACED", // Các trạng thái: PLACED, CONFIRMED, PREPARING, READY, COMPLETED
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val eta: Int = 0 // Thời gian chờ dự kiến
)