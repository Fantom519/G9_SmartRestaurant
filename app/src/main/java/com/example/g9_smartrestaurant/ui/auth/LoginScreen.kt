package com.example.g9_smartrestaurant.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.R
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    role: UserRole,
    onNavigateBack: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Kiểm tra xem đây có phải là vai trò nội bộ hay không
    val isInternalRole = role != UserRole.CUSTOMER

    // Nếu là vai trò nội bộ -> Ép buộc chỉ được Đăng nhập (isLoginTab = true)
    var isLoginTab by remember { mutableStateOf(true) }

    // State Popup Quên mật khẩu
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isSendingReset by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.85f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Trở về",
                    tint = BluePrimary
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_bep_viet),
                    contentDescription = "Logo Bếp Việt",
                    modifier = Modifier.size(90.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Hương vị Việt - Kết nối mọi người",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isLoginTab) "Đăng nhập" else "Đăng ký tài khoản",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )

                val roleTitle = when (role) {
                    UserRole.CUSTOMER -> "Khách hàng"
                    UserRole.STAFF -> "Nhân viên"
                    UserRole.KITCHEN -> "Phòng bếp"
                    UserRole.MANAGER -> "Quản lý"
                }

                Text(
                    text = "Cổng truy cập: $roleTitle",
                    fontSize = 12.sp,
                    color = RedAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CHI LƯU Ý: Chỉ hiển thị Tab Đăng ký nếu là KHÁCH HÀNG
            if (!isInternalRole) {
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Button(
                            onClick = { isLoginTab = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginTab) BluePrimary else Color.Transparent,
                                contentColor = if (isLoginTab) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = null
                        ) {
                            Text("Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { isLoginTab = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLoginTab) BluePrimary else Color.Transparent,
                                contentColor = if (!isLoginTab) Color.White else Color.Gray
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = null
                        ) {
                            Text("Đăng ký", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Thẻ thông báo dành riêng cho nhân sự nội bộ
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tài khoản nội bộ do Quản lý/Admin cấp. Vui lòng liên hệ Quản lý nếu chưa có tài khoản.",
                            fontSize = 11.sp,
                            color = BluePrimary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                placeholder = { Text("Email đăng nhập", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Mật khẩu", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = BluePrimary)
                    )
                    Text("Ghi nhớ đăng nhập", fontSize = 12.sp, color = Color.DarkGray)
                }

                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        resetEmail = email
                        showForgotPasswordDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    if (isLoginTab) {
                        // ĐĂNG NHẬP
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val uid = authResult.user?.uid ?: ""
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        isLoading = false
                                        val dbRoleStr = doc.getString("role") ?: UserRole.CUSTOMER.name
                                        val actualUserRole = try { UserRole.valueOf(dbRoleStr) } catch (e: Exception) { UserRole.CUSTOMER }

                                        if (actualUserRole != role) {
                                            auth.signOut()
                                            val actualRoleTitle = when (actualUserRole) {
                                                UserRole.CUSTOMER -> "Khách hàng"
                                                UserRole.STAFF -> "Nhân viên"
                                                UserRole.KITCHEN -> "Phòng bếp"
                                                UserRole.MANAGER -> "Quản lý"
                                            }
                                            Toast.makeText(
                                                context,
                                                "⚠️ Tài khoản của bạn là [$actualRoleTitle], không thể đăng nhập vào cổng này!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess(actualUserRole)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        auth.signOut()
                                        Toast.makeText(context, "Không thể đọc dữ liệu vai trò: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Mật khẩu hoặc Email không chính xác!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // CHỈ CHO PHÉP ĐĂNG KÝ TÀI KHOẢN KHÁCH HÀNG
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val uid = authResult.user?.uid ?: ""
                                val userMap = mapOf(
                                    "uid" to uid,
                                    "email" to email,
                                    "role" to UserRole.CUSTOMER.name,
                                    "createdAt" to System.currentTimeMillis()
                                )
                                db.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(context, "🎉 Đăng ký tài khoản Khách hàng thành công!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(UserRole.CUSTOMER)
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Đăng ký thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(25.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = if (isLoginTab) "Đăng nhập →" else "Đăng ký tài khoản →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // DIALOG QUÊN MẬT KHẨU
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { if (!isSendingReset) showForgotPasswordDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                title = {
                    Text("Quên mật khẩu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                },
                text = {
                    Column {
                        Text("Nhập email để nhận liên kết khôi phục mật khẩu từ Bếp Việt.", fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it.trim() },
                            placeholder = { Text("Địa chỉ Email", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập Email!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSendingReset = true
                            auth.sendPasswordResetEmail(resetEmail)
                                .addOnSuccessListener {
                                    isSendingReset = false
                                    showForgotPasswordDialog = false
                                    Toast.makeText(context, "📧 Đã gửi email khôi phục! Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    isSendingReset = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSendingReset
                    ) {
                        if (isSendingReset) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Gửi yêu cầu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }, enabled = !isSendingReset) {
                        Text("Hủy", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            )
        }
    }
}