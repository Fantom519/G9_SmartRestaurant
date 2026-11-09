package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import com.google.firebase.firestore.SetOptions

@Composable
fun CartTabContent(
    cartItems: MutableMap<String, CartItem>,
    currentUserId: String,
    selectedTableNumber: String,
    onOpenTableDialog: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onOrderPlacedSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val formatter = remember { DecimalFormat("#,###đ") }

    val itemList = cartItems.values.toList()
    val totalPrice = itemList.sumOf { it.menuItem.price * it.quantity }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(16.dp)
    ) {
        Text(
            text = "Giỏ hàng của bạn",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // THANH THÔNG TIN VỊ TRÍ BÀN ĐANG ĐƯỢC CHỌN
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { onOpenTableDialog() },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE3F2FD)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Vị trí bàn đặt ăn:", fontSize = 11.sp, color = Color.Gray)
                        Text(selectedTableNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }

                OutlinedButton(
                    onClick = onOpenTableDialog,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đổi / Quét QR", fontSize = 11.sp)
                }
            }
        }

        if (itemList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Giỏ hàng đang trống", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Hãy chọn thêm món ăn từ thực đơn nhé!", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Khám phá thực đơn", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemList) { cartItem ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cartItem.menuItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        formatter.format(cartItem.menuItem.price),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedAccent
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEEEEEE))
                                            .clickable {
                                                if (cartItem.quantity > 1) {
                                                    cartItems[cartItem.menuItem.id] = cartItem.copy(quantity = cartItem.quantity - 1)
                                                } else {
                                                    cartItems.remove(cartItem.menuItem.id)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    }

                                    Text(
                                        text = "${cartItem.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.widthIn(min = 16.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(BluePrimary)
                                            .clickable {
                                                cartItems[cartItem.menuItem.id] = cartItem.copy(quantity = cartItem.quantity + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Tăng", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cartItem.notes,
                                onValueChange = { newNote ->
                                    cartItems[cartItem.menuItem.id] = cartItem.copy(notes = newNote)
                                },
                                placeholder = { Text("Ghi chú món ăn (ví dụ: Không cay, ...)", fontSize = 12.sp, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFAF9F9),
                                    unfocusedContainerColor = Color(0xFFFAF9F9)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng cộng (${itemList.sumOf { it.quantity }} món):", fontSize = 14.sp, color = Color.Gray)
                        Text(formatter.format(totalPrice), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RedAccent)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isSubmitting = true
                            val newOrder = Order(
                                customerId = currentUserId,
                                tableNumber = selectedTableNumber, // TRUYỀN SELECTED TABLE ĐỘNG
                                items = itemList.map { it.toOrderItem() },
                                total = totalPrice,
                                status = OrderStatus.PLACED,
                                createdAt = System.currentTimeMillis()
                            )

                            db.collection("orders").add(newOrder)
                                .addOnSuccessListener { docRef ->
                                    // CẬP NHẬT HOẶC TẠO MỚI TRẠNG THÁI BÀN 'OCCUPIED'
                                    val tableIndex = selectedTableNumber.filter { it.isDigit() }.toIntOrNull() ?: 1
                                    val tableDocId = "table_%02d".format(tableIndex)

                                    val tableData = mapOf(
                                        "tableNumber" to selectedTableNumber,
                                        "status" to "OCCUPIED"
                                    )
                                    db.collection("tables").document(tableDocId).set(tableData, SetOptions.merge())

                                    isSubmitting = false
                                    Toast.makeText(context, "🎉 Đặt món thành công tại $selectedTableNumber!", Toast.LENGTH_LONG).show()
                                    onOrderPlacedSuccess(docRef.id)
                                }
                                .addOnFailureListener { e ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Lỗi đặt món: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Xác nhận đặt hàng →", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}