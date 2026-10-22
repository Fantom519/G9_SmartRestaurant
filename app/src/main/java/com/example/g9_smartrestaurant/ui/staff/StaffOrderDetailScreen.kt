
package com.example.g9_smartrestaurant.ui.staff

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderItem
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.OrderRepository
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.SurfaceGray
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { OrderRepository() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isConfirming by remember {
        mutableStateOf(false)
    }

    var isCompleting by remember {
        mutableStateOf(false)
    }

    var order by remember(orderId) {
        mutableStateOf<Order?>(null)
    }

    var isLoading by remember(orderId) {
        mutableStateOf(true)
    }

    var error by remember(orderId) {
        mutableStateOf<String?>(null)
    }

    // LẮNG NGHE FIRESTORE REALTIME
    LaunchedEffect(orderId) {
        repository.observeOrderById(orderId)
            .catch { exception ->
                error = exception.message
                    ?: "Không thể tải đơn hàng"
                isLoading = false
            }
            .collect { result ->
                order = result
                isLoading = false
                error = null
            }
    }

    Scaffold(
        containerColor = BgLight,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết đơn hàng",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateBack
                    ) {
                        Text(
                            text = "←",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary
                )
            )
        }
    ) { paddingValues ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = BluePrimary
                    )
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Đã xảy ra lỗi",
                        color = RedAccent
                    )
                }
            }

            order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không tìm thấy đơn hàng")
                }
            }

            else -> {
                val currentOrder = order!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OrderDetailContent(
                            order = currentOrder
                        )
                    }

                    // NÚT XÁC NHẬN ĐƠN
                    if (currentOrder.status == OrderStatus.PLACED) {
                        StaffActionButton(
                            text = "Xác nhận đơn hàng",
                            isLoading = isConfirming,
                            onClick = {
                                if (!isConfirming) {
                                    isConfirming = true

                                    coroutineScope.launch {
                                        val result =
                                            repository.confirmOrder(
                                                currentOrder.id
                                            )

                                        isConfirming = false

                                        result.onSuccess {
                                            Toast.makeText(
                                                context,
                                                "Xác nhận đơn thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure { exception ->
                                            Toast.makeText(
                                                context,
                                                "Lỗi: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // NÚT HOÀN TẤT ĐƠN
                    if (currentOrder.status == OrderStatus.READY) {
                        StaffActionButton(
                            text = "Hoàn tất đơn hàng",
                            isLoading = isCompleting,
                            onClick = {
                                if (!isCompleting) {
                                    isCompleting = true

                                    coroutineScope.launch {
                                        val result =
                                            repository.completeOrder(
                                                currentOrder.id
                                            )

                                        isCompleting = false

                                        result.onSuccess {
                                            Toast.makeText(
                                                context,
                                                "Đơn hàng đã hoàn tất",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            onNavigateBack()

                                        }.onFailure { exception ->
                                            Toast.makeText(
                                                context,
                                                "Lỗi: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffActionButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BluePrimary,
            contentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: Order
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )
    }

    val timeFormatter = remember {
        SimpleDateFormat(
            "HH:mm dd/MM/yyyy",
            Locale.getDefault()
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        // THÔNG TIN ĐƠN HÀNG
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatStaffTable(
                                order.tableNumber
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = detailStatusLabel(order.status),
                            color = detailStatusColor(order.status),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Đơn #${order.id.take(8)}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider(
                        color = SurfaceGray
                    )

                    Text(
                        text = "Thời gian: ${
                            timeFormatter.format(
                                Date(order.createdAt)
                            )
                        }"
                    )

                    Text(
                        text = "Số phần: ${
                            order.items.sumOf { it.quantity }
                        }"
                    )
                }
            }
        }

        // TIÊU ĐỀ DANH SÁCH MÓN
        item {
            Text(
                text = "Danh sách món ăn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // CÁC MÓN ĂN
        items(order.items) { item ->
            StaffOrderItemCard(
                item = item,
                currencyFormatter = currencyFormatter
            )
        }

        // TỔNG THANH TOÁN
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng thanh toán",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currencyFormatter.format(order.total),
                        color = RedAccent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffOrderItemCard(
    item: OrderItem,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "× ${item.quantity}",
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Đơn giá: ${
                    currencyFormatter.format(item.price)
                }",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            if (item.notes.isNotBlank()) {
                Text(
                    text = "Ghi chú: ${item.notes}",
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(
                color = SurfaceGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Thành tiền",
                    color = Color.Gray
                )

                Text(
                    text = currencyFormatter.format(
                        item.price * item.quantity
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatStaffTable(value: String): String {
    val number = value.trim().replace(
        Regex(
            "^(Bàn|Ban)\\s*:?\\s*",
            RegexOption.IGNORE_CASE
        ),
        ""
    )

    return if (number.isBlank()) {
        "Chưa xác định bàn"
    } else {
        "Bàn $number"
    }
}

private fun detailStatusLabel(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PLACED -> "Chờ xác nhận"
        OrderStatus.CONFIRMED -> "Đã xác nhận"
        OrderStatus.PREPARING -> "Đang chế biến"
        OrderStatus.READY -> "Sẵn sàng"
        OrderStatus.COMPLETED -> "Hoàn tất"
        OrderStatus.CANCELLED -> "Đã hủy"
    }
}

private fun detailStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PLACED -> RedAccent
        OrderStatus.CONFIRMED -> BluePrimary
        OrderStatus.PREPARING -> Color(0xFFEF6C00)
        OrderStatus.READY -> Color(0xFF2E7D32)
        OrderStatus.COMPLETED -> Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> RedAccent
    }
}
