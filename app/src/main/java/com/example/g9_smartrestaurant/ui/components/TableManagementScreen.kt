package com.example.g9_smartrestaurant.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableRestaurant
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
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.firestore.FirebaseFirestore

enum class TableStatus {
    EMPTY,      // Bàn trống (Màu trắng)
    RESERVED,   // Đã đặt trước (Màu cam)
    OCCUPIED    // Đang có khách (Màu xanh dương)
}

data class TableItem(
    val id: String,
    val name: String,
    val status: TableStatus,
    val activeOrder: Order? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableManagementScreen(
    onOrderClick: (Order) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var activeOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var tableStatusMap by remember { mutableStateOf(emptyMap<String, String>()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTableForAction by remember { mutableStateOf<TableItem?>(null) }

    // 1. LẮNG NGHE ĐƠN HÀNG DÀNH CHO BÀN CÓ KHÁCH (OCCUPIED)
    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereIn("status", listOf("PLACED", "CONFIRMED", "PREPARING", "READY"))
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    activeOrders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // 2. LẮNG NGHE COLLECTION 'tables' TRÊN FIRESTORE (NẾU CHƯA CÓ SẼ TỰ ĐỘNG KHỞI TẠO 20 BÀN)
    LaunchedEffect(Unit) {
        db.collection("tables").addSnapshotListener { snapshot, error ->
            isLoading = false
            if (error == null && snapshot != null) {
                if (snapshot.isEmpty) {
                    // Tự động khởi tạo 20 bàn trống lên Firestore lần đầu chạy
                    (1..20).forEach { i ->
                        val tableName = "Bàn %02d".format(i)
                        val docId = "table_%02d".format(i)
                        db.collection("tables").document(docId).set(
                            mapOf("tableNumber" to tableName, "status" to "EMPTY")
                        )
                    }
                } else {
                    val map = mutableMapOf<String, String>()
                    snapshot.documents.forEach { doc ->
                        val tableName = doc.getString("tableNumber") ?: ""
                        val status = doc.getString("status") ?: "EMPTY"
                        if (tableName.isNotBlank()) {
                            map[tableName] = status
                        }
                    }
                    tableStatusMap = map
                }
            }
        }
    }

    // 3. TÍNH TOÁN DANH SÁCH 20 BÀN THỜI GIAN THỰC (XÓA SẠCH HARDCODE)
    val tablesList = remember(activeOrders, tableStatusMap) {
        (1..20).map { i ->
            val tableName = "Bàn %02d".format(i)
            val docId = "table_%02d".format(i)
            val matchingOrder = activeOrders.find { it.tableNumber.equals(tableName, ignoreCase = true) }
            val dbStatusStr = tableStatusMap[tableName] ?: "EMPTY"

            val status = when {
                matchingOrder != null -> TableStatus.OCCUPIED
                dbStatusStr == "RESERVED" -> TableStatus.RESERVED
                else -> TableStatus.EMPTY
            }
            TableItem(id = docId, name = tableName, status = status, activeOrder = matchingOrder)
        }
    }

    val emptyCount = tablesList.count { it.status == TableStatus.EMPTY }
    val reservedCount = tablesList.count { it.status == TableStatus.RESERVED }
    val occupiedCount = tablesList.count { it.status == TableStatus.OCCUPIED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Sơ đồ quản lý bàn nhà hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
        Text("Theo dõi trạng thái phục vụ thời gian thực", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        // THANH THỐNG KÊ TỔNG QUAN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChipSummary(modifier = Modifier.weight(1f), title = "Trống", count = emptyCount, color = Color.White, textColor = Color.Black)
            StatusChipSummary(modifier = Modifier.weight(1f), title = "Đã đặt", count = reservedCount, color = Color(0xFFFF9800), textColor = Color.White)
            StatusChipSummary(modifier = Modifier.weight(1f), title = "Có khách", count = occupiedCount, color = Color(0xFF2196F3), textColor = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tablesList) { table ->
                    val bgColor = when (table.status) {
                        TableStatus.EMPTY -> Color.White
                        TableStatus.RESERVED -> Color(0xFFFFF3E0)
                        TableStatus.OCCUPIED -> Color(0xFF2196F3)
                    }

                    val textColor = when (table.status) {
                        TableStatus.EMPTY -> Color.Black
                        TableStatus.RESERVED -> Color(0xFFE65100)
                        TableStatus.OCCUPIED -> Color.White
                    }

                    Card(
                        modifier = Modifier
                            .height(95.dp)
                            .clickable {
                                if (table.status == TableStatus.OCCUPIED && table.activeOrder != null) {
                                    onOrderClick(table.activeOrder)
                                } else {
                                    selectedTableForAction = table
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = if (table.status == TableStatus.EMPTY) 1.dp else 0.dp,
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.TableRestaurant,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(table.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)

                            val statusLabel = when (table.status) {
                                TableStatus.EMPTY -> "Trống"
                                TableStatus.RESERVED -> "Đã đặt"
                                TableStatus.OCCUPIED -> "#${table.activeOrder?.id?.takeLast(4)?.uppercase()}"
                            }
                            Text(statusLabel, fontSize = 10.sp, color = textColor.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    // DIALOG THAO TÁC CỦA QUẢN LÝ (ĐẶT BÀN TRƯỚC / GIẢI PHÓNG BÀN)
    selectedTableForAction?.let { table ->
        AlertDialog(
            onDismissRequest = { selectedTableForAction = null },
            title = { Text("Thao tác quản lý ${table.name}", fontWeight = FontWeight.Bold, color = BluePrimary) },
            text = { Text("Chọn trạng thái để cập nhật bàn này trên toàn hệ thống:") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (table.status == TableStatus.EMPTY) {
                        Button(
                            onClick = {
                                db.collection("tables").document(table.id).update("status", "RESERVED")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Đã đổi ${table.name} sang ĐÃ ĐẶT", Toast.LENGTH_SHORT).show()
                                        selectedTableForAction = null
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text("Đánh dấu ĐÃ ĐẶT", color = Color.White)
                        }
                    } else if (table.status == TableStatus.RESERVED) {
                        Button(
                            onClick = {
                                db.collection("tables").document(table.id).update("status", "EMPTY")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Đã chuyển ${table.name} về BÀN TRỐNG", Toast.LENGTH_SHORT).show()
                                        selectedTableForAction = null
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Chuyển về BÀN TRỐNG", color = Color.White)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTableForAction = null }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun StatusChipSummary(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    color: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = textColor.copy(alpha = 0.8f))
            Text("$count bàn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}