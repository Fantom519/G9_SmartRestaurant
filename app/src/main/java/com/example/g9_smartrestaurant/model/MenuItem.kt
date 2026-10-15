package com.example.g9_smartrestaurant.model
data class MenuItem(
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val prepTime: Double = 0.0, // Thời gian chuẩn bị (phút) - rất quan trọng để tính ETA sau này
    val imageUrl: String = "",
    val description: String = "",
    val isAvailable: Boolean = true // Còn món hay đã hết hàng
)