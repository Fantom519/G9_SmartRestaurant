package com.example.g9_smartrestaurant.repository

import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Gói dữ liệu + trạng thái kết nối để giao diện hiển thị banner
data class OrderFeed(
    val orders: List<Order> = emptyList(),
    val isOffline: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

// Đơn đã bị người khác (Staff khác / khách) đổi trạng thái trước
class AlreadyProcessedException : Exception("Đơn đã được xử lý")

interface OrderRepository {
    fun observeOrders(): Flow<OrderFeed>
    fun confirmOrder(orderId: String, onResult: (Result<Unit>) -> Unit)
    fun rejectOrder(orderId: String, reason: String, onResult: (Result<Unit>) -> Unit)
    fun markServed(orderId: String, onResult: (Result<Unit>) -> Unit)
    fun completeOrder(orderId: String, onResult: (Result<Unit>) -> Unit)
}

class FirestoreOrderRepository : OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val orders = db.collection("orders")

    // Lắng nghe realtime: có đơn mới hoặc đổi trạng thái là danh sách tự cập nhật
    override fun observeOrders(): Flow<OrderFeed> = callbackFlow {
        val registration = orders
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(OrderFeed(error = error.message ?: "Lỗi kết nối"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                    trySend(OrderFeed(orders = list, isOffline = snapshot.metadata.isFromCache))
                }
            }
        awaitClose { registration.remove() }
    }

    override fun confirmOrder(orderId: String, onResult: (Result<Unit>) -> Unit) = transition(
        orderId, setOf(OrderStatus.PLACED), OrderStatus.CONFIRMED,
        mapOf("confirmedAt" to System.currentTimeMillis()), onResult
    )

    override fun rejectOrder(orderId: String, reason: String, onResult: (Result<Unit>) -> Unit) = transition(
        orderId, setOf(OrderStatus.PLACED, OrderStatus.CONFIRMED), OrderStatus.CANCELLED,
        mapOf("cancelReason" to reason), onResult
    )

    override fun markServed(orderId: String, onResult: (Result<Unit>) -> Unit) = transition(
        orderId, setOf(OrderStatus.READY), OrderStatus.SERVED, emptyMap(), onResult
    )

    override fun completeOrder(orderId: String, onResult: (Result<Unit>) -> Unit) = transition(
        orderId, setOf(OrderStatus.SERVED), OrderStatus.COMPLETED, emptyMap(), onResult
    )

    // Chỉ đổi trạng thái nếu đơn vẫn đang ở trạng thái hợp lệ (chống 2 Staff bấm cùng lúc)
    private fun transition(
        orderId: String,
        allowedFrom: Set<String>,
        to: String,
        extra: Map<String, Any>,
        onResult: (Result<Unit>) -> Unit
    ) {
        val ref = orders.document(orderId)
        db.runTransaction { tx ->
            val current = tx.get(ref).getString("status")
            if (current !in allowedFrom) throw AlreadyProcessedException()
            tx.update(ref, extra + ("status" to to))
            null
        }
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
