package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryTabContent(
    orders: List<Order>,
    isLoading: Boolean,
    onOrderClick: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val formatter = remember { DecimalFormat("#,###đ") }
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }

    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách đơn hàng",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )
        Text(
            text = "Nhấn vào đơn hàng bất kỳ để xem chi tiết thời gian thực",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bạn chưa có đơn hàng nào", fontSize = 15.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (order.id.isNotBlank()) {
                                    onOrderClick(order.id)
                                }
                            },
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
                                    Text(
                                        text = order.tableNumber.ifBlank { "Bàn 101" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "#${order.id.takeLast(6).uppercase()}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                val statusStr = try { order.status.name } catch (e: Exception) { order.status.toString() }
                                val (statusText, statusBg, statusTextColor) = when {
                                    statusStr.contains("PLACED", ignoreCase = true) -> Triple("Đã gửi", Color(0xFFE3F2FD), BluePrimary)
                                    statusStr.contains("CONFIRMED", ignoreCase = true) -> Triple("Đã duyệt", Color(0xFFFFF3E0), Color(0xFFE65100))
                                    statusStr.contains("PREPARING", ignoreCase = true) || statusStr.contains("COOKING", ignoreCase = true) -> Triple("Đang nấu", Color(0xFFFFF8E1), Color(0xFFF57F17))
                                    statusStr.contains("READY", ignoreCase = true) -> Triple("Sẵn sàng", Color(0xE8E8F5E9), Color(0xFF2E7D32))
                                    statusStr.contains("COMPLETED", ignoreCase = true) || statusStr.contains("SERVED", ignoreCase = true) -> Triple("Hoàn thành", Color(0xE8E8F5E9), Color(0xFF2E7D32))
                                    statusStr.contains("CANCELLED", ignoreCase = true) -> Triple("Đã hủy", Color(0xFFFFEBEE), RedAccent)
                                    else -> Triple("Đã đặt", Color(0xFFE3F2FD), BluePrimary)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = statusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTextColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dateFormat.format(Date(order.createdAt)),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

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

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        if (order.id.isNotBlank()) onOrderClick(order.id)
                                    }
                                ) {
                                    Text("Xem chi tiết", fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusStr = try { order.status.name } catch (e: Exception) { order.status.toString() }
                                    if (statusStr.contains("PLACED", ignoreCase = true)) {
                                        OutlinedButton(
                                            onClick = { orderToCancel = order },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Hủy đơn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Text(
                                        formatter.format(order.total),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Popup xác nhận Hủy đơn
    orderToCancel?.let { order ->
        AlertDialog(
            onDismissRequest = { if (!isCancelling) orderToCancel = null },
            title = {
                Text("Xác nhận hủy đơn", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = RedAccent)
            },
            text = {
                Text("Bạn có chắc chắn muốn hủy đơn hàng #${order.id.takeLast(6).uppercase()} này không?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isCancelling = true
                        db.collection("orders").document(order.id)
                            .update(
                                mapOf(
                                    "status" to "CANCELLED",
                                    "cancelReason" to "Khách yêu cầu hủy"
                                )
                            )
                            .addOnSuccessListener {
                                isCancelling = false
                                orderToCancel = null
                                Toast.makeText(context, "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                isCancelling = false
                                Toast.makeText(context, "Lỗi hủy đơn: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    enabled = !isCancelling,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Đồng ý hủy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { orderToCancel = null },
                    enabled = !isCancelling
                ) {
                    Text("Bỏ qua", color = Color.Gray, fontSize = 13.sp)
                }
            }
        )
    }
}