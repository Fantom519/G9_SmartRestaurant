package com.example.g9_smartrestaurant.model

object OrderStatus {
    const val PLACED = "PLACED"
    const val CONFIRMED = "CONFIRMED"
    const val PREPARING = "PREPARING"
    const val READY = "READY"
    const val SERVED = "SERVED"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}

data class OrderItem(
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val note: String = ""
)

data class Order(
    val id: String = "",
    val customerId: String = "",
    val tableNumber: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: String = OrderStatus.PLACED,
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long = 0L,
    val eta: Int = 0,
    val cancelReason: String = ""
)