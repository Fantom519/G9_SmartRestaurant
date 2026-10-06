package com.example.g9_smartrestaurant.repository

import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ordersCollection = firestore.collection("orders")

    suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val docRef = ordersCollection.document()
            val orderWithId = order.copy(id = docRef.id)
            docRef.set(orderWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("status", newStatus.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeActiveOrders(): Flow<List<Order>> = callbackFlow {
        val listener: ListenerRegistration = ordersCollection
            .whereIn("status", listOf(
                OrderStatus.PLACED.name,
                OrderStatus.CONFIRMED.name,
                OrderStatus.PREPARING.name,
                OrderStatus.READY.name
            ))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                    trySend(orders)
                }
            }

        awaitClose { listener.remove() }
    }

    fun observeOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)?.copy(id = snapshot.id)
                trySend(order)
            }

        awaitClose { listener.remove() }
    }
}