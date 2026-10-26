package com.ejemplo.miapp

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ejemplo.miapp.ui.auth.AuthViewModel
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.navigation.AppNavHost
import com.ejemplo.miapp.ui.product.ProductDetailViewModel
import com.ejemplo.miapp.ui.theme.DopaPcTheme

class MainActivity : ComponentActivity() {
    private val catalogViewModel: CatalogViewModel by viewModels()
    private val productDetailViewModel: ProductDetailViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            val isSessionReady by authViewModel.isSessionReady.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            DopaPcTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!isSessionReady) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563eb))
                        }
                    } else {
                        AppNavHost(
                            navController = navController,
                            catalogViewModel = catalogViewModel,
                            productDetailViewModel = productDetailViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
