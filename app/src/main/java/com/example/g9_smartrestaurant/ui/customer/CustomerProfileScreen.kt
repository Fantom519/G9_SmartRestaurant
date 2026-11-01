package com.example.g9_smartrestaurant.ui.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val user = auth.currentUser

    var nameInput by remember { mutableStateOf(user?.displayName ?: "") }
    var phoneInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(true) }

    // 1. Tải thông tin cá nhân từ Firestore khi mở màn hình
    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                isFetching = false
                if (doc.exists()) {
                    nameInput = doc.getString("name") ?: user.displayName ?: "Khách hàng"
                    phoneInput = doc.getString("phone") ?: ""
                }
            }
            .addOnFailureListener {
                isFetching = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, color = BluePrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { innerPadding ->
        if (isFetching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Khối Avatar lớn
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(BluePrimary.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = BluePrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.email ?: "khachhang@gmail.com",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Chỉnh sửa thông tin
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Họ và tên") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Email (Cố định)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(disabledContainerColor = Color(0xFFF0F0F0))
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Nút Lưu thông tin cập nhật lên Firestore
                Button(
                    onClick = {
                        val uid = user?.uid ?: return@Button
                        if (nameInput.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập họ và tên!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        val updateMap = mapOf(
                            "name" to nameInput.trim(),
                            "phone" to phoneInput.trim()
                        )

                        db.collection("users").document(uid).set(updateMap, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "🎉 Đã cập nhật thông tin cá nhân thành công!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Lưu thay đổi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nút Đăng xuất
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất tài khoản", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}