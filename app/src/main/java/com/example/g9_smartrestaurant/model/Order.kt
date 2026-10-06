package com.example.g9_smartrestaurant.model

data class Order(
    val id: String = "",
    val customerId: String = "",
    val tableNumber: String = "",
    val status: OrderStatus = OrderStatus.PLACED,
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val eta: Int = 0,
    val items: List<OrderItem> = emptyList()
)