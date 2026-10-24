package com.example.g9_smartrestaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.g9_smartrestaurant.ui.customer.CustomerHomeScreen
import com.example.g9_smartrestaurant.ui.customer.MenuOrderScreen
import com.example.g9_smartrestaurant.ui.customer.OrderTrackingScreen
import com.example.g9_smartrestaurant.ui.theme.G9_SmartRestaurantTheme
import com.example.g9_smartrestaurant.ui.staff.StaffOrderScreen
import com.example.g9_smartrestaurant.ui.staff.StaffOrderDetailScreen
import com.example.g9_smartrestaurant.ui.staff.StaffOrderHistoryScreen

// 1. THÊM IMPORT KITCHEN TẠI ĐÂY
import com.example.g9_smartrestaurant.ui.kitchen.KitchenQueueScreen

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

    NavHost(
        navController = navController,
        startDestination = "kitchen/queue"
//         startDestination = "home"
//         startDestination = "staff/orders"
    )
        {
        // Màn hình Kitchen
        composable("kitchen/queue") {
            KitchenQueueScreen()
        }

        // Màn hình 1: Trang chủ Khách hàng
        composable("home") {
            CustomerHomeScreen(
                onNavigateToMenu = {
                    navController.navigate("menu")
                },
                onNavigateToOrders = {
                    navController.navigate("menu")
                }
            )
        }

        // Màn hình 2: Đặt món, tìm kiếm, giỏ hàng, ghi chú
        composable("menu") {
            MenuOrderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderPlacedSuccess = { orderId ->
                    navController.navigate("tracking/$orderId")
                }
            )
        }

        // Màn hình 3: Theo dõi trạng thái đơn hàng thời gian thực
        composable(
            route = "tracking/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderTrackingScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("staff/orders") {
            StaffOrderScreen(
                onOrderClick = { orderId ->
                    navController.navigate("staff/order/$orderId")
                },
                onNavigateToHistory = {
                    navController.navigate("staff/history")
                }
            )
        }

        composable(
            route = "staff/order/{orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val orderId =
                backStackEntry.arguments?.getString("orderId") ?: ""

            StaffOrderDetailScreen(
                orderId = orderId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("staff/history") {
            StaffOrderHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOrderClick = { orderId ->
                    navController.navigate("staff/order/$orderId")
                }
            )
        }

    }
}