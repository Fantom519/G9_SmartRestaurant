package com.example.g9_smartrestaurant.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SoupKitchen // Thay cho Chef
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    Scaffold(
        containerColor = BgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Logo & Lời chào
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                // Logo Bếp Việt
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(BluePrimary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bếp Việt",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )

                Text(
                    text = "Hương vị Việt - Kết nối mọi người",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Chào mừng bạn!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                Text(
                    text = "Vui lòng chọn vai trò để đăng nhập\nvào hệ thống.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Lưới 4 vai trò
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RoleCard(
                        title = "Khách hàng",
                        description = "Đặt món, theo dõi đơn, trải nghiệm ẩm thực Việt",
                        icon = Icons.Default.Person,
                        role = UserRole.CUSTOMER,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoleSelected(UserRole.CUSTOMER) }
                    )

                    RoleCard(
                        title = "Nhân viên",
                        description = "Quản lý đơn hàng, phục vụ khách",
                        icon = Icons.Default.Person,
                        role = UserRole.STAFF,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoleSelected(UserRole.STAFF) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RoleCard(
                        title = "Phòng bếp",
                        description = "Xử lý đơn hàng, chuẩn bị món ăn",
                        icon = Icons.Default.Restaurant,
                        role = UserRole.KITCHEN,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoleSelected(UserRole.KITCHEN) }
                    )

                    RoleCard(
                        title = "Quản lý",
                        description = "Quản lý nhân sự, món ăn, thống kê doanh thu",
                        icon = Icons.Default.Settings,
                        role = UserRole.MANAGER,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoleSelected(UserRole.MANAGER) }
                    )
                }
            }

            // Footer
            Text(
                text = "— Ẩm thực Việt - Tinh hoa văn hóa —",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    role: UserRole,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BluePrimary.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = BluePrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .size(28.dp)
                    .background(RedAccent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
