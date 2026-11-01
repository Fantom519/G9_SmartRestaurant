package com.example.g9_smartrestaurant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class KitchenViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {

    // Mốc 00:00:00 đầu ngày hôm nay
    private fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // 1. Danh sách đơn hàng active của HÔM NAY
    val orders: StateFlow<List<Order>> = repository.observeActiveOrders()
        .map { orderList ->
            val startOfToday = getStartOfToday()
            orderList.filter { order ->
                order.createdAt >= startOfToday
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Danh sách đơn hàng LỊCH SỬ (các ngày trước)
    val historyOrders: StateFlow<List<Order>> = repository.observeActiveOrders()
        .map { orderList ->
            val startOfToday = getStartOfToday()
            orderList.filter { order ->
                order.createdAt < startOfToday
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }
}