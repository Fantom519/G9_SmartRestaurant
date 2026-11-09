package com.example.g9_smartrestaurant.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun TableSelectionDialog(
    currentTable: String,
    onTableSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val tablesList = remember { (1..20).map { "Bàn %02d".format(it) } }

    var isScanning by remember { mutableStateOf(false) }

    // 1. Danh sách bàn có đơn hàng đang hoạt động từ collection 'orders'
    var activeOrderTables by remember { mutableStateOf<Set<String>>(emptySet()) }

    // 2. Trạng thái bàn cài đặt thủ công từ collection 'tables' (Định kiểu chuẩn emptyMap<String, String>())
    var dbTableStatusMap by remember { mutableStateOf(emptyMap<String, String>()) }

    // LẮNG NGHE ĐƠN HÀNG ĐANG HOẠT ĐỘNG (PLACED, CONFIRMED, PREPARING, READY)
    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereIn("status", listOf("PLACED", "CONFIRMED", "PREPARING", "READY"))
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val busyTables = snapshot.documents.mapNotNull { doc ->
                        doc.getString("tableNumber")
                    }.toSet()
                    activeOrderTables = busyTables
                }
            }
    }

    // LẮNG NGHE COLLECTION 'tables'
    LaunchedEffect(Unit) {
        db.collection("tables").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val map = mutableMapOf<String, String>()
                snapshot.documents.forEach { doc ->
                    val tableName = doc.getString("tableNumber") ?: ""
                    val status = doc.getString("status") ?: "EMPTY"
                    if (tableName.isNotBlank()) {
                        map[tableName] = status
                    }
                }
                dbTableStatusMap = map
            }
        }
    }

    // Tự động nhảy sang bàn trống nếu bàn hiện tại đang có khách hoặc đã đặt
    LaunchedEffect(activeOrderTables, dbTableStatusMap) {
        val isCurrentOccupied = activeOrderTables.contains(currentTable) || dbTableStatusMap[currentTable] == "OCCUPIED"
        val isCurrentReserved = dbTableStatusMap[currentTable] == "RESERVED"

        if (isCurrentOccupied || isCurrentReserved) {
            val firstAvailable = tablesList.firstOrNull { table ->
                !activeOrderTables.contains(table) &&
                        dbTableStatusMap[table] != "OCCUPIED" &&
                        dbTableStatusMap[table] != "RESERVED"
            } ?: "Bàn 01"

            if (firstAvailable != currentTable) {
                onTableSelected(firstAvailable)
            }
        }
    }

    // Mô phỏng đếm ngược Quét QR
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(1500)
            val availableTables = tablesList.filter { table ->
                !activeOrderTables.contains(table) &&
                        dbTableStatusMap[table] != "OCCUPIED" &&
                        dbTableStatusMap[table] != "RESERVED"
            }
            val selected = if (availableTables.isNotEmpty()) availableTables.random() else "Bàn 01"
            onTableSelected(selected)
            isScanning = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BluePrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Xác nhận vị trí bàn", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                    Text("Chọn bàn trống hoặc quét mã QR tại bàn", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .border(2.dp, BluePrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = RedAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Đang mở camera quét mã QR...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Vui lòng hướng camera vào mã QR dán trên bàn", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { isScanning = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quét mã QR trên bàn (Mô phỏng)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem(color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32), label = "Trống")
                        LegendItem(color = BluePrimary, textColor = Color.White, label = "Đang chọn")
                        LegendItem(color = Color(0xFFFFF3E0), textColor = Color(0xFFE65100), label = "Đã đặt")
                        LegendItem(color = Color(0xFFFFEBEE), textColor = RedAccent, label = "Có khách")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(350.dp)
                    ) {
                        items(tablesList) { table ->
                            val isSelected = table == currentTable
                            val hasActiveOrder = activeOrderTables.contains(table)
                            val dbStatus = dbTableStatusMap[table] ?: "EMPTY"

                            val isOccupied = hasActiveOrder || dbStatus == "OCCUPIED"
                            val isReserved = !isOccupied && dbStatus == "RESERVED"

                            val isClickable = !isOccupied && !isReserved

                            val bgColor = when {
                                isSelected -> BluePrimary
                                isOccupied -> Color(0xFFFFEBEE)
                                isReserved -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF1F8E9)
                            }

                            val textColor = when {
                                isSelected -> Color.White
                                isOccupied -> RedAccent
                                isReserved -> Color(0xFFE65100)
                                else -> Color(0xFF33691E)
                            }

                            val statusText = when {
                                isSelected -> "Đang chọn"
                                isOccupied -> "Có khách"
                                isReserved -> "Đã đặt"
                                else -> "Trống"
                            }

                            Card(
                                modifier = Modifier
                                    .height(78.dp)
                                    .clickable(enabled = isClickable) {
                                        onTableSelected(table)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) BluePrimary else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.TableRestaurant,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = table,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = statusText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isScanning) {
                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun LegendItem(color: Color, textColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, textColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}