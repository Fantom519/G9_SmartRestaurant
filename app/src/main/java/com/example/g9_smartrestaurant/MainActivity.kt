package com.example.g9_smartrestaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.g9_smartrestaurant.ui.theme.G9_SmartRestaurantTheme
import com.example.g9_smartrestaurant.ui.MenuScreen
import com.example.g9_smartrestaurant.ui.StaffDashboardRoute

private const val DEBUG_ROLE = "Staff"   // đổi thành "Customer" để về màn khách
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            G9_SmartRestaurantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MenuScreen()
                        //if (DEBUG_ROLE == "Staff") StaffDashboardRoute() else MenuScreen()
                        //Dùng để chạy UI của Staff (tách biệt UI của khách)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    G9_SmartRestaurantTheme {
        Greeting("Android")
    }
}