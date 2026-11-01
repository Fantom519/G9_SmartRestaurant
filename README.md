# 🍽️ G9 - Smart Restaurant Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-FFA611?style=for-the-badge&logo=firebase&logoColor=white)](https://firebase.google.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blueviolet?style=for-the-badge)](https://developer.android.com/topic/architecture)

Hệ thống quản lý gọi món và điều phối đơn hàng thời gian thực cho nhà hàng thông minh. Dự án được xây dựng trên nền tảng **Android Native (Kotlin)** với giao diện declarative **Jetpack Compose** kết hợp cơ sở dữ liệu phi quan hệ thời gian thực **Google Cloud Firestore**.

---

## 🛠️ Công nghệ & Kỹ năng sử dụng

### Lập trình & Giao diện (Mobile Application)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Coil Image](https://img.shields.io/badge/Coil%20Image-689F38?style=for-the-badge)

### Backend, Dữ liệu & Mạng (Cloud & Infrastructure)
![Firebase](https://img.shields.io/badge/Firebase%20Firestore-FFA611?style=for-the-badge&logo=firebase&logoColor=white)
![Firebase Auth](https://img.shields.io/badge/Firebase%20Authentication-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

---

## 📌 Tính năng cốt lõi (Core Features)

- [x] **Dynamic Real-time Menu:** Đồng bộ danh mục món ăn trực tiếp từ Firestore, hỗ trợ tải ảnh mượt mà qua thư viện Coil, phân loại theo danh mục (Món chính, Đồ uống, Tráng miệng) và thanh tìm kiếm nhanh.
- [x] **Smart Cart & Order Customization:** Giỏ hàng thông minh dạng Modal Bottom Sheet cho phép điều chỉnh số lượng, xóa món và thêm ghi chú đặc biệt cho từng món (ví dụ: *ít ngọt, không cay*).
- [x] **Instant Order Placement:** Khách đặt đơn trực tiếp theo số bàn; dữ liệu đẩy lên Firestore kèm trạng thái khởi tạo `PLACED`.
- [ ] **Staff Order Management (Đang phát triển):** Giao diện nhân viên nhận đơn theo thời gian thực và cập nhật tiến độ món.
- [ ] **Kitchen Display System - KDS (Đang phát triển):** Điều phối hàng đợi món ăn trong bếp với thuật toán ước tính thời gian chờ (ETA).

---

## 🏗️ Kiến trúc dự án (Architecture)

Dự án áp dụng mô hình **MVVM (Model - View - ViewModel)** kết hợp Repository Pattern nhằm phân tách rõ ràng trách nhiệm giữa các tầng:

```text
com.example.g9_smartrestaurant/
├── model/
│   ├── CartItem.kt
│   ├── MenuItem.kt
│   ├── Order.kt
│   └── UserRole.kt
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt          # Xử lý Auth + Firestore Role Verification
│   │   └── RoleSelectionScreen.kt  # Chọn 1 trong 4 vai trò
│   ├── customer/
│   │   ├── CustomerHomeScreen.kt   # Màn hình chính chứa Bottom Navigation (4 Tabs)
│   │   ├── HomeTabScreen.kt        # Tab 0: Xem nhanh món nổi bật & thông tin
│   │   ├── OrderHistoryTabScreen.kt# Tab 1: Danh sách đơn hàng thời gian thực
│   │   ├── CartTabScreen.kt        # Tab 2: Giỏ hàng & đặt món
│   │   ├── CustomerProfileScreen.kt# Tab 3: Hồ sơ & Đăng xuất
│   │   ├── MenuOrderScreen.kt      # Màn hình Thực đơn chi tiết (Search & Filter)
│   │   └── OrderTrackingScreen.kt  # Màn hình Theo dõi tiến trình 4 bước (Timeline)
│   ├── kitchen/
│   │   └── KitchenQueueScreen.kt   # Màn hình KDS Phòng bếp
│   ├── staff/
│   │   └── StaffOrderScreen.kt     # Màn hình Tiếp nhận & Bưng món
│   └── manager/
│       └── ManagerDashboardScreen.kt # Dashboard Quản lý
└── MainActivity.kt                 # Khai báo Navigation Compose Route toàn bộ app
```
---

## 🚀 Hướng dẫn cài đặt & Khởi chạy (Getting Started)

### Yêu cầu tiên quyết
- **Android Studio:** Phiên bản Iguana / Jellyfish / Ladybug trở lên.
- **JDK:** Java 17 hoặc Java 21.
- **Android SDK:** Compile SDK 34 / 35, Min SDK 24.

