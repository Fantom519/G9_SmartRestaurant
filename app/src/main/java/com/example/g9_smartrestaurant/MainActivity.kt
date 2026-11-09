package com.example.g9_smartrestaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.g9_smartrestaurant.model.CartItem
import com.example.g9_smartrestaurant.model.UserRole
import com.example.g9_smartrestaurant.ui.auth.LoginScreen
import com.example.g9_smartrestaurant.ui.auth.RoleSelectionScreen
import com.example.g9_smartrestaurant.ui.customer.CustomerHomeScreen
import com.example.g9_smartrestaurant.ui.customer.MenuOrderScreen
import com.example.g9_smartrestaurant.ui.customer.OrderTrackingScreen
import com.example.g9_smartrestaurant.ui.kitchen.KitchenQueueScreen
import com.example.g9_smartrestaurant.ui.manager.ManagerDashboardScreen
import com.example.g9_smartrestaurant.ui.staff.StaffOrderScreen
import com.example.g9_smartrestaurant.ui.theme.G9_SmartRestaurantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            G9_SmartRestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Khởi tạo Giỏ hàng VÀ Số bàn dùng chung toàn ứng dụng (Duy trì khi chuyển giữa các màn hình)
    val cartItems = remember { mutableStateMapOf<String, CartItem>() }
    var selectedTableNumber by rememberSaveable { mutableStateOf("Bàn 01") }

    NavHost(
        navController = navController,
        startDestination = "role_selection"
    ) {
        // 1. Màn hình Chọn Vai Trò
        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate("login/${role.name}")
                }
            )
        }

        // 2. Màn hình Đăng nhập (Truyền vai trò đang chọn)
        composable(
            route = "login/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: UserRole.CUSTOMER.name
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.CUSTOMER }

            LoginScreen(
                role = role,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { userRole ->
                    when (userRole) {
                        UserRole.CUSTOMER -> navController.navigate("customer_home") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                        UserRole.STAFF -> navController.navigate("staff_home") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                        UserRole.KITCHEN -> navController.navigate("kitchen_home") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                        UserRole.MANAGER -> navController.navigate("manager_home") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                    }
                }
            )
        }

        // --- PHÂN HỆ KHÁCH HÀNG (CUSTOMER) ---
        composable("customer_home") {
            CustomerHomeScreen(
                cartItems = cartItems,
                initialTab = 0,
                selectedTableNumber = selectedTableNumber,
                onTableSelected = { newTable -> selectedTableNumber = newTable },
                onNavigateToMenu = { navController.navigate("menu") },
                onNavigateToTracking = { orderId ->
                    navController.navigate("tracking/$orderId")
                },
                onLogout = {
                    cartItems.clear()
                    selectedTableNumber = "Bàn 01"
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Route mở trực tiếp Tab Giỏ hàng
        composable("customer_home_cart") {
            CustomerHomeScreen(
                cartItems = cartItems,
                initialTab = 2,
                selectedTableNumber = selectedTableNumber,
                onTableSelected = { newTable -> selectedTableNumber = newTable }, // 👈 Đã bổ sung dòng bị thiếu
                onNavigateToMenu = { navController.navigate("menu") },
                onNavigateToTracking = { orderId ->
                    navController.navigate("tracking/$orderId")
                },
                onLogout = {
                    cartItems.clear()
                    selectedTableNumber = "Bàn 01"
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Màn hình chọn Thực đơn chi tiết
        composable("menu") {
            MenuOrderScreen(
                cartItems = cartItems,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = {
                    navController.navigate("customer_home_cart") {
                        popUpTo("customer_home") { inclusive = true }
                    }
                }
            )
        }

        // Màn hình Theo dõi trạng thái đơn hàng (Tracking)
        composable(
            route = "tracking/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- PHÂN HỆ NHÂN VIÊN (STAFF) ---
        composable("staff_home") {
            StaffOrderScreen(
                onLogout = {
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- PHÂN HỆ BẾP (KITCHEN) ---
        composable("kitchen_home") {
            KitchenQueueScreen(
                onLogout = {
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- PHÂN HỆ QUẢN LÝ (MANAGER) ---
        composable("manager_home") {
            ManagerDashboardScreen(
                onNavigateToTracking = { orderId ->
                    navController.navigate("tracking/$orderId")
                },
                onLogout = {
                    navController.navigate("role_selection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}