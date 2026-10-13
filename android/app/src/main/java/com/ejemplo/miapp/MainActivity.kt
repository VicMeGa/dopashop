package com.ejemplo.miapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ejemplo.miapp.ui.catalog.CatalogScreen
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.product.ProductDetailScreen
import com.ejemplo.miapp.ui.product.ProductDetailViewModel
import com.ejemplo.miapp.ui.theme.DopaPcTheme

sealed class Screen {
    object Catalog : Screen()
    data class Detail(val productId: Long) : Screen()
}

class MainActivity : ComponentActivity() {
    private val catalogViewModel: CatalogViewModel by viewModels()
    private val productDetailViewModel: ProductDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Catalog) }

            DopaPcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Catalog -> {
                            CatalogScreen(
                                viewModel = catalogViewModel,
                                onProductClick = { productId ->
                                    productDetailViewModel.loadProduct(productId)
                                    currentScreen = Screen.Detail(productId)
                                }
                            )
                        }
                        is Screen.Detail -> {
                            ProductDetailScreen(
                                viewModel = productDetailViewModel,
                                onBackClick = {
                                    currentScreen = Screen.Catalog
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
