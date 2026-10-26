package com.ejemplo.miapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ejemplo.miapp.ui.auth.AuthMode
import com.ejemplo.miapp.ui.auth.AuthScreen
import com.ejemplo.miapp.ui.auth.AuthViewModel
import com.ejemplo.miapp.ui.catalog.CatalogScreen
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.product.ProductDetailScreen
import com.ejemplo.miapp.ui.product.ProductDetailViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    catalogViewModel: CatalogViewModel,
    productDetailViewModel: ProductDetailViewModel,
    authViewModel: AuthViewModel
) {
    val session by authViewModel.session.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "catalog"
    ) {
        composable("catalog") {
            CatalogScreen(
                viewModel = catalogViewModel,
                user = session?.user,
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                onLogoutClick = { authViewModel.logout() },
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                }
            )
        }

        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { entry ->
            val productId = entry.arguments?.getLong("productId") ?: return@composable
            LaunchedEffect(productId) {
                productDetailViewModel.loadProduct(productId)
            }
            ProductDetailScreen(
                viewModel = productDetailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("login") {
            AuthScreen(
                viewModel = authViewModel,
                mode = AuthMode.Login,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack("catalog", inclusive = false)
                },
                onSwitchMode = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("register") {
            AuthScreen(
                viewModel = authViewModel,
                mode = AuthMode.Register,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack("catalog", inclusive = false)
                },
                onSwitchMode = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
