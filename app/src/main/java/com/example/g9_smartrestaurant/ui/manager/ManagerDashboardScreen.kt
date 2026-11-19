package com.example.g9_smartrestaurant.ui.manager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.components.NotificationBell
import com.example.g9_smartrestaurant.ui.components.TableManagementScreen
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CustomerFeedbackItem(
    val id: String = "",
    val userEmail: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    onNavigateToTracking: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    // 0: Tổng quan, 1: Sơ đồ bàn, 2: Thực đơn, 3: Đơn hàng, 4: Tài khoản
    var selectedTab by remember { mutableIntStateOf(0) }

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoadingMenu by remember { mutableStateOf(true) }

    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(true) }

    var feedbackList by remember { mutableStateOf<List<CustomerFeedbackItem>>(emptyList()) }

    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showFeedbackListDialog by remember { mutableStateOf(false) }

    // 1. Lắng nghe thực đơn Realtime từ 'menu_items'
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

    // 2. Lắng nghe toàn bộ đơn hàng Realtime từ 'orders'
    LaunchedEffect(Unit) {
        db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                isLoadingOrders = false
                if (error == null && snapshot != null) {
                    ordersList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // 3. Lắng nghe đánh giá khảo sát từ 'feedbacks'
    LaunchedEffect(Unit) {
        db.collection("feedbacks")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    feedbackList = snapshot.documents.map { doc ->
                        CustomerFeedbackItem(
                            id = doc.id,
                            userEmail = doc.getString("userEmail") ?: "Khách hàng",
                            rating = (doc.getLong("rating") ?: 5L).toInt(),
                            comment = doc.getString("comment") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    }.sortedByDescending { it.createdAt }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(BluePrimary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Bếp Việt - Quản lý", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            Text("Hệ thống quản trị nhà hàng", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    // Nút gửi thông báo quảng cáo / khảo sát / nội bộ
                    IconButton(onClick = { showBroadcastDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Gửi thông báo",
                            tint = BluePrimary
                        )
                    }

                    // Chuông thông báo hệ thống
                    NotificationBell(userRole = UserRole.MANAGER)

                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất", tint = RedAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Tổng quan
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Tổng quan") },
                    label = { Text("Tổng quan", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                // Tab 1: Sơ đồ bàn
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Sơ đồ bàn") },
                    label = { Text("Sơ đồ bàn", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                // Tab 2: Thực đơn
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Thực đơn") },
                    label = { Text("Thực đơn", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                // Tab 3: Đơn hàng
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        val activeOrders = ordersList.count { !it.status.name.contains("COMPLETED") && !it.status.name.contains("CANCELLED") }
                        if (activeOrders > 0) {
                            BadgedBox(badge = { Badge(containerColor = RedAccent) { Text("$activeOrders") } }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Đơn hàng")
                            }
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Đơn hàng")
                        }
                    },
                    label = { Text("Đơn hàng", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                // Tab 4: Tài khoản
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Tài khoản") },
                    label = { Text("Tài khoản", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
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
                0 -> ManagerOverviewTab(
                    ordersList = ordersList,
                    menuList = menuList,
                    feedbackList = feedbackList,
                    onOpenBroadcast = { showBroadcastDialog = true },
                    onOpenFeedbackList = { showFeedbackListDialog = true },
                    onOrderClick = { selectedOrderForDetail = it }
                )
                1 -> TableManagementScreen(
                    onOrderClick = { selectedOrderForDetail = it }
                )
                2 -> ManagerMenuTab(menuList = menuList, isLoading = isLoadingMenu, db = db)
                3 -> ManagerOrdersTab(
                    ordersList = ordersList,
                    isLoading = isLoadingOrders,
                    onOrderClick = { selectedOrderForDetail = it }
                )
                4 -> ManagerProfileTab(
                    onOpenBroadcast = { showBroadcastDialog = true },
                    onLogout = onLogout
                )
            }
        }
    }

    // POPUP XEM CHI TIẾT ĐƠN HÀNG
    selectedOrderForDetail?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onNavigateToTracking = { orderId ->
                selectedOrderForDetail = null
                onNavigateToTracking(orderId)
            }
        )
    }

    // POPUP SOẠN & GỬI THÔNG BÁO (QUẢNG CÁO, KHẢO SÁT, NỘI BỘ)
    if (showBroadcastDialog) {
        BroadcastNotificationDialog(
            onDismiss = { showBroadcastDialog = false },
            onSentSuccess = {
                showBroadcastDialog = false
                Toast.makeText(context, "📢 Đã phát thông báo tới các thiết bị!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // POPUP XEM ĐÁNH GIÁ KHẢO SÁT TỪ KHÁCH HÀNG
    if (showFeedbackListDialog) {
        FeedbackListDialog(
            feedbackList = feedbackList,
            onDismiss = { showFeedbackListDialog = false }
        )
    }
}

// ==========================================
// TAB 0: TỔNG QUAN & BÁO CÁO DOANH THU
// ==========================================
@Composable
fun ManagerOverviewTab(
    ordersList: List<Order>,
    menuList: List<MenuItem>,
    feedbackList: List<CustomerFeedbackItem>,
    onOpenBroadcast: () -> Unit,
    onOpenFeedbackList: () -> Unit,
    onOrderClick: (Order) -> Unit
) {
    val formatter = remember { DecimalFormat("#,###đ") }

    var timeFilterIndex by remember { mutableIntStateOf(0) }
    val filterLabels = listOf("Hôm nay", "7 ngày qua", "Tháng này", "Tất cả")

    val filteredOrders = remember(ordersList, timeFilterIndex) {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        val startOfThisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        when (timeFilterIndex) {
            0 -> ordersList.filter { it.createdAt >= startOfToday }
            1 -> ordersList.filter { it.createdAt >= sevenDaysAgo }
            2 -> ordersList.filter { it.createdAt >= startOfThisMonth }
            else -> ordersList
        }
    }

    val totalRevenue = filteredOrders
        .filter { order ->
            val statusStr = try { order.status.name } catch (e: Exception) { order.status.toString() }
            statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true)
        }
        .sumOf { it.total }

    val completedOrdersCount = filteredOrders.count {
        val statusStr = try { it.status.name } catch (e: Exception) { it.status.toString() }
        statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true)
    }
    val cancelledOrdersCount = filteredOrders.count {
        val statusStr = try { it.status.name } catch (e: Exception) { it.status.toString() }
        statusStr.contains("CANCELLED", ignoreCase = true)
    }

    val avgRating = remember(feedbackList) {
        if (feedbackList.isEmpty()) 0.0 else feedbackList.map { it.rating }.average()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Báo cáo doanh thu & Thống kê", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterLabels.forEachIndexed { index, label ->
                FilterChip(
                    selected = timeFilterIndex == index,
                    onClick = { timeFilterIndex = index },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BluePrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOANH THU HOÀN THÀNH (${filterLabels[timeFilterIndex].uppercase()})",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF81C784))
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(formatter.format(totalRevenue), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tổng đơn", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("${filteredOrders.size} đơn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Hoàn thành", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("$completedOrdersCount đơn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                    }
                    Column {
                        Text("Đã hủy", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("$cancelledOrdersCount đơn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thực đơn", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${menuList.size} món ăn", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = RedAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trung bình/Đơn xong", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val avg = if (completedOrdersCount > 0) totalRevenue / completedOrdersCount else 0.0
                    Text(formatter.format(avg), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // THẺ TRUYỀN THÔNG, KHUYẾN MÃI & KHẢO SÁT KHÁCH HÀNG
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Truyền thông & Khảo sát", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BluePrimary)
                            val ratingText = if (feedbackList.isEmpty()) {
                                "Chưa có đánh giá khảo sát"
                            } else {
                                String.format(Locale.forLanguageTag("vi-VN"), "⭐ %.1f/5 (%d đánh giá)", avgRating, feedbackList.size)
                            }
                            Text(ratingText, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }

                    TextButton(onClick = onOpenFeedbackList) {
                        Text("Xem (${feedbackList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onOpenBroadcast,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi ưu đãi / Khảo sát / Thông báo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Đơn hàng gần đây", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Nhấn để xem chi tiết", fontSize = 11.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Text("Không có đơn hàng nào trong khoảng thời gian này", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            filteredOrders.take(5).forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { onOrderClick(order) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(order.tableNumber.ifBlank { "Bàn 101" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("#${order.id.takeLast(6).uppercase()}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text("Xem chi tiết đơn →", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
                        }
                        Text(formatter.format(order.total), fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// DIALOG SOẠN & GỬI THÔNG BÁO (QUẢNG CÁO, KHẢO SÁT, NỘI BỘ)
// ==========================================
@Composable
fun BroadcastNotificationDialog(
    onDismiss: () -> Unit,
    onSentSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Đối tượng nhận: CUSTOMER, STAFF, KITCHEN, ALL
    val targetOptions = listOf(
        "CUSTOMER" to "Khách hàng",
        "STAFF" to "Nhân viên",
        "KITCHEN" to "Phòng bếp",
        "ALL" to "Tất cả"
    )
    var selectedTarget by remember { mutableStateOf("CUSTOMER") }

    // Loại thông báo: PROMO, SURVEY, INTERNAL
    val typeOptions = listOf(
        "PROMO" to "Ưu đãi / Giảm giá",
        "SURVEY" to "Khảo sát dịch vụ",
        "INTERNAL" to "Thông báo chung"
    )
    var selectedType by remember { mutableStateOf("PROMO") }

    var title by remember { mutableStateOf("Ưu đãi cuối tuần giảm 20%!") }
    var message by remember { mutableStateOf("Nhập mã BEPVIET20 khi thanh toán để nhận ưu đãi giảm 20% tổng hóa đơn tại Bếp Việt.") }
    var promoCode by remember { mutableStateOf("BEPVIET20") }
    var isSending by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = RedAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gửi thông báo hệ thống", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("1. Chọn nhóm người nhận:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targetOptions.forEach { (roleKey, label) ->
                        FilterChip(
                            selected = selectedTarget == roleKey,
                            onClick = {
                                selectedTarget = roleKey
                                if (roleKey != "CUSTOMER" && selectedType != "INTERNAL") {
                                    selectedType = "INTERNAL"
                                    title = "Thông báo vận hành ca làm việc"
                                    message = "Nhân sự các bộ phận lưu ý kiểm tra nguyên liệu và quy trình phục vụ giờ cao điểm."
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("2. Loại thông báo (Mẫu nhanh):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.forEach { (typeKey, label) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = {
                                selectedType = typeKey
                                when (typeKey) {
                                    "PROMO" -> {
                                        selectedTarget = "CUSTOMER"
                                        title = "Ưu đãi cuối tuần giảm 20%!"
                                        message = "Nhập mã BEPVIET20 để nhận ngay ưu đãi giảm 20% cho tất cả các món đặc sản tại Bếp Việt."
                                        promoCode = "BEPVIET20"
                                    }
                                    "SURVEY" -> {
                                        selectedTarget = "CUSTOMER"
                                        title = "Khảo sát trải nghiệm dùng bữa"
                                        message = "Bếp Việt rất mong nhận được đánh giá 5 sao và góp ý của bạn để nâng cao chất lượng phục vụ!"
                                        promoCode = ""
                                    }
                                    "INTERNAL" -> {
                                        title = "Thông báo từ Quản lý nhà hàng"
                                        message = "Cập nhật thực đơn món mới trong ngày. Vui lòng kiểm tra danh sách món trước khi phục vụ."
                                        promoCode = ""
                                    }
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề thông báo", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedType == "PROMO") {
                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = { promoCode = it.uppercase().trim() },
                        label = { Text("Mã giảm giá đính kèm (VD: BEPVIET20)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFFE65100)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Nội dung chi tiết", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ tiêu đề và nội dung!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSending = true
                    NotificationHelper.sendNotification(
                        title = title.trim(),
                        message = message.trim(),
                        type = selectedType,
                        targetRole = selectedTarget,
                        promoCode = if (selectedType == "PROMO") promoCode else null
                    ) { success ->
                        isSending = false
                        if (success) {
                            onSentSuccess()
                        } else {
                            Toast.makeText(context, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("Gửi ngay", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

// ==========================================
// DIALOG XEM DANH SÁCH ĐÁNH GIÁ KHẢO SÁT TỪ KHÁCH HÀNG
// ==========================================
@Composable
fun FeedbackListDialog(
    feedbackList: List<CustomerFeedbackItem>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text("Đánh giá từ Khách hàng (${feedbackList.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BluePrimary)
        },
        text = {
            if (feedbackList.isEmpty()) {
                Text("Chưa có khách hàng nào gửi phản hồi khảo sát.", fontSize = 13.sp, color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(feedbackList) { fb ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(fb.userEmail, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                    Row {
                                        repeat(fb.rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                if (fb.comment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(fb.comment, fontSize = 12.sp, color = Color.DarkGray)
                                }
                                if (fb.createdAt > 0L) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(dateFormat.format(Date(fb.createdAt)), fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đóng", color = Color.White)
            }
        }
    )
}

// ==========================================
// TAB 2: QUẢN LÝ THỰC ĐƠN
// ==========================================
@Composable
fun ManagerMenuTab(
    menuList: List<MenuItem>,
    isLoading: Boolean,
    db: FirebaseFirestore
) {
    val context = LocalContext.current
    val formatter = remember { DecimalFormat("#,###đ") }

    var showAddDialog by remember { mutableStateOf(false) }
    var menuItemToEdit by remember { mutableStateOf<MenuItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Quản lý Thực đơn", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                Text("Tổng số: ${menuList.size} món", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thêm món", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (menuList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có món ăn nào trong Thực đơn", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(menuList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = BluePrimary)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Danh mục: ${item.categoryId.ifBlank { "Cơm" }}", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⏱️ ${item.prepTime} phút", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                }

                                Text(formatter.format(item.price), fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 13.sp)
                            }

                            Row {
                                IconButton(onClick = { menuItemToEdit = item }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = BluePrimary, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    db.collection("menu_items").document(item.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Đã xóa ${item.name}", Toast.LENGTH_SHORT).show()
                                        }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = RedAccent, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MenuItemEditDialog(
            menuItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, price, categoryId, prepTime, imageUrl ->
                val newMap = mapOf(
                    "name" to name,
                    "price" to price,
                    "categoryId" to categoryId,
                    "prepTime" to prepTime,
                    "imageUrl" to imageUrl,
                    "isAvailable" to true
                )
                db.collection("menu_items").add(newMap)
                    .addOnSuccessListener {
                        showAddDialog = false
                        Toast.makeText(context, "Thêm món mới thành công!", Toast.LENGTH_SHORT).show()
                    }
            }
        )
    }

    menuItemToEdit?.let { editItem ->
        MenuItemEditDialog(
            menuItem = editItem,
            onDismiss = { menuItemToEdit = null },
            onSave = { name, price, categoryId, prepTime, imageUrl ->
                val updateMap = mapOf(
                    "name" to name,
                    "price" to price,
                    "categoryId" to categoryId,
                    "prepTime" to prepTime,
                    "imageUrl" to imageUrl
                )
                db.collection("menu_items").document(editItem.id).update(updateMap)
                    .addOnSuccessListener {
                        menuItemToEdit = null
                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    }
            }
        )
    }
}

// DIALOG THÊM / SỬA MÓN
@Composable
fun MenuItemEditDialog(
    menuItem: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(menuItem?.name ?: "") }
    var priceStr by remember { mutableStateOf(menuItem?.price?.toLong()?.toString() ?: "") }
    var categoryId by remember { mutableStateOf(menuItem?.categoryId ?: "Cơm") }
    var prepTimeStr by remember { mutableStateOf(menuItem?.prepTime?.toString() ?: "15") }
    var imageUrl by remember { mutableStateOf(menuItem?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (menuItem == null) "Thêm món ăn mới" else "Chỉnh sửa món ăn",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BluePrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Giá tiền (VNĐ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = categoryId,
                        onValueChange = { categoryId = it },
                        label = { Text("Danh mục") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = prepTimeStr,
                        onValueChange = { prepTimeStr = it },
                        label = { Text("Chuẩn bị (phút)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Đường dẫn hình ảnh (URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val prepTime = prepTimeStr.toIntOrNull() ?: 15
                    if (name.isNotBlank() && price > 0) {
                        onSave(name, price, categoryId, prepTime, imageUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
            ) {
                Text("Lưu món", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

// ==========================================
// TAB 3: QUẢN LÝ ĐƠN HÀNG
// ==========================================
@Composable
fun ManagerOrdersTab(
    ordersList: List<Order>,
    isLoading: Boolean,
    onOrderClick: (Order) -> Unit
) {
    val formatter = remember { DecimalFormat("#,###đ") }
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tất cả đơn hàng hệ thống", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
        Text("Nhấn vào đơn bất kỳ để xem chi tiết thông tin", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (ordersList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đơn hàng nào", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ordersList) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(order.tableNumber.ifBlank { "Bàn 101" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("#${order.id.takeLast(6).uppercase()}", fontSize = 11.sp, color = Color.Gray)
                                }

                                val statusStr = try { order.status.name } catch (e: Exception) { order.status.toString() }
                                val statusText = when {
                                    statusStr.contains("PLACED", ignoreCase = true) -> "Đã gửi"
                                    statusStr.contains("CONFIRMED", ignoreCase = true) -> "Đã duyệt"
                                    statusStr.contains("PREPARING", ignoreCase = true) -> "Đang nấu"
                                    statusStr.contains("READY", ignoreCase = true) -> "Sẵn sàng"
                                    statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true) -> "Hoàn thành"
                                    statusStr.contains("CANCELLED", ignoreCase = true) -> "Đã hủy"
                                    else -> "Đã đặt"
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (statusStr.contains("CANCELLED")) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
                                ) {
                                    Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = BluePrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dateFormat.format(Date(order.createdAt)), fontSize = 11.sp, color = Color.Gray)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                            order.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.quantity}x ${item.name}", fontSize = 13.sp)
                                    Text(formatter.format(item.price * item.quantity), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Xem chi tiết đơn →", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                Text(formatter.format(order.total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// POPUP CHI TIẾT ĐƠN HÀNG DÀNH CHO QUẢN LÝ
// ==========================================
@Composable
fun OrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val formatter = remember { DecimalFormat("#,###đ") }
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }

    val statusStr = try { order.status.name } catch (e: Exception) { order.status.toString() }
    val isCompleted = statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true)
    val isCancelled = statusStr.contains("CANCELLED", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn #${order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BluePrimary
                )
                Text(
                    text = order.tableNumber.ifBlank { "Bàn 101" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Thời gian đặt: ${dateFormat.format(Date(order.createdAt))}", fontSize = 12.sp, color = Color.Gray)
                Text("Mã khách hàng: ${order.customerId.ifBlank { "Khách vãng lai" }}", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> Color(0xFFE8F5E9)
                        isCancelled -> Color(0xFFFFEBEE)
                        else -> Color(0xFFE3F2FD)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToTracking(order.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val statusText = when {
                                isCompleted -> "✅ Trạng thái: Đã hoàn thành"
                                isCancelled -> "❌ Trạng thái: Đã hủy (${if (order.cancelReason.isNullOrBlank()) "Khách hủy" else order.cancelReason})"
                                else -> "🔄 Trạng thái: Đang xử lý trên hệ thống"
                            }
                            Text(
                                text = statusText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when {
                                    isCompleted -> Color(0xFF2E7D32)
                                    isCancelled -> RedAccent
                                    else -> BluePrimary
                                }
                            )
                            Text(
                                text = "Bấm để xem tiến trình thời gian thực ➔",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(10.dp))

                Text("Danh sách món ăn:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${item.quantity}x ${item.name}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            if (item.notes.isNotBlank()) {
                                Text("Ghi chú: ${item.notes}", fontSize = 11.sp, color = RedAccent)
                            }
                        }
                        Text(formatter.format(item.price * item.quantity), fontSize = 13.sp, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TỔNG TIỀN:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(formatter.format(order.total), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RedAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đóng", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ==========================================
// TAB 4: TÀI KHOẢN QUẢN LÝ
// ==========================================
@Composable
fun ManagerProfileTab(
    onOpenBroadcast: () -> Unit,
    onLogout: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Quản lý Hệ thống", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
        Text(currentUser?.email ?: "manager@bepviet.com", fontSize = 13.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenBroadcast,
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gửi thông báo & Khảo sát", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất khỏi hệ thống", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}