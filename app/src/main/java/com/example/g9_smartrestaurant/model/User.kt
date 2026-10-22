
package com.example.g9_smartrestaurant.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = UserRole.CUSTOMER.name,
    val createdAt: Long = System.currentTimeMillis()
)