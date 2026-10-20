package com.example.trianglekit.ui.settings

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trianglekit.repository.AppTheme
import com.example.trianglekit.ui.TriangleKitViewModel
import java.math.RoundingMode

@Composable
fun SettingsScreen(
    viewModel: TriangleKitViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val view = LocalView.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        if (settings.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Customize calculation precision and interface.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance Section
            SettingsSectionTitle("APPEARANCE")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Theme selector
                    DropdownSettingRow(
                        title = "Theme",
                        subtitle = settings.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                        options = AppTheme.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        selectedIndex = AppTheme.entries.indexOf(settings.theme),
                        onSelect = { idx ->
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(theme = AppTheme.entries[idx]))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    // Dynamic Color
                    SwitchSettingRow(
                        title = "Dynamic Color",
                        subtitle = "Match system Material You palette",
                        checked = settings.dynamicColor,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(dynamicColor = it))
                        }
                    )
                }
            }

            // Behavior Section
            SettingsSectionTitle("BEHAVIOR")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SwitchSettingRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile response on taps and calculations",
                        checked = settings.hapticFeedback,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(hapticFeedback = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchSettingRow(
                        title = "Animations",
                        subtitle = "Smooth mode and result transitions",
                        checked = settings.animations,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(animations = it))
                        }
                    )
                }
            }

            // Number Display Section
            SettingsSectionTitle("NUMBER DISPLAY")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val decimalOptions = listOf(0, 1, 2, 3, 4, 6, 8, 10, 12)
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DropdownSettingRow(
                        title = "Decimal Places",
                        subtitle = "${settings.decimalPlaces} decimal places",
                        options = decimalOptions.map { "$it places" },
                        selectedIndex = decimalOptions.indexOf(settings.decimalPlaces).coerceAtLeast(0),
                        onSelect = { idx ->
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(decimalPlaces = decimalOptions[idx]))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownSettingRow(
                        title = "Rounding Mode",
                        subtitle = settings.roundingMode.name,
                        options = RoundingMode.entries.map { it.name },
                        selectedIndex = RoundingMode.entries.indexOf(settings.roundingMode).coerceAtLeast(0),
                        onSelect = { idx ->
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(roundingMode = RoundingMode.entries[idx]))
                        }
                    )
                }
            }

            // Display Features Section
            SettingsSectionTitle("DISPLAY")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SwitchSettingRow(
                        title = "Show Formulas",
                        subtitle = "Formulas used for solving mode",
                        checked = settings.showFormula,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(showFormula = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchSettingRow(
                        title = "Show Calculation Steps",
                        subtitle = "Domain mathematical step breakdown",
                        checked = settings.showCalculationSteps,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(showCalculationSteps = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchSettingRow(
                        title = "Show Triangle Diagram",
                        subtitle = "Proportional vector triangle diagram",
                        checked = settings.showDiagram,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(showDiagram = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SwitchSettingRow(
                        title = "Show Classification",
                        subtitle = "Triangle classification by sides and angles",
                        checked = settings.showClassification,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(showClassification = it))
                        }
                    )
                }
            }

            // History & Reset Section
            SettingsSectionTitle("HISTORY & DATA")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SwitchSettingRow(
                        title = "Confirm Delete",
                        subtitle = "Ask before deleting history items",
                        checked = settings.confirmDelete,
                        onCheckedChange = {
                            triggerHaptic()
                            viewModel.updateSettings(settings.copy(confirmDelete = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                triggerHaptic()
                                showClearHistoryDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear History", color = MaterialTheme.colorScheme.error)
                        }

                        OutlinedButton(
                            onClick = {
                                triggerHaptic()
                                showResetSettingsDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Settings")
                        }
                    }
                }
            }
        }
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all saved triangle calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all application settings to defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DropdownSettingRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = options.getOrNull(selectedIndex) ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { idx, opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}
