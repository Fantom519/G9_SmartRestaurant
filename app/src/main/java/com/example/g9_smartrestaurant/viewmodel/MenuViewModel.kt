package com.example.g9_smartrestaurant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.Category
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.MenuRepository
import com.example.g9_smartrestaurant.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MenuUiState(
    val categories: List<Category> = emptyList(),
    val menuItems: List<MenuItem> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastPlacedOrderId: String? = null
)

class MenuViewModel(
    private val menuRepository: MenuRepository = MenuRepository(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val categoriesResult = menuRepository.getCategories()
            val menuResult = menuRepository.getMenuItems(_uiState.value.selectedCategoryId)

            if (menuResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    categories = categoriesResult.getOrDefault(emptyList()),
                    menuItems = menuResult.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = menuResult.exceptionOrNull()?.message ?: "Lỗi tải thực đơn"
                )
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadData()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addToCart(menuItem: MenuItem) {
        val currentList = _uiState.value.cartItems.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == menuItem.id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(quantity = currentList[index].quantity + 1)
        } else {
            currentList.add(CartItem(menuItem = menuItem, quantity = 1))
        }
        _uiState.value = _uiState.value.copy(cartItems = currentList)
    }

    fun updateQuantity(itemId: String, delta: Int) {
        val currentList = _uiState.value.cartItems.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == itemId }
        if (index != -1) {
            val newQty = currentList[index].quantity + delta
            if (newQty > 0) {
                currentList[index] = currentList[index].copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
            _uiState.value = _uiState.value.copy(cartItems = currentList)
        }
    }

    // Bổ sung: Cập nhật ghi chú cho từng món
    fun updateItemNotes(itemId: String, notes: String) {
        val currentList = _uiState.value.cartItems.toMutableList()
        val index = currentList.indexOfFirst { it.menuItem.id == itemId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(notes = notes)
            _uiState.value = _uiState.value.copy(cartItems = currentList)
        }
    }

    // Bổ sung: Xóa sạch giỏ hàng
    fun clearCart() {
        _uiState.value = _uiState.value.copy(cartItems = emptyList())
    }

    fun placeOrder(tableNumber: String, onSuccess: (String) -> Unit) {
        val currentCart = _uiState.value.cartItems
        if (currentCart.isEmpty()) return

        val orderItems = currentCart.map { it.toOrderItem() }
        val total = currentCart.sumOf { it.menuItem.price * it.quantity }

        val newOrder = Order(
            tableNumber = tableNumber,
            status = OrderStatus.PLACED,
            total = total,
            items = orderItems
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = orderRepository.placeOrder(newOrder)
            if (result.isSuccess) {
                val orderId = result.getOrNull() ?: ""
                _uiState.value = _uiState.value.copy(
                    cartItems = emptyList(),
                    isLoading = false,
                    lastPlacedOrderId = orderId
                )
                onSuccess(orderId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Lỗi đặt món"
                )
            }
        }
    }
}