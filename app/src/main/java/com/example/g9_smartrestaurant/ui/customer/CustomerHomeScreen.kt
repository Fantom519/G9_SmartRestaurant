package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    cartItems: SnapshotStateMap<String, CartItem>,
    initialTab: Int = 0,
    selectedTableNumber: String,
    onTableSelected: (String) -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var showAvatarMenu by remember { mutableStateOf(false) }

    // STATE BÀN DÙNG CHUNG TOÀN BỘ PHÂN HỆ KHÁCH HÀNG (GIỮ NGUYÊN KHI CHUYỂN TAB)
    var showTableDialog by remember { mutableStateOf(false) }

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoadingMenu by remember { mutableStateOf(true) }

    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(true) }

    // Tải thực đơn từ Firestore
    LaunchedEffect(Unit) {
        db.collection("menu_items").addSnapshotListener { snapshot, error ->
            isLoadingMenu = false
            if (error == null && snapshot != null) {
                menuList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    // Lắng nghe đơn hàng lọc theo customerId
    LaunchedEffect(currentUserId) {
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                isLoadingOrders = false
                if (error == null && snapshot != null) {
                    val allOrders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                    ordersList = if (currentUserId.isNotBlank()) {
                        allOrders.filter { it.customerId == currentUserId }
                            .sortedByDescending { it.createdAt }
                    } else {
                        allOrders.sortedByDescending { it.createdAt }
                    }
                }
            }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Đơn hàng") },
                    label = { Text("Đơn hàng", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        val totalCount = cartItems.values.sumOf { it.quantity }
                        if (totalCount > 0) {
                            BadgedBox(
                                badge = { Badge(containerColor = RedAccent) { Text("$totalCount") } }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng")
                            }
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng")
                        }
                    },
                    label = { Text("Giỏ hàng", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
                    label = { Text("Tài khoản", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )
            }
        },
        containerColor = BgLight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(
                    menuList = menuList,
                    isLoading = isLoadingMenu,
                    selectedTableNumber = selectedTableNumber,
                    onOpenTableDialog = { showTableDialog = true },
                    onNavigateToMenu = onNavigateToMenu,
                    showAvatarMenu = showAvatarMenu,
                    onToggleAvatarMenu = { isShow: Boolean -> showAvatarMenu = isShow },
                    onNavigateToProfile = { selectedTab = 3 },
                    onAddToCart = { menuItem: MenuItem ->
                        val currentItem = cartItems[menuItem.id]
                        if (currentItem != null) {
                            cartItems[menuItem.id] = currentItem.copy(quantity = currentItem.quantity + 1)
                        } else {
                            cartItems[menuItem.id] = CartItem(menuItem = menuItem, quantity = 1)
                        }
                        Toast.makeText(context, "Đã thêm ${menuItem.name} vào giỏ!", Toast.LENGTH_SHORT).show()
                    },
                    onLogout = onLogout
                )

                1 -> OrderHistoryTabContent(
                    orders = ordersList,
                    isLoading = isLoadingOrders,
                    onOrderClick = { orderId: String ->
                        onNavigateToTracking(orderId)
                    }
                )

                2 -> CartTabContent(
                    cartItems = cartItems,
                    currentUserId = currentUserId,
                    selectedTableNumber = selectedTableNumber,
                    onOpenTableDialog = { showTableDialog = true },
                    onNavigateToMenu = onNavigateToMenu,
                    onOrderPlacedSuccess = { orderId: String ->
                        cartItems.clear()
                        selectedTab = 1
                        onNavigateToTracking(orderId)
                    }
                )

                3 -> CustomerProfileScreen(onLogout = onLogout)
            }
        }
    }

    // DIALOG QUÉT QR / CHỌN BÀN TOÀN CỤC
    if (showTableDialog) {
        TableSelectionDialog(
            currentTable = selectedTableNumber,
            onTableSelected = { newTable ->
                onTableSelected(newTable)
                showTableDialog = false
            },
            onDismiss = { showTableDialog = false }
        )
    }
}