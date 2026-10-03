package com.example.g9_smartrestaurant.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.repository.MenuRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(menuRepository: MenuRepository = MenuRepository()) {
    val context = LocalContext.current
    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    // Quản lý danh mục đang chọn và thanh tìm kiếm
    val categories = listOf("Tất cả", "mon_chinh", "do_uong", "trang_mieng")
    val categoryLabels = mapOf(
        "Tất cả" to "Tất cả",
        "mon_chinh" to "Món chính",
        "do_uong" to "Đồ uống",
        "trang_mieng" to "Tráng miệng"
    )
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var showCartSheet by remember { mutableStateOf(false) }

    val totalAmount = cartItems.sumOf { it.menuItem.price * it.quantity }
    val totalCount = cartItems.sumOf { it.quantity }

    // Lọc danh sách món theo danh mục và từ khóa tìm kiếm
    val filteredMenu = menuList.filter { item ->
        val matchesCategory = (selectedCategory == "Tất cả" || item.categoryId.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LaunchedEffect(Unit) {
        menuRepository.getMenuItems(
            onSuccess = { items ->
                menuList = items
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Smart Restaurant 🍽️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Xin chào! Bạn đang ngồi tại Bàn 01",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Ô tìm kiếm món ăn
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm món ăn, đồ uống...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Thanh cuộn chọn danh mục (Category Chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = (selectedCategory == cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(categoryLabels[cat] ?: cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Đã chọn $totalCount món", fontSize = 13.sp, color = Color.Gray)
                            Text(
                                "${totalAmount.toInt()} VNĐ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Xem giỏ hàng", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = Color(0xFFE65100))
                filteredMenu.isEmpty() -> Text("Không tìm thấy món ăn phù hợp", color = Color.Gray)
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredMenu) { item ->
                            FoodItemCard(
                                item = item,
                                onAddToCart = {
                                    val existing = cartItems.find { it.menuItem.id == item.id }
                                    if (existing != null) {
                                        val index = cartItems.indexOf(existing)
                                        cartItems[index] = existing.copy(quantity = existing.quantity + 1)
                                    } else {
                                        cartItems.add(CartItem(menuItem = item, quantity = 1))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheet chi tiết giỏ hàng
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Chi tiết đơn gọi món (Bàn 01)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(cartItems) { cartItem ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cartItem.menuItem.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(
                                                onClick = {
                                                    val index = cartItems.indexOf(cartItem)
                                                    if (cartItem.quantity > 1) {
                                                        cartItems[index] = cartItem.copy(quantity = cartItem.quantity - 1)
                                                    } else {
                                                        cartItems.removeAt(index)
                                                        if (cartItems.isEmpty()) showCartSheet = false
                                                    }
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("-")
                                            }
                                            Text(
                                                text = "${cartItem.quantity}",
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    val index = cartItems.indexOf(cartItem)
                                                    cartItems[index] = cartItem.copy(quantity = cartItem.quantity + 1)
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("+")
                                            }

                                            TextButton(
                                                onClick = {
                                                    cartItems.remove(cartItem)
                                                    if (cartItems.isEmpty()) showCartSheet = false
                                                }
                                            ) {
                                                Text("Xóa", color = Color.Red, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = cartItem.note,
                                        onValueChange = { newNote ->
                                            val index = cartItems.indexOf(cartItem)
                                            cartItems[index] = cartItem.copy(note = newNote)
                                        },
                                        placeholder = { Text("Ghi chú cho bếp (vd: không cay...)", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${totalAmount.toInt()} VNĐ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFE65100)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isPlacingOrder = true
                            menuRepository.placeOrder(
                                tableNumber = "Bàn 01",
                                items = cartItems.toList(),
                                totalAmount = totalAmount,
                                onSuccess = {
                                    isPlacingOrder = false
                                    cartItems.clear()
                                    showCartSheet = false
                                    Toast.makeText(context, "Đặt món thành công!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    isPlacingOrder = false
                                    Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isPlacingOrder && cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (isPlacingOrder) "Đang gửi đơn..." else "Xác nhận gửi order", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(
    item: MenuItem,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh món ăn load qua Coil
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chuẩn bị: ~${item.prepTime} phút",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${item.price.toInt()} VNĐ",
                    color = Color(0xFFE65100),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("+ Thêm", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}