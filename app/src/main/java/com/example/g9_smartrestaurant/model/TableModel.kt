package com.example.g9_smartrestaurant.model

enum class TableStatus {
    EMPTY,      // Bàn trống (Màu xanh lá)
    RESERVED,   // Đã đặt trước (Màu cam)
    OCCUPIED    // Đang có khách (Màu đỏ/xanh dương)
}

data class TableItem(
    val id: String = "",
    val name: String = "",
    val status: TableStatus = TableStatus.EMPTY,
    val activeOrder: Order? = null
)