package com.example.g9_smartrestaurant.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.g9_smartrestaurant.MainActivity
import com.example.g9_smartrestaurant.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object NotificationHelper {
    private const val CHANNEL_ID = "bep_viet_notifications_channel"
    private const val CHANNEL_NAME = "Thông báo Bếp Việt"
    private const val CHANNEL_DESC = "Cập nhật đơn hàng, ưu đãi và thông báo vận hành nhà hàng"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showLocalNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannel(context)

        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_bep_viet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun sendNotification(
        title: String,
        message: String,
        type: String,
        targetRole: String,
        targetUserId: String? = null,
        orderId: String? = null,
        promoCode: String? = null,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("notifications").document()

        val payload = hashMapOf(
            "id" to docRef.id,
            "title" to title,
            "message" to message,
            "type" to type,
            "targetRole" to targetRole,
            "targetUserId" to targetUserId,
            "orderId" to orderId,
            "promoCode" to promoCode,
            "readBy" to emptyList<String>(),
            "createdAt" to System.currentTimeMillis()
        )

        docRef.set(payload)
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    fun markAsRead(notificationId: String, userId: String) {
        if (notificationId.isBlank() || userId.isBlank()) return
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(notificationId)
            .update("readBy", FieldValue.arrayUnion(userId))
    }

    fun markAllAsRead(notificationIds: List<String>, userId: String) {
        if (notificationIds.isEmpty() || userId.isBlank()) return
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        notificationIds.forEach { id ->
            val ref = db.collection("notifications").document(id)
            batch.update(ref, "readBy", FieldValue.arrayUnion(userId))
        }
        batch.commit()
    }

    fun submitSurveyFeedback(
        userId: String,
        userEmail: String,
        rating: Int,
        comment: String,
        orderId: String? = null,
        onComplete: (Boolean) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("feedbacks").document()
        val data = hashMapOf(
            "id" to docRef.id,
            "userId" to userId,
            "userEmail" to userEmail,
            "rating" to rating,
            "comment" to comment.trim(),
            "orderId" to orderId,
            "createdAt" to System.currentTimeMillis()
        )
        docRef.set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}