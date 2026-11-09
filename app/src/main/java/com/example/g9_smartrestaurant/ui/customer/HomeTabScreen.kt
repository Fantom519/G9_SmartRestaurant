package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.g9_smartrestaurant.model.MenuItem
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat

@Composable
fun HomeTabContent(
    menuList: List<MenuItem>,
    isLoading: Boolean,
    selectedTableNumber: String,       // THÊM: Số bàn đang chọn động
    onOpenTableDialog: () -> Unit,     // THÊM: Callback mở Dialog chọn / Quét QR bàn
    onNavigateToMenu: () -> Unit,
    showAvatarMenu: Boolean,
    onToggleAvatarMenu: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { DecimalFormat("#,###đ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // HEADER TRANG CHỦ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BluePrimary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bếp Việt", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // KHỐI HIỂN THỊ SỐ BÀN ĐỘNG & NÚT ĐỔI / QUÉT QR
                Surface(
                    modifier = Modifier.clickable { onOpenTableDialog() },
                    shape = RoundedCornerShape(6.dp),
                    color = RedAccent.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RedAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Vị trí: $selectedTableNumber (Đổi / Quét QR ➔)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { Toast.makeText(context, "Không có thông báo mới", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Thông báo", tint = BluePrimary, modifier = Modifier.size(20.dp))
                }

                Box {
                    IconButton(
                        onClick = { onToggleAvatarMenu(true) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = BluePrimary)
                    }

                    DropdownMenu(
                        expanded = showAvatarMenu,
                        onDismissRequest = { onToggleAvatarMenu(false) },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa trang cá nhân", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = BluePrimary) },
                            onClick = {
                                onToggleAvatarMenu(false)
                                onNavigateToProfile()
                            }
                        )

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        DropdownMenuItem(
                            text = { Text("Đăng xuất", fontSize = 14.sp, color = RedAccent) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = RedAccent) },
                            onClick = {
                                onToggleAvatarMenu(false)
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clickable { onNavigateToMenu() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BluePrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hương vị Việt Nam", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Tinh hoa ẩm thực truyền thống", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Khám phá ngay →", fontSize = 12.sp, color = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thanh tìm kiếm
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text("Tìm món ăn, danh mục...", fontSize = 14.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToMenu() },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                disabledBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Danh mục món
        Text("Danh mục", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))

        val categories = listOf("Tất cả", "Phở", "Cơm", "Bún", "Món xào", "Đồ uống")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { category ->
                Surface(
                    modifier = Modifier
                        .width(70.dp)
                        .height(76.dp)
                        .clickable { onNavigateToMenu() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(BluePrimary.copy(alpha = 0.1f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(category, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Món nổi bật
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Món nổi bật", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "Xem tất cả →",
                fontSize = 12.sp,
                color = BluePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToMenu() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (menuList.isEmpty()) {
            Text("Chưa có món ăn trên hệ thống Firestore", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                menuList.take(2).forEach { menuItem ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToMenu() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (menuItem.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = menuItem.imageUrl,
                                        contentDescription = menuItem.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(40.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = menuItem.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatter.format(menuItem.price),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RedAccent
                                )

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(RedAccent)
                                        .clickable { onAddToCart(menuItem) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}