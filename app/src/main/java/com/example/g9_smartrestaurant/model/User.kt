package com.example.g9_smartrestaurant.model
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Customer" // Mặc định là khách hàng
)