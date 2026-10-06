package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.Category
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.model.Order
import com.example.g9_smartrestaurant.model.OrderStatus
import com.example.g9_smartrestaurant.repository.MenuRepository
import com.example.g9_smartrestaurant.repository.OrderRepository
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.ui.theme.SurfaceGray
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuOrderScreen(
    onNavigateBack: () -> Unit = {},
    onOrderPlacedSuccess: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val menuRepository = remember { MenuRepository() }
    val orderRepository = remember { OrderRepository() }

    // States
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<String?>("all") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    // Cart State
    var showCartSheet by remember { mutableStateOf(false) }
    val cartItems = remember { mutableStateMapOf<String, CartItem>() }

    // Load Categories & Menu
    fun loadMenuData(catId: String? = null) {
        coroutineScope.launch {
            isLoading = true
            val catResult = menuRepository.getCategories()
            catResult.onSuccess { categories = it }

            val filterCatId = if (catId == "all") null else catId
            val itemsResult = menuRepository.getMenuItems(filterCatId)
            itemsResult.onSuccess {
                menuList = it
                isLoading = false
            }.onFailure {
                isLoading = false
                Toast.makeText(context, "Lỗi tải món: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMenuData()
    }

    // Filter list by search query
    val filteredMenuItems = remember(menuList, searchQuery) {
        if (searchQuery.isBlank()) menuList
        else menuList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalItemsCount = cartItems.values.sumOf { it.quantity }
    val totalAmount = cartItems.values.sumOf { it.menuItem.price * it.quantity }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("vi").setRegion("VN").build())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đặt món",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Tìm kiếm")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgLight,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            // Bottom Bar tóm tắt giỏ hàng (Mục 3 trong bản vẽ)
            if (totalItemsCount > 0) {
                Surface(
                    color = Color.White,
                    shadowElevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.clickable { showCartSheet = true }
                        ) {
                            Text(
                                text = "Tổng cộng ($totalItemsCount món)  ▲",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = currencyFormatter.format(totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedAccent
                            )
                        }
                        Button(
                            onClick = {
                                if (cartItems.isEmpty() || isPlacingOrder) return@Button
                                isPlacingOrder = true
                                val newOrder = Order(
                                    tableNumber = "Bàn 01",
                                    status = OrderStatus.PLACED,
                                    total = totalAmount,
                                    items = cartItems.values.map { it.toOrderItem() }
                                )

                                coroutineScope.launch {
                                    val result = orderRepository.placeOrder(newOrder)
                                    isPlacingOrder = false
                                    result.onSuccess { orderId ->
                                        cartItems.clear()
                                        Toast.makeText(context, "Đặt món thành công!", Toast.LENGTH_SHORT).show()
                                        onOrderPlacedSuccess(orderId)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Lỗi gửi đơn: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isPlacingOrder
                        ) {
                            if (isPlacingOrder) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "→ Xác nhận đặt hàng",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = BgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thanh Tìm kiếm (Search Bar) có thể đóng/mở
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm món ăn, đồ uống...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Thanh Danh mục ngang (Category Pill Chips)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút "Tất cả" mặc định
                item {
                    CategoryChip(
                        name = "Tất cả",
                        isSelected = selectedCategoryId == "all",
                        onClick = {
                            selectedCategoryId = "all"
                            loadMenuData("all")
                        }
                    )
                }

                items(categories) { cat ->
                    CategoryChip(
                        name = cat.name,
                        isSelected = selectedCategoryId == cat.id,
                        onClick = {
                            selectedCategoryId = cat.id
                            loadMenuData(cat.id)
                        }
                    )
                }
            }

            // Danh sách món ăn
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (filteredMenuItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Không tìm thấy món ăn phù hợp" else "Chưa có món ăn trong danh mục này",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMenuItems, key = { it.id }) { item ->
                        val qty = cartItems[item.id]?.quantity ?: 0

                        MenuItemCard(
                            item = item,
                            quantity = qty,
                            formattedPrice = currencyFormatter.format(item.price),
                            onIncrease = {
                                val currentItem = cartItems[item.id]
                                if (currentItem != null) {
                                    cartItems[item.id] = currentItem.copy(quantity = currentItem.quantity + 1)
                                } else {
                                    cartItems[item.id] = CartItem(menuItem = item, quantity = 1)
                                }
                            },
                            onDecrease = {
                                val currentItem = cartItems[item.id]
                                if (currentItem != null) {
                                    if (currentItem.quantity > 1) {
                                        cartItems[item.id] = currentItem.copy(quantity = currentItem.quantity - 1)
                                    } else {
                                        cartItems.remove(item.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    //  MODAL BOTTOM SHEET (GHI CHÚ & CHI TIẾT GIỎ HÀNG)
    // =========================================================================
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chi tiết giỏ hàng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { cartItems.clear(); showCartSheet = false }) {
                        Text("Xóa tất cả", color = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems.values.toList(), key = { it.menuItem.id }) { cartItem ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceGray, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.menuItem.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = currencyFormatter.format(cartItem.menuItem.price),
                                        color = RedAccent,
                                        fontSize = 13.sp
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
                                            .background(Color.White)
                                            .clickable {
                                                if (cartItem.quantity > 1) {
                                                    cartItems[cartItem.menuItem.id] = cartItem.copy(quantity = cartItem.quantity - 1)
                                                } else {
                                                    cartItems.remove(cartItem.menuItem.id)
                                                    if (cartItems.isEmpty()) showCartSheet = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Giảm",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Text(text = "${cartItem.quantity}", fontWeight = FontWeight.Bold)

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
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Tăng",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ô ghi chú món ăn (notes)
                            OutlinedTextField(
                                value = cartItem.notes,
                                onValueChange = { newNote ->
                                    cartItems[cartItem.menuItem.id] = cartItem.copy(notes = newNote)
                                },
                                placeholder = { Text("Ghi chú cho bếp (vd: ít cay, không hành...)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nút Đặt món bên trong Sheet
                Button(
                    onClick = {
                        if (cartItems.isEmpty() || isPlacingOrder) return@Button
                        isPlacingOrder = true
                        val newOrder = Order(
                            tableNumber = "Bàn 01",
                            status = OrderStatus.PLACED,
                            total = totalAmount,
                            items = cartItems.values.map { it.toOrderItem() }
                        )

                        coroutineScope.launch {
                            val result = orderRepository.placeOrder(newOrder)
                            isPlacingOrder = false
                            result.onSuccess { orderId ->
                                cartItems.clear()
                                showCartSheet = false
                                Toast.makeText(context, "Đặt món thành công!", Toast.LENGTH_SHORT).show()
                                onOrderPlacedSuccess(orderId)
                            }.onFailure { err ->
                                Toast.makeText(context, "Lỗi gửi đơn: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isPlacingOrder
                ) {
                    Text(
                        text = if (isPlacingOrder) "Đang gửi đơn..." else "Gửi order • ${currencyFormatter.format(totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BluePrimary else SurfaceGray
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    formattedPrice: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh món ăn
            AsyncImage(
                model = item.imageUrl.ifBlank { "https://via.placeholder.com/150" },
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin món: Tên, mô tả, giá
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = RedAccent
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cụm nút số lượng: [-] qty [+] (Khớp hình số 3)
            if (quantity > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Nút Giảm (-)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SurfaceGray)
                            .clickable { onDecrease() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Giảm",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Số lượng hiển thị
                    Text(
                        text = "$quantity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.widthIn(min = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Nút Tăng (+)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BluePrimary)
                            .clickable { onIncrease() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tăng",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Nút Thêm lúc số lượng = 0
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(RedAccent)
                        .clickable { onIncrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}