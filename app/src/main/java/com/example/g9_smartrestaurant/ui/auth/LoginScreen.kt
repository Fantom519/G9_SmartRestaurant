package com.example.g9_smartrestaurant.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.R
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.utils.EmailOtpSender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    role: UserRole,
    onNavigateBack: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val isInternalRole = role != UserRole.CUSTOMER
    var isLoginTab by remember { mutableStateOf(true) }

    // State cho Dialog Xác thực OTP Đăng ký
    var showOtpDialog by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var isCreatingAccount by remember { mutableStateOf(false) }

    // State cho quy trình Quên mật khẩu
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isSendingReset by remember { mutableStateOf(false) }

    var showForgotOtpDialog by remember { mutableStateOf(false) }
    var forgotOtp by remember { mutableStateOf("") }

    var showNewPasswordDialog by remember { mutableStateOf(false) }
    var isUpdatingPassword by remember { mutableStateOf(false) }

    // Điều kiện kiểm tra mật khẩu (cho phần Đăng ký)
    val hasMinLength = password.length >= 6
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val isPasswordMatch = password.isNotEmpty() && password == confirmPassword
    val isPasswordValid = hasMinLength && hasLetter && hasDigit

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

            if (!isInternalRole) {
                Surface(
                    shape = RoundedCornerShape(25.dp),
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Button(
                            onClick = {
                                isLoginTab = true
                                confirmPassword = ""
                            },
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

            // 1. Ô NHẬP EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                placeholder = { Text("Email đăng nhập (VD: ten@gmail.com)", fontSize = 13.sp, color = Color.Gray) },
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

            // 2. Ô NHẬP MẬT KHẨU
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

            // 3. Ô XÁC NHẬN MẬT KHẨU & ĐIỀU KIỆN (CHỈ HIỆN KHI ĐĂNG KÝ)
            if (!isLoginTab) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Xác nhận lại mật khẩu", fontSize = 13.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = BluePrimary) },
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (confirmPassword.isNotEmpty() && !isPasswordMatch) RedAccent else BluePrimary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PasswordConditionRow("Tối thiểu 6 ký tự", hasMinLength)
                        PasswordConditionRow("Bao gồm cả chữ cái và chữ số", hasLetter && hasDigit)
                        PasswordConditionRow("Mật khẩu xác nhận trùng khớp", isPasswordMatch)
                    }
                }
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // NÚT HÀNH ĐỘNG CHÍNH
            Button(
                onClick = {
                    if (isLoginTab) {
                        // --- LUỒNG ĐĂNG NHẬP ---
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val uid = authResult.user?.uid ?: ""
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { doc ->
                                        isLoading = false
                                        val rawRole = doc.getString("role")?.trim()?.uppercase() ?: ""
                                        val actualUserRole = try {
                                            UserRole.valueOf(rawRole)
                                        } catch (e: Exception) {
                                            null
                                        }

                                        if (actualUserRole == null || actualUserRole != role) {
                                            auth.signOut()
                                            val actualRoleTitle = when (actualUserRole) {
                                                UserRole.CUSTOMER -> "Khách hàng"
                                                UserRole.STAFF -> "Nhân viên"
                                                UserRole.KITCHEN -> "Phòng bếp"
                                                UserRole.MANAGER -> "Quản lý"
                                                null -> "Không xác định"
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
                        // --- LUỒNG ĐĂNG KÝ -> CHỈ GỬI DUY NHẤT 1 EMAIL OTP 6 SỐ ---
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Định dạng Email không hợp lệ!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isPasswordValid) {
                            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm cả chữ và số!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isPasswordMatch) {
                            Toast.makeText(context, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        db.collection("users").whereEqualTo("email", email).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    isLoading = false
                                    Toast.makeText(context, "Email này đã được đăng ký trước đó!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newOtp = (100000..999999).random().toString()
                                    generatedOtp = newOtp

                                    coroutineScope.launch {
                                        val isSent = EmailOtpSender.sendOtpEmail(email, newOtp)
                                        isLoading = false
                                        showOtpDialog = true
                                        if (isSent) {
                                            Toast.makeText(context, "📧 Đã gửi mã OTP 6 số đến $email!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "⚠️ Xem mã OTP trong Logcat (OTP_VERIFY)!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // ==========================================
        // 1. DIALOG XÁC THỰC OTP ĐĂNG KÝ (KHÔNG GỬI LINK XÁC THỰC FIREBASE)
        // ==========================================
        if (showOtpDialog) {
            OtpVerificationDialog(
                title = "Xác thực đăng ký",
                email = email,
                expectedOtp = generatedOtp,
                isVerifying = isCreatingAccount,
                onResendOtp = {
                    val newOtp = (100000..999999).random().toString()
                    generatedOtp = newOtp
                    coroutineScope.launch {
                        EmailOtpSender.sendOtpEmail(email, newOtp)
                        Toast.makeText(context, "Đã gửi lại mã OTP mới!", Toast.LENGTH_SHORT).show()
                    }
                },
                onVerifySuccess = {
                    isCreatingAccount = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: ""
                            val userMap = mapOf(
                                "uid" to uid,
                                "email" to email,
                                "password" to password,
                                "role" to UserRole.CUSTOMER.name,
                                "isEmailVerified" to true,
                                "createdAt" to System.currentTimeMillis()
                            )
                            db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener {
                                    auth.signOut()
                                    isCreatingAccount = false
                                    showOtpDialog = false
                                    password = ""
                                    confirmPassword = ""
                                    isLoginTab = true
                                    Toast.makeText(
                                        context,
                                        "🎉 Xác thực OTP thành công! Vui lòng đăng nhập.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    isCreatingAccount = false
                                    Toast.makeText(context, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            isCreatingAccount = false
                            Toast.makeText(context, "Lỗi tạo tài khoản: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                onDismiss = {
                    if (!isCreatingAccount) showOtpDialog = false
                }
            )
        }

        // ==========================================
        // 2. QUÊN MẬT KHẨU - BƯỚC 1: NHẬP EMAIL & KIỂM TRA ĐÚNG VAI TRÒ CỔNG TRUY CẬP
        // ==========================================
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
                        Text(
                            "Nhập địa chỉ email đã đăng ký để nhận mã xác thực (OTP) 6 số từ Bếp Việt.",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
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
                            if (!Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                                Toast.makeText(context, "Vui lòng nhập Email hợp lệ!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSendingReset = true
                            db.collection("users").whereEqualTo("email", resetEmail).get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.isEmpty) {
                                        isSendingReset = false
                                        Toast.makeText(context, "Email này chưa được đăng ký trong hệ thống!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val doc = snapshot.documents.first()
                                        val rawRole = doc.getString("role")?.trim()?.uppercase() ?: ""
                                        val actualUserRole = try {
                                            UserRole.valueOf(rawRole)
                                        } catch (e: Exception) {
                                            null
                                        }

                                        if (actualUserRole == null || actualUserRole != role) {
                                            isSendingReset = false
                                            val actualRoleTitle = when (actualUserRole) {
                                                UserRole.CUSTOMER -> "Khách hàng"
                                                UserRole.STAFF -> "Nhân viên"
                                                UserRole.KITCHEN -> "Phòng bếp"
                                                UserRole.MANAGER -> "Quản lý"
                                                null -> "Không xác định ($rawRole)"
                                            }
                                            Toast.makeText(
                                                context,
                                                "⚠️ Tài khoản này thuộc cổng [$actualRoleTitle], không thể khôi phục tại đây!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnSuccessListener
                                        }

                                        val newOtp = (100000..999999).random().toString()
                                        forgotOtp = newOtp
                                        coroutineScope.launch {
                                            val isSent = EmailOtpSender.sendOtpEmail(resetEmail, newOtp)
                                            isSendingReset = false
                                            showForgotPasswordDialog = false
                                            showForgotOtpDialog = true
                                            if (isSent) {
                                                Toast.makeText(context, "📧 Đã gửi mã OTP 6 số tới $resetEmail!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "⚠️ Xem mã OTP trong Logcat (OTP_VERIFY)!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isSendingReset = false
                                    Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSendingReset
                    ) {
                        if (isSendingReset) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Gửi mã OTP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        // ==========================================
        // 3. QUÊN MẬT KHẨU - BƯỚC 2: NHẬP OTP 6 SỐ
        // ==========================================
        if (showForgotOtpDialog) {
            OtpVerificationDialog(
                title = "Xác thực khôi phục",
                email = resetEmail,
                expectedOtp = forgotOtp,
                isVerifying = false,
                onResendOtp = {
                    val newOtp = (100000..999999).random().toString()
                    forgotOtp = newOtp
                    coroutineScope.launch {
                        EmailOtpSender.sendOtpEmail(resetEmail, newOtp)
                        Toast.makeText(context, "Đã gửi lại mã OTP mới!", Toast.LENGTH_SHORT).show()
                    }
                },
                onVerifySuccess = {
                    showForgotOtpDialog = false
                    showNewPasswordDialog = true
                },
                onDismiss = {
                    showForgotOtpDialog = false
                }
            )
        }

        // ==========================================
        // 4. QUÊN MẬT KHẨU - BƯỚC 3: ĐẶT MẬT KHẨU MỚI
        // ==========================================
        if (showNewPasswordDialog) {
            ResetPasswordDialog(
                email = resetEmail,
                isUpdating = isUpdatingPassword,
                onConfirmNewPassword = { newPwd ->
                    isUpdatingPassword = true
                    db.collection("users").whereEqualTo("email", resetEmail).get()
                        .addOnSuccessListener { snapshot ->
                            val doc = snapshot.documents.firstOrNull()
                            val rawRole = doc?.getString("role")?.trim()?.uppercase() ?: ""
                            val docRole = try {
                                UserRole.valueOf(rawRole)
                            } catch (e: Exception) {
                                null
                            }

                            if (doc == null || docRole != role) {
                                isUpdatingPassword = false
                                Toast.makeText(context, "⚠️ Sai lệch quyền truy cập tài khoản!", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val oldPassword = doc.getString("password")
                            if (!oldPassword.isNullOrBlank()) {
                                auth.signInWithEmailAndPassword(resetEmail, oldPassword)
                                    .addOnSuccessListener { authResult ->
                                        authResult.user?.updatePassword(newPwd)
                                            ?.addOnSuccessListener {
                                                db.collection("users").document(doc.id)
                                                    .update("password", newPwd)
                                                    .addOnCompleteListener {
                                                        auth.signOut()
                                                        isUpdatingPassword = false
                                                        showNewPasswordDialog = false
                                                        email = resetEmail
                                                        password = ""
                                                        isLoginTab = true
                                                        Toast.makeText(
                                                            context,
                                                            "🎉 Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            }
                                            ?.addOnFailureListener { e ->
                                                auth.signOut()
                                                isUpdatingPassword = false
                                                Toast.makeText(context, "Lỗi cập nhật mật khẩu: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        isUpdatingPassword = false
                                        Toast.makeText(context, "Không thể xác thực mật khẩu cũ trên hệ thống!", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                isUpdatingPassword = false
                                Toast.makeText(context, "Tài khoản này chưa hỗ trợ đổi mật khẩu qua OTP!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            isUpdatingPassword = false
                            Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                onDismiss = {
                    if (!isUpdatingPassword) showNewPasswordDialog = false
                }
            )
        }
    }
}

@Composable
private fun ResetPasswordDialog(
    email: String,
    isUpdating: Boolean,
    onConfirmNewPassword: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isNewPwdVisible by remember { mutableStateOf(false) }
    var isConfirmPwdVisible by remember { mutableStateOf(false) }

    val hasMinLength = newPassword.length >= 6
    val hasLetter = newPassword.any { it.isLetter() }
    val hasDigit = newPassword.any { it.isDigit() }
    val isMatch = newPassword.isNotEmpty() && newPassword == confirmNewPassword
    val isValid = hasMinLength && hasLetter && hasDigit && isMatch

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BluePrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đặt mật khẩu mới", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                Text(email, fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("Mật khẩu mới", fontSize = 13.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { isNewPwdVisible = !isNewPwdVisible }) {
                            Icon(
                                imageVector = if (isNewPwdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (isNewPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    placeholder = { Text("Xác nhận mật khẩu mới", fontSize = 13.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = BluePrimary) },
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPwdVisible = !isConfirmPwdVisible }) {
                            Icon(
                                imageVector = if (isConfirmPwdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (isConfirmPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PasswordConditionRow("Tối thiểu 6 ký tự", hasMinLength)
                    PasswordConditionRow("Bao gồm cả chữ cái và chữ số", hasLetter && hasDigit)
                    PasswordConditionRow("Mật khẩu xác nhận trùng khớp", isMatch)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isValid) {
                        Toast.makeText(context, "Vui lòng đáp ứng đầy đủ các điều kiện mật khẩu!", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirmNewPassword(newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun PasswordConditionRow(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF2E7D32) else Color.Gray,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isMet) Color(0xFF2E7D32) else Color.Gray,
            fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun OtpVerificationDialog(
    title: String = "Xác thực mã OTP",
    email: String,
    expectedOtp: String,
    isVerifying: Boolean,
    onResendOtp: () -> Unit,
    onVerifySuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BluePrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = BluePrimary)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mã xác thực gồm 6 chữ số đã được gửi tới:",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                BasicTextField(
                    value = otpInput,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            otpInput = newValue
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) { index ->
                                val char = otpInput.getOrNull(index)?.toString() ?: ""
                                val isFocused = otpInput.length == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .background(Color(0xFFF8F9FA), RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isFocused) 2.dp else 1.dp,
                                            color = if (isFocused) BluePrimary else Color(0xFFD0D5DD),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (countdown > 0) {
                    Text("Gửi lại mã sau ${countdown}s", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text(
                        text = "Gửi lại mã OTP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedAccent,
                        modifier = Modifier.clickable {
                            countdown = 60
                            otpInput = ""
                            onResendOtp()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (otpInput.length < 6) {
                        Toast.makeText(context, "Vui lòng nhập đủ 6 chữ số OTP!", Toast.LENGTH_SHORT).show()
                    } else if (otpInput != expectedOtp) {
                        Toast.makeText(context, "❌ Mã OTP không chính xác!", Toast.LENGTH_SHORT).show()
                    } else {
                        onVerifySuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isVerifying
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("Xác nhận", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isVerifying) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}