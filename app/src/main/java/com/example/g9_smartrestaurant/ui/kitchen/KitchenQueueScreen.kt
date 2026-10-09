package com.example.g9_smartrestaurant.ui.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class KitchenOrderItem(val name: String, val quantity: Int)

data class KitchenOrder(
    val orderId: String,
    val time: String,
    val items: List<KitchenOrderItem>,
    val status: OrderStatus
)

enum class OrderStatus {
    NEW, COOKING, COMPLETED
}

@Composable
fun KitchenQueueScreen() {
    val sampleOrders = listOf(
        KitchenOrder("#DH0012", "10:25", listOf(KitchenOrderItem("Phở bò", 2), KitchenOrderItem("Cơm tấm", 1)), OrderStatus.NEW),
        KitchenOrder("#DH0013", "10:27", listOf(KitchenOrderItem("Bún chả", 1)), OrderStatus.NEW),
        KitchenOrder("#DH0008", "10:20", listOf(KitchenOrderItem("Cơm tấm", 3), KitchenOrderItem("Bún chả", 1)), OrderStatus.COOKING),
        KitchenOrder("#DH0007", "10:12", listOf(KitchenOrderItem("Bún chả", 2)), OrderStatus.COMPLETED)
    )

    val newOrders = sampleOrders.filter { it.status == OrderStatus.NEW }
    val cookingOrders = sampleOrders.filter { it.status == OrderStatus.COOKING }
    val completedOrders = sampleOrders.filter { it.status == OrderStatus.COMPLETED }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA)) // Màu nền xám nhạt
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KitchenColumn(
            title = "ĐƠN MỚI",
            count = newOrders.size,
            headerColor = Color(0xFFE53935), // Màu đỏ
            orders = newOrders,
            buttonText = "Nhận đơn",
            modifier = Modifier.weight(1f)
        )

        KitchenColumn(
            title = "ĐANG NẤU",
            count = cookingOrders.size,
            headerColor = Color(0xFF1E88E5), // Màu xanh dương
            orders = cookingOrders,
            buttonText = "Đang nấu",
            modifier = Modifier.weight(1f)
        )

        KitchenColumn(
            title = "HOÀN THÀNH",
            count = completedOrders.size,
            headerColor = Color(0xFF43A047), // Màu xanh lá
            orders = completedOrders,
            buttonText = "Hoàn thành",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun KitchenColumn(
    title: String,
    count: Int,
    headerColor: Color,
    orders: List<KitchenOrder>,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = count.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                OrderCard(order = order, color = headerColor, buttonText = buttonText)
            }
        }
    }
}

@Composable
fun OrderCard(order: KitchenOrder, color: Color, buttonText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 16.sp
                )
                Text(
                    text = order.time,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            order.items.forEach { item ->
                Text(
                    text = "${item.name} x${item.quantity}",
                    color = Color(0xFF475569),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Xử lý sự kiện cập nhật trạng thái lên Firebase */ },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240", name = "Tablet Landscape")
@Composable
fun KitchenQueuePreview() {
    KitchenQueueScreen()
}