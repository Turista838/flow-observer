package dev.goncaloramalho.flowobserver.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Flow Observer Demo")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            enabled = !uiState.loading && !uiState.loggedIn,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = viewModel::login,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.username.isNotBlank() && !uiState.loading && !uiState.loggedIn,
        ) {
            Text(if (uiState.loading) "Logging in…" else "Login")
        }

        if (uiState.loggedIn) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Logged in as ${uiState.username}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = viewModel::reset) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Check Logcat for FlowObserver output",
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
