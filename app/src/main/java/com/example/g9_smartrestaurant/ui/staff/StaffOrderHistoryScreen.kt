
package com.example.g9_smartrestaurant.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.SurfaceGray
import com.example.g9_smartrestaurant.viewmodel.StaffOrderHistoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    historyViewModel: StaffOrderHistoryViewModel = viewModel()
) {
    val orders by historyViewModel.orders.collectAsStateWithLifecycle()
    val isLoading by historyViewModel.isLoading.collectAsStateWithLifecycle()
    val error by historyViewModel.error.collectAsStateWithLifecycle()

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

    Scaffold(
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sử đơn hàng",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // THỐNG KÊ LỊCH SỬ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryCounter(
                    title = "Đã hoàn tất",
                    count = orders.count {
                        it.status == OrderStatus.COMPLETED
                    },
                    numberColor = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                HistoryCounter(
                    title = "Đã hủy",
                    count = orders.count {
                        it.status == OrderStatus.CANCELLED
                    },
                    numberColor = RedAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Danh sách lịch sử",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BỘ LỌC TRẠNG THÁI
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilterChip(
                    text = "Tất cả",
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null }
                )

                HistoryFilterChip(
                    text = "Hoàn tất",
                    selected = selectedStatus == OrderStatus.COMPLETED,
                    onClick = {
                        selectedStatus = OrderStatus.COMPLETED
                    }
                )

                HistoryFilterChip(
                    text = "Đã hủy",
                    selected = selectedStatus == OrderStatus.CANCELLED,
                    onClick = {
                        selectedStatus = OrderStatus.CANCELLED
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            text = "Chưa có đơn hàng trong mục này",
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
                            HistoryOrderCard(
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
private fun HistoryCounter(
    title: String,
    count: Int,
    numberColor: Color,
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = numberColor
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
private fun HistoryFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
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
private fun HistoryOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )
    }

    val dateFormatter = remember {
        SimpleDateFormat(
            "HH:mm dd/MM/yyyy",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatHistoryTable(order.tableNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                Text(
                    text = if (
                        order.status == OrderStatus.COMPLETED
                    ) "Hoàn tất" else "Đã hủy",
                    color = if (
                        order.status == OrderStatus.COMPLETED
                    ) Color(0xFF2E7D32) else RedAccent,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Đơn #${order.id.take(8)} · ${
                    dateFormatter.format(Date(order.createdAt))
                }",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF777777)
            )

            Text(
                text = "${order.items.sumOf { it.quantity }} phần",
                color = Color(0xFF555555)
            )

            HorizontalDivider(color = SurfaceGray)

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
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatHistoryTable(value: String): String {
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
