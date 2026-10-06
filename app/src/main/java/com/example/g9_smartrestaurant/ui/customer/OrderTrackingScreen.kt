package com.example.g9_smartrestaurant.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.OrderRepository
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.ui.theme.SurfaceGray
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onNavigateBack: () -> Unit = {}
) {
    val orderRepository = remember { OrderRepository() }
    val orderState by orderRepository.observeOrderById(orderId).collectAsState(initial = null)

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi đơn hàng", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { innerPadding ->
        val currentOrder = orderState
        if (currentOrder == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Thẻ thông tin đơn hàng
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Bàn: ${currentOrder.tableNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "#${currentOrder.id.takeLast(6).uppercase()}", color = Color.Gray, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stepper tiến trình trạng thái
                        OrderProgressBar(status = currentOrder.status)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Danh sách món đã đặt
                Text(text = "Món đã gọi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentOrder.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${item.quantity}x ${item.name}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    if (item.notes.isNotBlank()) {
                                        Text(text = "Ghi chú: ${item.notes}", fontSize = 12.sp, color = RedAccent)
                                    }
                                }
                                Text(
                                    text = currencyFormatter.format(item.price * item.quantity),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tổng thanh toán
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = currencyFormatter.format(currentOrder.total),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RedAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderProgressBar(status: OrderStatus) {
    val steps = listOf(
        OrderStatus.PLACED to "Đã đặt",
        OrderStatus.CONFIRMED to "Đã duyệt",
        OrderStatus.PREPARING to "Đang nấu",
        OrderStatus.READY to "Xong món"
    )

    val currentStepIndex = when (status) {
        OrderStatus.PLACED -> 0
        OrderStatus.CONFIRMED -> 1
        OrderStatus.PREPARING -> 2
        OrderStatus.READY, OrderStatus.COMPLETED -> 3
        OrderStatus.CANCELLED -> -1
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, pair ->
            val isDone = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isDone) BluePrimary else SurfaceGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(text = "${index + 1}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pair.second,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) BluePrimary else Color.Gray
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (index < currentStepIndex) BluePrimary else SurfaceGray)
                )
            }
        }
    }
}