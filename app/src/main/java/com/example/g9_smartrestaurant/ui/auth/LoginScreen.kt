package com.example.g9_smartrestaurant.ui.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.User
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.theme.BgLight
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    role: UserRole,
    onNavigateBack: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var isLoginTab by remember { mutableStateOf(true) }
    var accountInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    val roleName = when (role) {
        UserRole.CUSTOMER -> "Khách hàng"
        UserRole.STAFF -> "Nhân viên"
        UserRole.KITCHEN -> "Phòng bếp"
        UserRole.MANAGER -> "Quản lý"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        },
        containerColor = BgLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Tiêu đề nghiêng nghệ thuật
                Text(
                    text = if (isLoginTab) "Đăng nhập" else "Đăng ký",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    color = BluePrimary
                )

                Text(
                    text = "Để tiếp tục sử dụng ứng dụng ($roleName)",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Toggle Tab Đăng nhập / Đăng ký
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color(0xFFEEEEEE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .background(
                                    if (isLoginTab) BluePrimary else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable { isLoginTab = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đăng nhập",
                                fontWeight = FontWeight.Bold,
                                color = if (isLoginTab) Color.White else Color.DarkGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .background(
                                    if (!isLoginTab) BluePrimary else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable { isLoginTab = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đăng ký",
                                fontWeight = FontWeight.Bold,
                                color = if (!isLoginTab) Color.White else Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Ô Email / Số điện thoại
                OutlinedTextField(
                    value = accountInput,
                    onValueChange = { accountInput = it },
                    placeholder = { Text("Email hoặc số điện thoại", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Ô Mật khẩu
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    placeholder = { Text("Mật khẩu", color = Color.Gray) },
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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color(0xFFDDDDDD)
                    )
                )

                // 3. Ô Xác nhận mật khẩu (Chỉ xuất hiện ở Tab Đăng ký)
                if (!isLoginTab) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        placeholder = { Text("Xác nhận mật khẩu", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary) },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )
                } else {
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
                            Text(text = "Ghi nhớ đăng nhập", fontSize = 12.sp, color = Color.DarkGray)
                        }

                        Text(
                            text = "Quên mật khẩu?",
                            fontSize = 12.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Tính năng quên mật khẩu đang phát triển", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút Đăng nhập / Đăng ký
                Button(
                    onClick = {
                        val email = accountInput.trim()
                        val password = passwordInput.trim()

                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (isLoginTab) {
                            // ==========================================
                            // 1. LUỒNG ĐĂNG NHẬP + KIỂM TRA LINK & ROLE
                            // ==========================================
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener { authResult ->
                                    val user = authResult.user
                                    val uid = user?.uid ?: ""

                                    // Làm mới trạng thái tài khoản để kiểm tra đã bấm link xác minh chưa
                                    user?.reload()?.addOnCompleteListener {
                                        val isVerified = user.isEmailVerified

                                        if (!isVerified) {
                                            isLoading = false
                                            auth.signOut() // Đăng xuất ngay nếu chưa bấm Link
                                            Toast.makeText(
                                                context,
                                                "⚠️ Email chưa được kích hoạt! Vui lòng vào Gmail (kể cả thư rác/Spam) bấm vào Link xác thực trước khi Đăng nhập.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnCompleteListener
                                        }

                                        // Kiểm tra quyền vai trò trên Firestore
                                        db.collection("users").document(uid).get()
                                            .addOnSuccessListener { document ->
                                                isLoading = false
                                                if (document.exists()) {
                                                    val savedRoleInDb = document.getString("role") ?: ""
                                                    if (savedRoleInDb == role.name) {
                                                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess(role)
                                                    } else {
                                                        auth.signOut()
                                                        val savedRoleName = when (savedRoleInDb) {
                                                            UserRole.CUSTOMER.name -> "Khách hàng"
                                                            UserRole.STAFF.name -> "Nhân viên"
                                                            UserRole.KITCHEN.name -> "Phòng bếp"
                                                            UserRole.MANAGER.name -> "Quản lý"
                                                            else -> savedRoleInDb
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "⛔ LỖI PHÂN QUYỀN: Tài khoản của bạn là '$savedRoleName'. Không thể truy cập cổng '$roleName'!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                } else {
                                                    auth.signOut()
                                                    Toast.makeText(context, "Không tìm thấy dữ liệu vai trò!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                auth.signOut()
                                                Toast.makeText(context, "Lỗi kiểm tra dữ liệu!", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Đăng nhập thất bại: Sai Email hoặc Mật khẩu!", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // ==========================================
                            // 2. LUỒNG ĐĂNG KÝ (GỬI EMAIL THẬT + TỰ ĐỘNG THOÁT)
                            // ==========================================
                            if (password != confirmPasswordInput.trim()) {
                                Toast.makeText(context, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (password.length < 6) {
                                Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener { authResult ->
                                    val user = authResult.user
                                    val uid = user?.uid ?: ""
                                    val newUser = User(uid = uid, email = email, role = role.name)

                                    // Lưu thông tin vai trò vào Firestore
                                    db.collection("users").document(uid).set(newUser)
                                        .addOnSuccessListener {
                                            // Gửi Email chứa Link kích hoạt chính thức từ Firebase
                                            user?.sendEmailVerification()
                                                ?.addOnCompleteListener { sendTask ->
                                                    isLoading = false
                                                    auth.signOut() // Đăng xuất để chờ người dùng bấm Link

                                                    if (sendTask.isSuccessful) {
                                                        Toast.makeText(
                                                            context,
                                                            "📩 Đã gửi Email kích hoạt tới $email!\nVui lòng mở Gmail (kể cả thư rác/Spam) và bấm vào Link xác nhận.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        isLoginTab = true // Chuyển giao diện về Đăng nhập
                                                    } else {
                                                        Toast.makeText(context, "Lỗi gửi email kích hoạt: ${sendTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Đăng ký thất bại: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(26.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = if (isLoginTab) "Đăng nhập →" else "Đăng ký ngay →",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Đường phân cách
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDDDDDD)
                    )
                    Text(
                        text = "Hoặc đăng nhập bằng",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDDDDDD)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3 Nút đăng nhập mạng xã hội (Google, Facebook, Apple)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialButton(
                        onClick = {
                            Toast.makeText(context, "Đăng nhập Google đang khởi chạy...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = "G",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEA4335)
                        )
                    }

                    SocialButton(
                        onClick = {
                            Toast.makeText(context, "Đăng nhập Facebook đang khởi chạy...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = "f",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1877F2)
                        )
                    }

                    SocialButton(
                        onClick = {
                            Toast.makeText(context, "Đăng nhập Apple đang khởi chạy...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = "",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SocialButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .shadow(elevation = 2.dp, shape = CircleShape),
        shape = CircleShape,
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}