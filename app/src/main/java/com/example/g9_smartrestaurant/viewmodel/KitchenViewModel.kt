package com.example.g9_smartrestaurant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class KitchenViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    init {
        observeKitchenOrders()
    }

    private fun observeKitchenOrders() {
        viewModelScope.launch {
            // Lắng nghe realtime từ Firestore
            repository.observeActiveOrders()
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { activeOrders ->
                    // Bếp chỉ quan tâm các đơn đã được Nhân viên xác nhận (CONFIRMED),
                    // đang nấu (PREPARING) hoặc đã nấu xong (READY)
                    val kitchenOrders = activeOrders.filter {
                        it.status == OrderStatus.CONFIRMED ||
                                it.status == OrderStatus.PREPARING ||
                                it.status == OrderStatus.READY
                    }
                    // Sắp xếp đơn cũ nhất lên đầu
                    _orders.value = kitchenOrders.sortedBy { it.id }
                }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }
}