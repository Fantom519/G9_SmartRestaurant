package com.example.g9_smartrestaurant.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailOtpSender {
    // Thay bằng Gmail dùng để gửi thư của hệ thống và Mật khẩu ứng dụng (16 ký tự)
    private const val SENDER_EMAIL = "bepvietapp@gmail.com"
    private const val SENDER_APP_PASSWORD = "epyc tzmx isxy nzdc"

    suspend fun sendOtpEmail(recipientEmail: String, otpCode: String): Boolean {
        // In ra Logcat để tiện kiểm tra ngay cả khi chưa cấu hình App Password
        Log.d("OTP_VERIFY", "Gửi mã OTP [$otpCode] đến email: $recipientEmail")

        return withContext(Dispatchers.IO) {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL, "Bếp Việt Smart Restaurant"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "[$otpCode] Mã xác thực đăng ký tài khoản Bếp Việt"
                    setText(
                        """
                        Xin chào,
                        
                        Mã xác thực (OTP) để hoàn tất đăng ký tài khoản tại nhà hàng Bếp Việt của bạn là:
                        
                        👉 $otpCode 
                        
                        Mã có hiệu lực trong vòng 60 giây. Vui lòng không chia sẻ mã này cho bất kỳ ai.
                        
                        Trân trọng,
                        Đội ngũ Bếp Việt.
                        """.trimIndent()
                    )
                }

                Transport.send(message)
                true
            } catch (e: Exception) {
                Log.e("OTP_VERIFY", "Lỗi gửi Gmail SMTP: ${e.message}")
                false
            }
        }
    }
}