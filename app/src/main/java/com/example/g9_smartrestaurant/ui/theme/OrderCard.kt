package com.example.g9_smartrestaurant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import java.util.Locale

// Màu lấy theo MenuScreen của khách để hai phần cùng một tông
internal val StaffOrange = Color(0xFFE65100)
internal val StaffOrangeLight = Color(0xFFFFF3E0)
internal val DangerRed = Color(0xFFC62828)

// Đơn chờ xác nhận quá số phút này sẽ bị cảnh báo
internal const val PENDING_WARN_MINUTES = 3

internal fun orderCode(order: Order) = "#" + order.id.takeLast(4).uppercase()

internal fun formatVnd(amount: Double): String =
    String.format(Locale.US, "%,d", amount.toLong()).replace(',', '.') + " VNĐ"

private data class StatusStyle(val label: String, val bg: Color, val fg: Color)

private fun statusStyle(status: String) = when (status) {
    OrderStatus.PLACED -> StatusStyle("Chờ xác nhận", Color(0xFFFFF4CC), Color(0xFF8A6100))
    OrderStatus.CONFIRMED -> StatusStyle("Đã xác nhận", Color(0xFFE0F2F1), Color(0xFF00695C))
    OrderStatus.PREPARING -> StatusStyle("Đang làm", Color(0xFFE3F2FD), Color(0xFF1565C0))
    OrderStatus.READY -> StatusStyle("Sẵn sàng", Color(0xFFE8F5E9), Color(0xFF2E7D32))
    OrderStatus.SERVED -> StatusStyle("Đã phục vụ", Color(0xFFEEEEEE), Color(0xFF424242))
    OrderStatus.COMPLETED -> StatusStyle("Hoàn tất", Color(0xFFEEEEEE), Color(0xFF424242))
    OrderStatus.CANCELLED -> StatusStyle("Đã hủy", Color(0xFFFFEBEE), DangerRed)
    else -> StatusStyle(status, Color(0xFFEEEEEE), Color(0xFF424242))
}

@Composable
fun StatusChip(status: String) {
    val s = statusStyle(status)
    Surface(shape = RoundedCornerShape(8.dp), color = s.bg) {
        Text(
            text = s.label,
            color = s.fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    now: Long,
    busy: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onServed: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val waitMinutes = ((now - order.createdAt) / 60_000L).coerceAtLeast(0L)
    val overdue = order.status == OrderStatus.PLACED && waitMinutes >= PENDING_WARN_MINUTES
    val itemCount = order.items.sumOf { it.quantity }
    val notes = order.items.filter { it.note.isNotBlank() }
        .joinToString("; ") { "${it.name}: ${it.note}" }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (overdue) BorderStroke(1.dp, DangerRed) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${orderCode(order)} · ${order.tableNumber.ifBlank { "Chưa rõ bàn" }}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(order.status)
            }

            if (order.status == OrderStatus.PLACED) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (overdue) "Đã chờ $waitMinutes phút, cần xử lý ngay" else "Chờ $waitMinutes phút",
                    fontSize = 12.sp,
                    color = if (overdue) DangerRed else Color.Gray,
                    fontWeight = if (overdue) FontWeight.Medium else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.items.joinToString(", ") { "${it.quantity} × ${it.name}" },
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ghi chú: $notes",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$itemCount món · ${formatVnd(order.total)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StaffOrange
            )

            when (order.status) {
                OrderStatus.PLACED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            enabled = !busy,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Từ chối") }
                        Button(
                            onClick = onConfirm,
                            enabled = !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = StaffOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Xác nhận", fontWeight = FontWeight.Bold) }
                    }
                }

                OrderStatus.CONFIRMED, OrderStatus.PREPARING -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (order.eta > 0) "Dự kiến còn ${order.eta} phút" else "Đang tính thời gian",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                OrderStatus.READY -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onServed,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = StaffOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Đã phục vụ", fontWeight = FontWeight.Bold) }
                }

                OrderStatus.SERVED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onComplete,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = StaffOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Hoàn tất", fontWeight = FontWeight.Bold) }
                }

                OrderStatus.CANCELLED -> {
                    if (order.cancelReason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Lý do: ${order.cancelReason}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
