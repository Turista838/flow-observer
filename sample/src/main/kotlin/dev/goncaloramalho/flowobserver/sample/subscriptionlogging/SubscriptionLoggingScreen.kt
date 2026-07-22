package dev.goncaloramalho.flowobserver.sample.subscriptionlogging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import dev.goncaloramalho.flowobserver.FlowObserver

@Composable
fun SubscriptionLoggingScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionLoggingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    var uiCollecting by rememberSaveable { mutableStateOf(true) }
    var logOnlyWhenSubscribed by rememberSaveable {
        mutableStateOf(FlowObserver.settings.logOnlyWhenSubscribed)
    }

    val defaultCount by viewModel.defaultSubscriptionCount.collectAsStateWithLifecycle()
    val alwaysCount by viewModel.alwaysSubscriptionCount.collectAsStateWithLifecycle()
    val enabled = FlowObserver.settings.enabled

    val defaultWouldLog = enabled && (!logOnlyWhenSubscribed || defaultCount > 0)
    val alwaysWouldLog = enabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Subscription logging")
        Text(text = "enabled=$enabled · logOnlyWhenSubscribed=$logOnlyWhenSubscribed")

        SettingSwitch(
            label = "logOnlyWhenSubscribed",
            checked = logOnlyWhenSubscribed,
            onCheckedChange = { checked ->
                logOnlyWhenSubscribed = checked
                FlowObserver.configure(
                    FlowObserver.settings.copy(logOnlyWhenSubscribed = checked),
                )
            },
        )
        SettingSwitch(
            label = "UI collecting",
            checked = uiCollecting,
            onCheckedChange = { uiCollecting = it },
        )

        HorizontalDivider()

        if (uiCollecting) {
            CollectingStatus(
                viewModel = viewModel,
                defaultCount = defaultCount,
                alwaysCount = alwaysCount,
                defaultWouldLog = defaultWouldLog,
                alwaysWouldLog = alwaysWouldLog,
            )
        } else {
            FlowStatusRow(
                tag = SubscriptionLoggingViewModel.DEFAULT_TAG,
                value = viewModel.defaultState.value,
                subscribers = defaultCount,
                willLog = defaultWouldLog,
            )
            FlowStatusRow(
                tag = SubscriptionLoggingViewModel.ALWAYS_TAG,
                value = viewModel.alwaysState.value,
                subscribers = alwaysCount,
                willLog = alwaysWouldLog,
            )
        }

        Button(onClick = viewModel::bumpDefault, modifier = Modifier.fillMaxWidth()) {
            Text("Bump Default")
        }
        Button(onClick = viewModel::bumpAlways, modifier = Modifier.fillMaxWidth()) {
            Text("Bump Always")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun CollectingStatus(
    viewModel: SubscriptionLoggingViewModel,
    defaultCount: Int,
    alwaysCount: Int,
    defaultWouldLog: Boolean,
    alwaysWouldLog: Boolean,
) {
    val defaultValue by viewModel.defaultState.collectAsStateWithLifecycle()
    val alwaysValue by viewModel.alwaysState.collectAsStateWithLifecycle()
    FlowStatusRow(
        tag = SubscriptionLoggingViewModel.DEFAULT_TAG,
        value = defaultValue,
        subscribers = defaultCount,
        willLog = defaultWouldLog,
    )
    FlowStatusRow(
        tag = SubscriptionLoggingViewModel.ALWAYS_TAG,
        value = alwaysValue,
        subscribers = alwaysCount,
        willLog = alwaysWouldLog,
    )
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FlowStatusRow(
    tag: String,
    value: Int,
    subscribers: Int,
    willLog: Boolean,
) {
    Text(text = "$tag  v=$value  subs=$subscribers  log=${if (willLog) "YES" else "NO"}")
}
