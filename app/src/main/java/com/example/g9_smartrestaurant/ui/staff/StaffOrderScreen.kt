
package com.example.g9_smartrestaurant.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.SurfaceGray
import com.example.g9_smartrestaurant.viewmodel.StaffViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffOrderScreen(
    onOrderClick: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    staffViewModel: StaffViewModel = viewModel()
) {
    val orders by staffViewModel.orders.collectAsStateWithLifecycle()
    val isLoading by staffViewModel.isLoading.collectAsStateWithLifecycle()
    val error by staffViewModel.error.collectAsStateWithLifecycle()

    var selectedStatus by remember {
        mutableStateOf<OrderStatus?>(null)
    }

    val filteredOrders = remember(orders, selectedStatus) {
        if (selectedStatus == null) {
            orders
        } else {
            orders.filter { it.status == selectedStatus }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {

        // HEADER THƯƠNG HIỆU
        StaffHeader()

        // THỐNG KÊ VÀ DANH SÁCH
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // HÀNG THỐNG KÊ THỨ NHẤT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                StatusCounter(
                    title = "Chờ xác nhận",
                    count = orders.count {
                        it.status == OrderStatus.PLACED
                    },
                    modifier = Modifier.weight(1f)
                )

                StatusCounter(
                    title = "Đã xác nhận",
                    count = orders.count {
                        it.status == OrderStatus.CONFIRMED
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HÀNG THỐNG KÊ THỨ HAI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                StatusCounter(
                    title = "Đang chế biến",
                    count = orders.count {
                        it.status == OrderStatus.PREPARING
                    },
                    modifier = Modifier.weight(1f)
                )

                StatusCounter(
                    title = "Sẵn sàng",
                    count = orders.count {
                        it.status == OrderStatus.READY
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TIÊU ĐỀ DANH SÁCH VÀ LỊCH SỬ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Danh sách đơn hàng",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                TextButton(
                    onClick = onNavigateToHistory
                ) {
                    Text(
                        text = "Lịch sử →",
                        color = BluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // BỘ LỌC CUỘN NGANG
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                StaffFilterChip(
                    text = "Tất cả",
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null }
                )

                StaffFilterChip(
                    text = "Chờ xác nhận",
                    selected = selectedStatus == OrderStatus.PLACED,
                    onClick = {
                        selectedStatus = OrderStatus.PLACED
                    }
                )

                StaffFilterChip(
                    text = "Đã xác nhận",
                    selected = selectedStatus == OrderStatus.CONFIRMED,
                    onClick = {
                        selectedStatus = OrderStatus.CONFIRMED
                    }
                )

                StaffFilterChip(
                    text = "Đang chế biến",
                    selected = selectedStatus == OrderStatus.PREPARING,
                    onClick = {
                        selectedStatus = OrderStatus.PREPARING
                    }
                )

                StaffFilterChip(
                    text = "Sẵn sàng",
                    selected = selectedStatus == OrderStatus.READY,
                    onClick = {
                        selectedStatus = OrderStatus.READY
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DANH SÁCH ĐƠN HÀNG REALTIME
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BluePrimary
                        )
                    }
                }

                error != null -> {
                    Text(
                        text = error ?: "Đã xảy ra lỗi",
                        color = RedAccent
                    )
                }

                filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có đơn hàng trong mục này",
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                        contentPadding =
                            PaddingValues(bottom = 20.dp)
                    ) {
                        items(
                            items = filteredOrders,
                            key = { it.id }
                        ) { order ->
                            StaffOrderCard(
                                order = order,
                                onClick = {
                                    onOrderClick(order.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BluePrimary)
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "G9 SMART RESTAURANT",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFDDE8FA),
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Quản lý đơn hàng",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Nhân viên phục vụ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE8EEF5)
        )
    }
}

@Composable
private fun StatusCounter(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555)
            )
        }
    }
}

@Composable
private fun StaffFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = Color(0xFF555555),
            selectedContainerColor = BluePrimary,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = SurfaceGray,
            selectedBorderColor = BluePrimary
        )
    )
}

@Composable
private fun StaffOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )
    }

    val timeFormatter = remember {
        SimpleDateFormat(
            "HH:mm dd/MM",
            Locale.getDefault()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            // SỐ BÀN VÀ TRẠNG THÁI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTableNumber(order.tableNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                Text(
                    text = statusLabel(order.status),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor(order.status)
                )
            }

            // MÃ ĐƠN VÀ THỜI GIAN
            Text(
                text = "Đơn #${order.id.take(8)} · ${
                    timeFormatter.format(Date(order.createdAt))
                }",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777777)
            )

            Text(
                text = "${order.items.sumOf { it.quantity }} phần",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF555555)
            )

            HorizontalDivider(
                color = SurfaceGray
            )

            // TỔNG TIỀN VÀ XEM CHI TIẾT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = currencyFormatter.format(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )

                Text(
                    text = "Xem chi tiết →",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
            }
        }
    }
}

private fun statusLabel(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PLACED -> "Chờ xác nhận"
        OrderStatus.CONFIRMED -> "Đã xác nhận"
        OrderStatus.PREPARING -> "Đang chế biến"
        OrderStatus.READY -> "Sẵn sàng"
        OrderStatus.COMPLETED -> "Hoàn tất"
        OrderStatus.CANCELLED -> "Đã hủy"
    }
}

private fun statusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PLACED -> RedAccent
        OrderStatus.CONFIRMED -> BluePrimary
        OrderStatus.PREPARING -> Color(0xFFEF6C00)
        OrderStatus.READY -> Color(0xFF2E7D32)
        OrderStatus.COMPLETED -> Color(0xFF2E7D32)
        OrderStatus.CANCELLED -> RedAccent
    }
}

private fun formatTableNumber(value: String): String {
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
