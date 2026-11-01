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
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tổng quan, 1: Thực đơn, 2: Đơn hàng, 3: Tài khoản

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoadingMenu by remember { mutableStateOf(true) }

    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(true) }

    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

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
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Tổng quan") },
                    label = { Text("Tổng quan", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Thực đơn") },
                    label = { Text("Thực đơn", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
                        val activeOrders = ordersList.count { !it.status.name.contains("COMPLETED") && !it.status.name.contains("CANCELLED") }
                        if (activeOrders > 0) {
                            BadgedBox(badge = { Badge(containerColor = RedAccent) { Text("$activeOrders") } }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Đơn hàng")
                            }
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Đơn hàng")
                        }
                    },
                    label = { Text("Đơn hàng", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
                0 -> ManagerOverviewTab(
                    ordersList = ordersList,
                    menuList = menuList,
                    onOrderClick = { selectedOrderForDetail = it }
                )
                1 -> ManagerMenuTab(menuList = menuList, isLoading = isLoadingMenu, db = db)
                2 -> ManagerOrdersTab(
                    ordersList = ordersList,
                    isLoading = isLoadingOrders,
                    onOrderClick = { selectedOrderForDetail = it }
                )
                3 -> ManagerProfileTab(onLogout = onLogout)
            }
        }
    }

    // POPUP XEM CHI TIẾT ĐƠN HÀNG (CHỈ XEM - KHÔNG ĐỔI TRẠNG THÁI)
    selectedOrderForDetail?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selectedOrderForDetail = null }
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
    onOrderClick: (Order) -> Unit
) {
    val formatter = remember { DecimalFormat("#,###đ") }

    // Bộ lọc thời gian: 0: Hôm nay, 1: 7 ngày qua, 2: Tháng này, 3: Tất cả
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

    // YÊU CẦU 1: CHỈ ĐƠN HOÀN THÀNH MỚI ĐƯỢC TÍNH VÀO DOANH THU
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Báo cáo doanh thu & Thống kê", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary)

        Spacer(modifier = Modifier.height(10.dp))

        // Chip lọc thời gian
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

        // Thẻ Doanh thu
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

        // Thống kê phụ 2 ô
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

        Spacer(modifier = Modifier.height(20.dp))

        // Danh sách Đơn hàng
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
// TAB 1: QUẢN LÝ THỰC ĐƠN
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
// TAB 2: QUẢN LÝ ĐƠN HÀNG
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
    onDismiss: () -> Unit
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

                // YÊU CẦU 2 & 3: HIỂN THỊ THÔNG TIN THEO TRẠNG THÁI HOÀN THÀNH HOẶC HỦY
                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("✅ Trạng thái: Đã hoàn thành", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                            Text("Phục vụ bởi: NV_BếpViệt_01", fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                            Text("Thời gian xong: ${dateFormat.format(Date(order.createdAt + 15 * 60 * 1000))}", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                } else if (isCancelled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("❌ Trạng thái: Đã hủy", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RedAccent)
                            Text(
                                text = "Lý do hủy: ${if (order.cancelReason.isNullOrBlank()) "Khách yêu cầu hủy" else order.cancelReason}",
                                fontSize = 11.sp,
                                color = RedAccent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔄 Trạng thái: Đang xử lý trên hệ thống",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BluePrimary,
                            modifier = Modifier.padding(10.dp)
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
// TAB 3: TÀI KHOẢN QUẢN LÝ
// ==========================================
@Composable
fun ManagerProfileTab(
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

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất khỏi hệ thống", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}