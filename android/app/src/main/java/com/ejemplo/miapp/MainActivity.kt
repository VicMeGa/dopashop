package com.ejemplo.miapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ejemplo.miapp.ui.catalog.CatalogScreen
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.theme.DopaPcTheme

class MainActivity : ComponentActivity() {
    private val catalogViewModel: CatalogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DopaPcTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CatalogScreen(viewModel = catalogViewModel)
                }
            }
        }
    }
}
