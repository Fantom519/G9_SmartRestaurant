package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuOrderScreen(
    cartItems: SnapshotStateMap<String, CartItem>,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val formatter = remember { DecimalFormat("#,###đ") }

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val categories = listOf("Tất cả", "Phở", "Cơm", "Bún", "Món xào", "Đồ uống")

    // Lắng nghe thực đơn từ Firestore
    LaunchedEffect(Unit) {
        db.collection("menu_items").addSnapshotListener { snapshot, error ->
            isLoading = false
            if (error == null && snapshot != null) {
                menuList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    val filteredMenu = remember(menuList, searchQuery, selectedCategory) {
        menuList.filter { item ->
            val cat = item.categoryId.trim()
            val selCat = selectedCategory.trim()

            val matchesCategory = selCat == "Tất cả" ||
                    cat.equals(selCat, ignoreCase = true) ||
                    cat.contains(selCat, ignoreCase = true) ||
                    selCat.contains(cat, ignoreCase = true) ||
                    item.name.contains(selCat, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery.trim(), ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    val totalCartCount = cartItems.values.sumOf { it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thực đơn Bếp Việt", fontWeight = FontWeight.Bold, color = BluePrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = BluePrimary)
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = BluePrimary)
                        }
                        if (totalCartCount > 0) {
                            Badge(
                                containerColor = RedAccent,
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text("$totalCartCount")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Ô tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm món ăn...", fontSize = 14.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips lọc danh mục
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            } else if (filteredMenu.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy món ăn phù hợp", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredMenu) { item ->
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
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = item.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = BluePrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.categoryId.ifBlank { "Món ăn" }, fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("⏱️ ${item.prepTime} phút", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatter.format(item.price), fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 14.sp)
                                }

                                Button(
                                    onClick = {
                                        val currentItem = cartItems[item.id]
                                        if (currentItem != null) {
                                            cartItems[item.id] = currentItem.copy(quantity = currentItem.quantity + 1)
                                        } else {
                                            cartItems[item.id] = CartItem(menuItem = item, quantity = 1)
                                        }
                                        Toast.makeText(context, "Đã thêm ${item.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Thêm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}