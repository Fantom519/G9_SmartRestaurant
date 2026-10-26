package com.example.g9_smartrestaurant.ui.kitchen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.viewmodel.KitchenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenQueueScreen(
    viewModel: KitchenViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = if (selectedFilter == null) {
        orders
    } else {
        orders.filter { it.status == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Màn Hình Bếp (KDS)", fontWeight = FontWeight.Bold)
                        Text(
                            "Quản lý & Chế biến món ăn trong ngày",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. Thống kê nhanh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Chờ nấu",
                    count = orders.count { it.status == OrderStatus.CONFIRMED },
                    color = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Đang nấu",
                    count = orders.count { it.status == OrderStatus.PREPARING },
                    color = Color(0xFFFB8C00),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sẵn sàng",
                    count = orders.count { it.status == OrderStatus.READY },
                    color = Color(0xFF43A047),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Các Filter Chip
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Tất cả") }
                )
                FilterChip(
                    selected = selectedFilter == OrderStatus.CONFIRMED,
                    onClick = { selectedFilter = OrderStatus.CONFIRMED },
                    label = { Text("Chờ nấu") }
                )
                FilterChip(
                    selected = selectedFilter == OrderStatus.PREPARING,
                    onClick = { selectedFilter = OrderStatus.PREPARING },
                    label = { Text("Đang nấu") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Danh sách đơn hàng trong bếp
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có đơn hàng nào trong ngày hôm nay", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredOrders) { order ->
                        KitchenOrderCard(
                            order = order,
                            onStatusChange = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
            Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun KitchenOrderCard(
    order: Order,
    onStatusChange: (OrderStatus) -> Unit
) {
    val (statusLabel, statusColor) = when (order.status) {
        OrderStatus.CONFIRMED -> "Chờ nấu" to Color(0xFFE53935)
        OrderStatus.PREPARING -> "Đang nấu" to Color(0xFFFB8C00)
        OrderStatus.READY -> "Hoàn thành" to Color(0xFF43A047)
        else -> order.status.name to Color.Gray
    }

    // Định dạng hiển thị giờ:phút - ngày/tháng/năm
    val formattedDateTime = remember(order.createdAt) {
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bàn ${order.tableNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mã: ${order.id.takeLast(4)}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    // Dòng hiển thị ngày giờ đặt món
                    Text(
                        text = "Đặt lúc: $formattedDateTime",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Danh sách món ăn
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        if (!item.notes.isNullOrEmpty()) {
                            Text(
                                text = "• Ghi chú: ${item.notes}",
                                fontSize = 13.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                    Text(
                        text = "x${item.quantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thao tác chuyển trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (order.status) {
                    OrderStatus.CONFIRMED -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8C00))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bắt đầu nấu")
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.READY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đã nấu xong")
                        }
                    }
                    OrderStatus.READY -> {
                        Text(
                            "Đã xong - Chờ nhân viên lên món",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}