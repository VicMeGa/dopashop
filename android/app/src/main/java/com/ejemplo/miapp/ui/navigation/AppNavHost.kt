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
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.ui.auth.AuthMode
import com.ejemplo.miapp.ui.auth.AuthScreen
import com.ejemplo.miapp.ui.auth.AuthViewModel
import com.ejemplo.miapp.ui.cart.CartScreen
import com.ejemplo.miapp.ui.cart.CartViewModel
import com.ejemplo.miapp.ui.catalog.CatalogScreen
import com.ejemplo.miapp.ui.catalog.CatalogViewModel
import com.ejemplo.miapp.ui.common.PlaceholderScreen
import com.ejemplo.miapp.ui.product.ProductDetailScreen
import com.ejemplo.miapp.ui.product.ProductDetailViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    catalogViewModel: CatalogViewModel,
    productDetailViewModel: ProductDetailViewModel,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel
) {
    val session by authViewModel.session.collectAsStateWithLifecycle()

    val requireLogin: (String) -> Unit = { redirect ->
        authViewModel.setRedirect(redirect)
        navController.navigate("login") {
            popUpTo(navController.currentDestination?.id ?: navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = "catalog"
    ) {
        composable("catalog") {
            CatalogScreen(
                viewModel = catalogViewModel,
                onProductClick = { productId ->
                    navController.navigate("product/$productId")
                },
                onAddToCart = cartViewModel::add
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
                onBackClick = { navController.popBackStack() },
                onAddToCart = cartViewModel::add
            )
        }

        composable("cart") {
            CartScreen(
                viewModel = cartViewModel,
                onContinueShopping = {
                    navController.navigate("catalog") { launchSingleTop = true }
                },
                onCheckout = { navController.navigate("checkout") }
            )
        }

        composable("checkout") {
            RequireAuth(
                session = session,
                redirect = "checkout",
                onRequireAuth = requireLogin
            ) {
                PlaceholderScreen("El checkout se implementa en la próxima fase.")
            }
        }

        composable("orders") {
            RequireAuth(
                session = session,
                redirect = "orders",
                onRequireAuth = requireLogin
            ) {
                PlaceholderScreen("Tus órdenes estarán disponibles en la próxima fase.")
            }
        }

        composable("addresses") {
            RequireAuth(
                session = session,
                redirect = "addresses",
                onRequireAuth = requireLogin
            ) {
                PlaceholderScreen("Tus direcciones estarán disponibles en la próxima fase.")
            }
        }

        composable("payment-methods") {
            RequireAuth(
                session = session,
                redirect = "payment-methods",
                onRequireAuth = requireLogin
            ) {
                PlaceholderScreen("Tus métodos de pago estarán disponibles en la próxima fase.")
            }
        }

        composable("login") {
            AuthScreen(
                viewModel = authViewModel,
                mode = AuthMode.Login,
                onBack = {
                    authViewModel.clearRedirect()
                    navController.popBackStack()
                },
                onSuccess = {
                    navigateAfterAuth(navController, authViewModel.consumeRedirect())
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
                onBack = {
                    authViewModel.clearRedirect()
                    navController.popBackStack()
                },
                onSuccess = {
                    navigateAfterAuth(navController, authViewModel.consumeRedirect())
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

@Composable
private fun RequireAuth(
    session: Session?,
    redirect: String,
    onRequireAuth: (String) -> Unit,
    content: @Composable () -> Unit
) {
    if (session == null) {
        LaunchedEffect(redirect) {
            onRequireAuth(redirect)
        }
    } else {
        content()
    }
}

private fun navigateAfterAuth(navController: NavHostController, target: String?) {
    when {
        target == null -> navController.popBackStack()
        target == "catalog" -> {
            if (!navController.popBackStack("catalog", inclusive = false)) {
                navController.navigate("catalog") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        else -> navController.navigate(target) {
            popUpTo("login") { inclusive = true }
            launchSingleTop = true
        }
    }
}
