package com.example.g9_smartrestaurant.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g9_smartrestaurant.model.AppNotification
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.theme.BluePrimary
import com.example.g9_smartrestaurant.ui.theme.RedAccent
import com.example.g9_smartrestaurant.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBell(
    userRole: UserRole,
    modifier: Modifier = Modifier,
    iconTint: Color = BluePrimary
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUserId = currentUser?.uid ?: "guest_${userRole.name}"
    val currentUserEmail = currentUser?.email ?: ""

    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var showSheet by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Tất cả, 1: Đơn hàng, 2: Ưu đãi & Khảo sát

    // State cho Popup Khảo sát 5 sao
    var surveyNotification by remember { mutableStateOf<AppNotification?>(null) }

    // Mốc thời gian bắt đầu lắng nghe để chỉ bắn banner cho thông báo MỚI phát sinh
    val listenerStartTime = remember { System.currentTimeMillis() }
    val alertedIds = remember { mutableStateSetOf<String>() }

    // Xin quyền thông báo trên Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Lắng nghe collection 'notifications' real-time
    DisposableEffect(userRole, currentUserId) {
        val registration = db.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val filteredList = snapshot.documents.mapNotNull { doc ->
                    val targetRole = doc.getString("targetRole") ?: ""
                    val targetUserId = doc.getString("targetUserId")

                    val isRoleMatched = targetRole == userRole.name || targetRole == "ALL"
                    val isUserMatched = targetUserId.isNullOrBlank() || targetUserId == currentUserId

                    if (isRoleMatched && isUserMatched) {
                        @Suppress("UNCHECKED_CAST")
                        val readByList = doc.get("readBy") as? List<String> ?: emptyList()
                        AppNotification(
                            id = doc.id,
                            title = doc.getString("title") ?: "Thông báo",
                            message = doc.getString("message") ?: "",
                            type = doc.getString("type") ?: "ORDER_NEW",
                            targetRole = targetRole,
                            targetUserId = targetUserId,
                            orderId = doc.getString("orderId"),
                            promoCode = doc.getString("promoCode"),
                            readBy = readByList,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } else null
                }.sortedByDescending { it.createdAt }

                // Bắn thông báo hệ thống nếu có thông báo mới tạo sau lúc mở app và chưa đọc
                filteredList.forEach { item ->
                    val isUnread = !item.readBy.contains(currentUserId)
                    if (isUnread && item.createdAt > listenerStartTime && !alertedIds.contains(item.id)) {
                        alertedIds.add(item.id)
                        NotificationHelper.showLocalNotification(
                            context = context,
                            title = item.title,
                            message = item.message,
                            notificationId = item.id.hashCode()
                        )
                    }
                }

                notifications = filteredList
            }

        onDispose { registration.remove() }
    }

    val unreadNotifications = remember(notifications, currentUserId) {
        notifications.filter { !it.readBy.contains(currentUserId) }
    }
    val unreadCount = unreadNotifications.size

    // Biểu tượng Quả chuông + Badge số lượng chưa đọc
    IconButton(
        onClick = { showSheet = true },
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = RedAccent,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Thông báo",
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
    }

    // BottomSheet danh sách thông báo
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trung tâm thông báo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(RedAccent.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$unreadCount mới",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedAccent
                                )
                            }
                        }
                    }

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                NotificationHelper.markAllAsRead(
                                    unreadNotifications.map { it.id },
                                    currentUserId
                                )
                            }
                        ) {
                            Text(
                                text = "Đọc tất cả",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                    }
                }

                // Bộ lọc Tab (Dành riêng cho Khách hàng hiển thị đủ 3 tab, nội bộ hiển thị 2 tab)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    val tabs = if (userRole == UserRole.CUSTOMER) {
                        listOf("Tất cả", "Đơn hàng", "Ưu đãi & Khảo sát")
                    } else {
                        listOf("Tất cả", "Đơn hàng")
                    }

                    tabs.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedFilter == index,
                            onClick = { selectedFilter = index },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                val displayedList = remember(notifications, selectedFilter) {
                    when (selectedFilter) {
                        1 -> notifications.filter { it.type == "ORDER_NEW" || it.type == "ORDER_STATUS" }
                        2 -> notifications.filter { it.type == "PROMO" || it.type == "SURVEY" }
                        else -> notifications
                    }
                }

                if (displayedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Chưa có thông báo nào", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(displayedList, key = { it.id }) { item ->
                            val isUnread = !item.readBy.contains(currentUserId)
                            NotificationItemCard(
                                notification = item,
                                isUnread = isUnread,
                                onClick = {
                                    if (isUnread) {
                                        NotificationHelper.markAsRead(item.id, currentUserId)
                                    }
                                    when (item.type) {
                                        "PROMO" -> {
                                            val code = item.promoCode
                                            if (!code.isNullOrBlank()) {
                                                clipboardManager.setText(AnnotatedString(code))
                                                Toast.makeText(
                                                    context,
                                                    "🎉 Đã sao chép mã ưu đãi: $code",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        "SURVEY" -> {
                                            surveyNotification = item
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Popup Khảo sát chất lượng 5 sao khi khách bấm vào thông báo SURVEY
    surveyNotification?.let { targetNotif ->
        SurveyFeedbackDialog(
            title = targetNotif.title,
            onDismiss = { surveyNotification = null },
            onSubmit = { rating, comment ->
                NotificationHelper.submitSurveyFeedback(
                    userId = currentUserId,
                    userEmail = currentUserEmail,
                    rating = rating,
                    comment = comment,
                    orderId = targetNotif.orderId
                ) { success ->
                    surveyNotification = null
                    if (success) {
                        Toast.makeText(
                            context,
                            "❤️ Cảm ơn bạn đã gửi đánh giá cho Bếp Việt!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Gửi đánh giá thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    val (icon, badgeColor) = when (notification.type) {
        "ORDER_NEW" -> Icons.Default.ReceiptLong to RedAccent
        "ORDER_STATUS" -> Icons.Default.Restaurant to BluePrimary
        "PROMO" -> Icons.Default.LocalOffer to Color(0xFFE65100)
        "SURVEY" -> Icons.Default.StarRate to Color(0xFF2E7D32)
        else -> Icons.Default.Campaign to BluePrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) BluePrimary.copy(alpha = 0.06f) else Color(0xFFF8FAFC)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUnread) BluePrimary.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(badgeColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnread) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RedAccent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )

                // Thẻ hiển thị mã giảm giá hoặc nút mời đánh giá
                if (notification.type == "PROMO" && !notification.promoCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Mã: ${notification.promoCode} (Chạm để sao chép)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                } else if (notification.type == "SURVEY") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "👉 Chạm để đánh giá trải nghiệm ngay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatRelativeTime(notification.createdAt),
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun SurveyFeedbackDialog(
    title: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Đánh giá dịch vụ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BluePrimary)
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Mức độ hài lòng của bạn về món ăn & phục vụ:", fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }, modifier = Modifier.size(38.dp)) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star sao",
                                tint = if (star <= rating) Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Chia sẻ thêm cảm nhận của bạn (tùy chọn)...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gửi đánh giá", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Để sau", color = Color.Gray)
            }
        }
    )
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        else -> SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}