package com.example.g9_smartrestaurant.repository

import com.example.g9_smartrestaurant.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore

class MenuRepository {
    private val db = FirebaseFirestore.getInstance()

    // Hàm lấy danh sách món ăn từ Firestore về
    fun getMenuItems(onSuccess: (List<MenuItem>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("menu_items")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { doc ->
                    // Ép dữ liệu từ Document của Firebase sang Data Class MenuItem
                    val item = doc.toObject(MenuItem::class.java)
                    item?.copy(id = doc.id) // Gán thêm Document ID nếu cần
                }
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun placeOrder(
        tableNumber: String,
        items: List<com.example.g9_smartrestaurant.model.CartItem>,
        totalAmount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val orderData = hashMapOf(
            "tableNumber" to tableNumber,
            "total" to totalAmount,
            "status" to "PLACED",
            "createdAt" to System.currentTimeMillis(),
            "items" to items.map {
                hashMapOf(
                    "name" to it.menuItem.name,
                    "price" to it.menuItem.price,
                    "quantity" to it.quantity,
                    "note" to it.note // Lưu thêm ghi chú món ăn lên Firestore
                )
            }
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}