package com.example.g9_smartrestaurant.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderItem
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.AlreadyProcessedException
import com.example.g9_smartrestaurant.repository.FirestoreOrderRepository
import com.example.g9_smartrestaurant.repository.OrderFeed
import com.example.g9_smartrestaurant.repository.OrderRepository
import kotlinx.coroutines.delay

// Gom trạng thái đơn vào 4 tab. SERVED nằm ở "Sẵn sàng" vì còn chờ Staff bấm "Hoàn tất".
enum class StaffTab(val label: String, val statuses: Set<String>) {
    PENDING("Chờ xác nhận", setOf(OrderStatus.PLACED)),
    COOKING("Đang chế biến", setOf(OrderStatus.CONFIRMED, OrderStatus.PREPARING)),
    READY("Sẵn sàng", setOf(OrderStatus.READY, OrderStatus.SERVED)),
    DONE("Đã xong", setOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED))
}

/** Điểm vào: nối repository thật với giao diện. Chưa cần đăng nhập để chạy. */
@Composable
fun StaffDashboardRoute(
    repository: OrderRepository = remember { FirestoreOrderRepository() }
) {
    val context = LocalContext.current
    val feedFlow = remember(repository) { repository.observeOrders() }
    val feed by feedFlow.collectAsState(initial = OrderFeed(loading = true))

    // Đồng hồ để cập nhật "Chờ X phút" mà không cần đơn đổi
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = System.currentTimeMillis()
        }
    }

    // Đơn đang xử lý: khóa nút để tránh bấm đúp
    val busyIds = remember { mutableStateListOf<String>() }

    fun run(order: Order, action: ((Result<Unit>) -> Unit) -> Unit) {
        busyIds.add(order.id)
        action { result ->
            busyIds.remove(order.id)
            result.exceptionOrNull()?.let { e ->
                val msg = if (e is AlreadyProcessedException || e.cause is AlreadyProcessedException)
                    "Đơn đã được xử lý" else "Lỗi: ${e.message}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    StaffDashboardScreen(
        feed = feed,
        now = now,
        busyIds = busyIds,
        onConfirm = { o -> run(o) { cb -> repository.confirmOrder(o.id, cb) } },
        onReject = { o, reason -> run(o) { cb -> repository.rejectOrder(o.id, reason, cb) } },
        onServed = { o -> run(o) { cb -> repository.markServed(o.id, cb) } },
        onComplete = { o -> run(o) { cb -> repository.completeOrder(o.id, cb) } }
    )
}

/** Giao diện thuần: không biết Firebase hay đăng nhập, nên xem được bằng @Preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    feed: OrderFeed,
    now: Long,
    busyIds: List<String>,
    onConfirm: (Order) -> Unit,
    onReject: (Order, String) -> Unit,
    onServed: (Order) -> Unit,
    onComplete: (Order) -> Unit,
    initialTab: StaffTab = StaffTab.PENDING
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var rejectTarget by remember { mutableStateOf<Order?>(null) }

    val counts = StaffTab.values().associateWith { tab ->
        feed.orders.count { it.status in tab.statuses }
    }
    val visible = feed.orders
        .filter { it.status in selectedTab.statuses }
        .let { list ->
            // Tab chờ xác nhận: đơn chờ lâu nhất lên đầu. Các tab khác: mới nhất lên đầu.
            if (selectedTab == StaffTab.PENDING) list.sortedBy { it.createdAt }
            else list.sortedByDescending { it.createdAt }
        }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Smart Restaurant · Nhân viên",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StaffOrange
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (feed.isOffline || feed.error != null) DangerRed else Color(0xFF2E7D32),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            feed.error != null -> "Mất kết nối, đang thử lại"
                            feed.isOffline -> "Đang ngoại tuyến, dữ liệu có thể chưa mới"
                            else -> "Đã kết nối, cập nhật tự động"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StaffTab.values().toList()) { tab ->
                        val count = counts[tab] ?: 0
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(if (count > 0) "${tab.label} ($count)" else tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StaffOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                feed.loading -> CircularProgressIndicator(color = StaffOrange)
                feed.error != null && feed.orders.isEmpty() ->
                    Text("Không tải được đơn hàng. Kiểm tra kết nối mạng.", color = Color.Gray)
                visible.isEmpty() -> Text("Chưa có đơn nào trong mục này", color = Color.Gray)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(visible, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            now = now,
                            busy = order.id in busyIds,
                            onConfirm = { onConfirm(order) },
                            onReject = { rejectTarget = order },
                            onServed = { onServed(order) },
                            onComplete = { onComplete(order) }
                        )
                    }
                }
            }
        }
    }

    rejectTarget?.let { order ->
        RejectDialog(
            order = order,
            onDismiss = { rejectTarget = null },
            onConfirm = { reason ->
                onReject(order, reason)
                rejectTarget = null
            }
        )
    }
}

@Composable
private fun RejectDialog(order: Order, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val reasons = listOf("Hết món", "Khách yêu cầu hủy", "Đơn bị trùng", "Lý do khác")
    var selected by remember { mutableStateOf(reasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Từ chối đơn ${orderCode(order)}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = reason },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == reason,
                            onClick = { selected = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = StaffOrange)
                        )
                        Text(reason, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Khách sẽ nhận được lý do này.", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Từ chối đơn") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Quay lại") } }
    )
}

// ---------- Preview: xem ngay trong Android Studio, không cần chạy app ----------

private fun sampleOrders(now: Long) = listOf(
    Order(
        id = "x9a1102", tableNumber = "Bàn 05", status = OrderStatus.PLACED, total = 195000.0,
        createdAt = now - 4 * 60_000L,
        items = listOf(OrderItem("Phở bò", 80000.0, 2), OrderItem("Cà phê sữa", 35000.0, 1, "ít đá"))
    ),
    Order(
        id = "x9a1103", tableNumber = "Bàn 02", status = OrderStatus.PLACED, total = 110000.0,
        createdAt = now - 60_000L,
        items = listOf(OrderItem("Cơm tấm sườn bì chả", 50000.0, 1), OrderItem("Trà đào", 60000.0, 1))
    ),
    Order(
        id = "x9a1099", tableNumber = "Bàn 03", status = OrderStatus.PREPARING, total = 160000.0,
        createdAt = now - 8 * 60_000L, eta = 3,
        items = listOf(OrderItem("Phở bò", 80000.0, 2))
    ),
    Order(
        id = "x9a1098", tableNumber = "Bàn 07", status = OrderStatus.READY, total = 50000.0,
        createdAt = now - 12 * 60_000L,
        items = listOf(OrderItem("Cơm tấm sườn bì chả", 50000.0, 1))
    )
)

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun PendingTabPreview() {
    val now = System.currentTimeMillis()
    MaterialTheme {
        StaffDashboardScreen(
            feed = OrderFeed(orders = sampleOrders(now)), now = now, busyIds = emptyList(),
            onConfirm = {}, onReject = { _, _ -> }, onServed = {}, onComplete = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun CookingTabPreview() {
    val now = System.currentTimeMillis()
    MaterialTheme {
        StaffDashboardScreen(
            feed = OrderFeed(orders = sampleOrders(now)), now = now, busyIds = emptyList(),
            onConfirm = {}, onReject = { _, _ -> }, onServed = {}, onComplete = {},
            initialTab = StaffTab.COOKING
        )
    }
}
