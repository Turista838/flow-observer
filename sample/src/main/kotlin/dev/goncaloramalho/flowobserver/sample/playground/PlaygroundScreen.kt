package dev.goncaloramalho.flowobserver.sample.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlaygroundScreen(
    username: String,
    onLogout: () -> Unit,
    onOpenScreenScopedVm: () -> Unit,
    onOpenWhileSubscribed: () -> Unit,
    viewModel: PlaygroundViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var lastTick by remember { mutableStateOf<Long?>(null) }
    var lastPulse by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.ticks.collect { lastTick = it }
    }
    LaunchedEffect(viewModel) {
        viewModel.pulses.collect { lastPulse = it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Playground")
        Text(text = "Logged in as $username")

        FilledTonalButton(onClick = onOpenScreenScopedVm, modifier = Modifier.fillMaxWidth()) {
            Text("Open scoped-VM screen")
        }
        FilledTonalButton(onClick = onOpenWhileSubscribed, modifier = Modifier.fillMaxWidth()) {
            Text("Open WhileSubscribed screen")
        }

        HorizontalDivider()

        Text(text = "StateFlow")
        Text(text = "Counter: ${uiState.counter}")
        Text(text = "Ticker: ${if (uiState.isTickerRunning) "running" else "stopped"}")
        Text(text = "Status: ${uiState.statusMessage}")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = viewModel::decrement, modifier = Modifier.weight(1f)) {
                Text("−1")
            }
            OutlinedButton(onClick = viewModel::increment, modifier = Modifier.weight(1f)) {
                Text("+1")
            }
            OutlinedButton(onClick = viewModel::resetCounter, modifier = Modifier.weight(1f)) {
                Text("Reset")
            }
        }

        Button(onClick = viewModel::simulateLoad, modifier = Modifier.fillMaxWidth()) {
            Text("Simulate load")
        }

        HorizontalDivider()

        Text(text = "SharedFlow — ticks")
        Text(text = "Last tick: ${lastTick?.toString() ?: "—"}")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = viewModel::startTicker,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isTickerRunning,
            ) {
                Text("Start ticker")
            }
            Button(
                onClick = viewModel::stopTicker,
                modifier = Modifier.weight(1f),
                enabled = uiState.isTickerRunning,
            ) {
                Text("Stop ticker")
            }
        }

        HorizontalDivider()

        Text(text = "SharedFlow — pulses")
        Text(text = "Last pulse: ${lastPulse ?: "—"}")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = viewModel::sendPulse, modifier = Modifier.weight(1f)) {
                Text("Send pulse")
            }
            Button(onClick = viewModel::burstPulses, modifier = Modifier.weight(1f)) {
                Text("Burst ×3")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}
