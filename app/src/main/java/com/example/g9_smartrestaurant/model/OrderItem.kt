package com.example.g9_smartrestaurant.model

data class OrderItem(
    val menuItemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = ""
)