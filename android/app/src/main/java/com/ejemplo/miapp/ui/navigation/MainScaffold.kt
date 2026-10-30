package com.ejemplo.miapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ejemplo.miapp.data.model.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    session: Session?,
    cartCount: Int,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val isAuthRoute = route == "login" || route == "register"
    val isProductDetail = route == "product/{productId}"

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    fun navigate(target: String) {
        closeDrawer()
        navController.navigate(target) { launchSingleTop = true }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthRoute,
        drawerContent = {
            AppDrawer(
                session = session,
                currentRoute = route,
                cartCount = cartCount,
                onNavigate = { navigate(it) },
                onLogin = {
                    closeDrawer()
                    onLogin()
                },
                onRegister = {
                    closeDrawer()
                    onRegister()
                },
                onLogout = {
                    closeDrawer()
                    onLogout()
                    navController.navigate("catalog") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (!isAuthRoute) {
                    TopAppBar(
                        title = {
                            Text(
                                text = titleForRoute(route),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            if (isProductDetail) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menú"
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { navigate("cart") }) {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge { Text(cartCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Carrito"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            },
            contentWindowInsets = if (isAuthRoute) {
                WindowInsets(0, 0, 0, 0)
            } else {
                WindowInsets.navigationBars
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun AppDrawer(
    session: Session?,
    currentRoute: String?,
    cartCount: Int,
    onNavigate: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "dopaPC",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (session == null) {
                Text(
                    text = "Invitado",
                    fontSize = 14.sp,
                    color = Color(0xFF6b6375)
                )
            } else {
                Text(
                    text = session.user.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF08060d)
                )
                Text(
                    text = session.user.email,
                    fontSize = 12.sp,
                    color = Color(0xFF6b6375)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFe5e4e7))

        DrawerItem(
            label = "Catálogo",
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            selected = currentRoute == "catalog",
            onClick = { onNavigate("catalog") }
        )
        DrawerItem(
            label = if (cartCount > 0) "Carrito ($cartCount)" else "Carrito",
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            selected = currentRoute == "cart",
            onClick = { onNavigate("cart") }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color(0xFFe5e4e7)
        )

        if (session == null) {
            DrawerItem(
                label = "Iniciar sesión",
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                selected = false,
                onClick = onLogin
            )
            DrawerItem(
                label = "Registrarse",
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                selected = false,
                onClick = onRegister
            )
        } else {
            DrawerItem(
                label = "Mis órdenes",
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                selected = currentRoute == "orders",
                onClick = { onNavigate("orders") }
            )
            DrawerItem(
                label = "Direcciones",
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                selected = currentRoute == "addresses",
                onClick = { onNavigate("addresses") }
            )
            DrawerItem(
                label = "Métodos de pago",
                icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                selected = currentRoute == "payment-methods",
                onClick = { onNavigate("payment-methods") }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFe5e4e7)
            )
            DrawerItem(
                label = "Cerrar sesión",
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                selected = false,
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = icon,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

private fun titleForRoute(route: String?): String = when (route) {
    "cart" -> "Carrito"
    "checkout" -> "Checkout"
    "orders" -> "Mis órdenes"
    "addresses" -> "Direcciones"
    "payment-methods" -> "Métodos de pago"
    "product/{productId}" -> "Detalle de producto"
    else -> "dopaPC"
}
