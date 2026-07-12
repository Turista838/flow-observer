package dev.goncaloramalho.flowobserver.sample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.goncaloramalho.flowobserver.sample.login.LoginScreen
import dev.goncaloramalho.flowobserver.sample.playground.PlaygroundScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    var username by rememberSaveable { mutableStateOf<String?>(null) }

    if (username == null) {
        LoginScreen(
            modifier = modifier,
            onLoginSuccess = { loggedInUsername -> username = loggedInUsername },
        )
    } else {
        PlaygroundScreen(
            modifier = modifier,
            username = username!!,
            onLogout = { username = null },
        )
    }
}
