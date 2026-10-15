package com.example.g9_smartrestaurant.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1,
    var notes: String = ""
) {
    fun toOrderItem(): OrderItem {
        return OrderItem(
            menuItemId = menuItem.id,
            name = menuItem.name,
            price = menuItem.price,
            quantity = quantity,
            notes = notes
        )
    }
}