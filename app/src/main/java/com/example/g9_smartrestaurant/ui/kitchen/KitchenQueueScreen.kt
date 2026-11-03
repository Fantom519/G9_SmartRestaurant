package com.example.g9_smartrestaurant.ui.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.viewmodel.KitchenViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Bảng màu trang trí hiện đại
val DarkBlueHeader = Color(0xFF0F3460) // Xanh đậm TopBar
val AppBackground = Color(0xFFF4F6F9)   // Màu nền trang trí nhẹ nhàng, dịu mắt
val RedSection = Color(0xFFE63946)      // Đỏ - Đơn mới
val BlueSection = Color(0xFF1D3557)     // Xanh đậm - Đang nấu
val GreenSection = Color(0xFF2A9D8F)    // Xanh lá - Hoàn thành

@Composable
fun KitchenQueueScreen(
    viewModel: KitchenViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val orders by viewModel.orders.collectAsState()

    // Lọc danh sách theo từng trạng thái
    val newOrders = orders.filter { it.status == OrderStatus.CONFIRMED }
    val preparingOrders = orders.filter { it.status == OrderStatus.PREPARING }
    val readyOrders = orders.filter { it.status == OrderStatus.READY }

    // Đồng hồ thời gian thực
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime = Calendar.getInstance().time
        }
    }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // --- 1. HEADER (TopBar Xanh Đậm) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlueHeader)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Bếp Việt",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "PHÒNG BẾP",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeFormat,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat,
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Đăng xuất",
                        tint = Color.White
                    )
                }
            }
        }

        // --- 2. DANH SÁCH HÀNG DỌC (VERTICAL LAYOUT) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PHẦN 1: ĐƠN MỚI
            item {
                SectionHeader(title = "ĐƠN MỚI", count = newOrders.size, color = RedSection)
            }
            if (newOrders.isEmpty()) {
                item { EmptySectionCard(message = "Không có đơn mới nào") }
            } else {
                items(newOrders, key = { it.id }) { order ->
                    VerticalOrderCard(
                        order = order,
                        badgeColor = RedSection,
                        buttonColor = RedSection,
                        buttonText = "Nhận đơn",
                        onActionClick = { viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING) }
                    )
                }
            }

            // PHẦN 2: ĐANG NẤU
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(title = "ĐANG NẤU", count = preparingOrders.size, color = BlueSection)
            }
            if (preparingOrders.isEmpty()) {
                item { EmptySectionCard(message = "Chưa có món nào đang nấu") }
            } else {
                items(preparingOrders, key = { it.id }) { order ->
                    VerticalOrderCard(
                        order = order,
                        badgeColor = BlueSection,
                        buttonColor = BlueSection,
                        buttonText = "Hoàn thành",
                        onActionClick = { viewModel.updateOrderStatus(order.id, OrderStatus.READY) }
                    )
                }
            }

            // PHẦN 3: HOÀN THÀNH
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(title = "HOÀN THÀNH", count = readyOrders.size, color = GreenSection)
            }
            if (readyOrders.isEmpty()) {
                item { EmptySectionCard(message = "Chưa có món nào hoàn thành") }
            } else {
                items(readyOrders, key = { it.id }) { order ->
                    VerticalOrderCard(
                        order = order,
                        badgeColor = GreenSection,
                        buttonColor = Color.LightGray,
                        buttonText = "Đã xong",
                        onActionClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun EmptySectionCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VerticalOrderCard(
    order: Order,
    badgeColor: Color,
    buttonColor: Color,
    buttonText: String,
    onActionClick: () -> Unit
) {
    val orderTime = remember(order.createdAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Card: Mã đơn, Số bàn & Giờ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#${order.id.takeLast(6).uppercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bàn ${order.tableNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkBlueHeader
                    )
                }

                Text(
                    text = orderTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color.LightGray.copy(alpha = 0.4f)
            )

            // Danh sách các món ăn trong đơn
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        if (!item.notes.isNullOrEmpty()) {
                            Text(
                                text = "Ghi chú: ${item.notes}",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                    Text(
                        text = "x${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nút chuyển trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (order.status != OrderStatus.READY) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(buttonColor)
                            .clickable { onActionClick() }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Đã xong",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}