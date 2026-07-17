package dev.goncaloramalho.flowobserver.sample.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.goncaloramalho.flowobserver.sample.login.LoginScreen
import dev.goncaloramalho.flowobserver.sample.playground.PlaygroundScreen
import dev.goncaloramalho.flowobserver.sample.playground.PlaygroundViewModel
import dev.goncaloramalho.flowobserver.sample.screenscoped.ScreenScopedVmScreen

private object Routes {
    const val PLAYGROUND = "playground"
    const val SCREEN_SCOPED_VM = "screen_scoped_vm"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    var username by rememberSaveable { mutableStateOf<String?>(null) }

    if (username == null) {
        LoginScreen(
            modifier = modifier,
            onLoginSuccess = { loggedInUsername -> username = loggedInUsername },
        )
        return
    }

    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    // Keep playground Activity-scoped so state survives opening the screen-scoped destination.
    val playgroundViewModel: PlaygroundViewModel = viewModel(viewModelStoreOwner = activity)

    NavHost(
        navController = navController,
        startDestination = Routes.PLAYGROUND,
        modifier = modifier,
    ) {
        composable(Routes.PLAYGROUND) {
            PlaygroundScreen(
                username = username!!,
                viewModel = playgroundViewModel,
                onOpenScreenScopedVm = { navController.navigate(Routes.SCREEN_SCOPED_VM) },
                onLogout = { username = null },
            )
        }
        composable(Routes.SCREEN_SCOPED_VM) {
            // Default viewModel() uses this NavBackStackEntry as owner → screen-scoped.
            ScreenScopedVmScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
