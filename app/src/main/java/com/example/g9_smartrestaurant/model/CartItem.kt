package com.example.g9_smartrestaurant.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1,
    var note: String = ""
)