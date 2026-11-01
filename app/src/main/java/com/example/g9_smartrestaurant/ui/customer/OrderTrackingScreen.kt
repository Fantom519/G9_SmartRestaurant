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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onNavigateBack: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val formatter = remember { DecimalFormat("#,###đ") }

    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Lắng nghe Realtime trạng thái đơn hàng từ Firestore
    LaunchedEffect(orderId) {
        if (orderId.isNotBlank()) {
            db.collection("orders").document(orderId)
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error == null && snapshot != null && snapshot.exists()) {
                        order = snapshot.toObject(Order::class.java)?.copy(id = snapshot.id)
                    }
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theo dõi đơn hàng", fontWeight = FontWeight.Bold, color = BluePrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = BluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy dữ liệu đơn hàng", color = Color.Gray)
            }
        } else {
            val currentOrder = order!!
            val statusStr = try { currentOrder.status.name } catch (e: Exception) { currentOrder.status.toString() }

            // Xác định bước hiện tại (1: Đã đặt, 2: Đã duyệt, 3: Đang nấu, 4: Xong món)
            val currentStep = when {
                statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true) || statusStr.contains("READY", ignoreCase = true) -> 4
                statusStr.contains("PREPARING", ignoreCase = true) || statusStr.contains("COOKING", ignoreCase = true) -> 3
                statusStr.contains("CONFIRMED", ignoreCase = true) -> 2
                else -> 1
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Khối Thanh Trạng Thái Tiến Trình (Timeline)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bàn: ${currentOrder.tableNumber.ifBlank { "Bàn 101" }}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("#${currentOrder.id.takeLast(6).uppercase()}", color = Color.Gray, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Vẽ 4 bước trạng thái
                        val steps = listOf("Đã đặt", "Đã duyệt", "Đang nấu", "Xong món")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.forEachIndexed { index, title ->
                                val stepNumber = index + 1
                                val isDone = stepNumber <= currentStep

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (isDone) BluePrimary else Color(0xFFE0E0E0),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        color = if (isDone) BluePrimary else Color.Gray,
                                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Món đã gọi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                // Danh sách món ăn chi tiết
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentOrder.items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.quantity}x ${item.name}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    if (item.notes.isNotBlank()) {
                                        Text("Ghi chú: ${item.notes}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Text(formatter.format(item.price * item.quantity), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Khối Tổng thanh toán
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(formatter.format(currentOrder.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                    }
                }
            }
        }
    }
}