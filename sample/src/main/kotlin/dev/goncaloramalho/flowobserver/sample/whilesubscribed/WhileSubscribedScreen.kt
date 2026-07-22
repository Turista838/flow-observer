package dev.goncaloramalho.flowobserver.sample.whilesubscribed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WhileSubscribedScreen(
    onBack: () -> Unit,
    viewModel: WhileSubscribedViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    var uiCollecting by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "WhileSubscribed")
        Text(
            text = "Cold upstream → stateIn(WhileSubscribed(2s)) + FlowObserver. " +
                "Turn off UI collection and watch Logcat: ${WhileSubscribedViewModel.UPSTREAM_TAG}",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "UI collecting")
            Switch(
                checked = uiCollecting,
                onCheckedChange = { uiCollecting = it },
            )
        }

        if (uiCollecting) {
            WhileSubscribedCollectingContent(viewModel = viewModel)
        } else {
            Text(text = "UI not collecting. If upstream ticks continue, the observer is keeping WhileSubscribed alive.")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to playground")
        }
    }
}

@Composable
private fun WhileSubscribedCollectingContent(
    viewModel: WhileSubscribedViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Text(text = "Tick: ${uiState.tick}")
}
