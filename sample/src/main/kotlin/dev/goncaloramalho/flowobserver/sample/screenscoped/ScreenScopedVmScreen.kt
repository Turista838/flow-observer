package dev.goncaloramalho.flowobserver.sample.screenscoped

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScreenScopedVmScreen(
    onBack: () -> Unit,
    viewModel: ScreenScopedViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Screen-scoped ViewModel")
        Text(
            text = "This destination owns its own ViewModelStore. " +
                "Leaving pops the entry and clears the VM; coming back creates a new instance.",
        )
        Text(
            text = "Observation starts in ViewModel init via attachFlowObserver(), so this " +
                "Nav-scoped instance is logged. Filter Logcat: ScreenScopedViewModel.uiState",
        )

        HorizontalDivider()

        Text(text = "Taps: ${uiState.taps}")
        Text(text = uiState.label)
        Text(text = "hashCode: ${viewModel.hashCode()}")

        Button(onClick = viewModel::tap, modifier = Modifier.fillMaxWidth()) {
            Text("Tap (emit state change)")
        }
        OutlinedButton(onClick = viewModel::reset, modifier = Modifier.fillMaxWidth()) {
            Text("Reset")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to playground")
        }
    }
}
