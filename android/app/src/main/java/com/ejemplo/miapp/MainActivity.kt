package com.ejemplo.miapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.ejemplo.miapp.ui.catalog.CatalogScreen
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.product.ProductDetailScreen
import com.ejemplo.miapp.ui.product.ProductDetailViewModel
import com.ejemplo.miapp.ui.theme.DopaPcTheme

class MainActivity : ComponentActivity() {
    private val catalogViewModel: CatalogViewModel by viewModels()
    private val productDetailViewModel: ProductDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedProductId by rememberSaveable { mutableStateOf<Long?>(null) }

            BackHandler(enabled = selectedProductId != null) {
                selectedProductId = null
            }

            LaunchedEffect(selectedProductId) {
                val id = selectedProductId
                if (id != null) {
                    if (productDetailViewModel.uiState.value.product?.id != id) {
                        productDetailViewModel.loadProduct(id)
                    }
                }
            }

            DopaPcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (selectedProductId == null) {
                        CatalogScreen(
                            viewModel = catalogViewModel,
                            onProductClick = { productId ->
                                selectedProductId = productId
                            }
                        )
                    } else {
                        ProductDetailScreen(
                            viewModel = productDetailViewModel,
                            onBackClick = {
                                selectedProductId = null
                            }
                        )
                    }
                }
            }
        }
    }
}
