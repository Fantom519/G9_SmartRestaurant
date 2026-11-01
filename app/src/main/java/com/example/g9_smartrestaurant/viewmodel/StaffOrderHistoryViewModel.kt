
package com.example.g9_smartrestaurant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffOrderHistoryViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            try {
                repository.observeOrderHistory().collect { orderList ->
                    _orders.value = orderList.sortedByDescending {
                        it.createdAt
                    }
                    _isLoading.value = false
                    _error.value = null
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
                    ?: "Không thể tải lịch sử đơn hàng"
            }
        }
    }
}
