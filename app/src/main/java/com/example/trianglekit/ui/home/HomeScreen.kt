package com.example.trianglekit.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trianglekit.domain.TriangleClassifier
import com.example.trianglekit.domain.TriangleFormatter
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.SolveType
import com.example.trianglekit.model.TriangleResult
import com.example.trianglekit.ui.TriangleKitViewModel
import com.example.trianglekit.ui.components.NumericUnitInputField
import com.example.trianglekit.ui.components.SolveModeSelector
import com.example.trianglekit.ui.components.TriangleDiagram
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: TriangleKitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    fun triggerHaptic(feedbackConstant: Int = HapticFeedbackConstants.KEYBOARD_TAP) {
        if (settings.hapticFeedback) {
            view.performHapticFeedback(feedbackConstant)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "TriangleKit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calculate triangle sides, area, height, and angles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Solve Mode Selector
            Text(
                text = "SOLVE MODE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            SolveModeSelector(
                selectedType = uiState.solveType,
                onSelectType = { mode ->
                    triggerHaptic()
                    viewModel.setSolveType(mode)
                }
            )

            // Result Units Bar
            ResultUnitsSelectorRow(
                currentLengthUnit = uiState.lengthUnit,
                currentAreaUnit = uiState.areaUnit,
                onLengthUnitChange = {
                    triggerHaptic()
                    viewModel.setResultLengthUnit(it)
                },
                onAreaUnitChange = {
                    triggerHaptic()
                    viewModel.setResultAreaUnit(it)
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Dynamic Inputs Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "KNOWN VALUES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    RenderInputsForMode(
                        solveType = uiState.solveType,
                        inputs = uiState.inputs,
                        inputUnits = uiState.inputUnits,
                        defaultLengthUnit = uiState.lengthUnit,
                        onInputChange = { k, v -> viewModel.updateInput(k, v) },
                        onUnitChange = { k, u -> viewModel.updateInputUnit(k, u) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                triggerHaptic()
                                viewModel.solve()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "SOLVE TRIANGLE",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                triggerHaptic()
                                viewModel.reset()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset inputs")
                        }
                    }
                }
            }

            // Error Banner
            uiState.errorMessage?.let { error ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Note (e.g., for Base + Height)
            uiState.result?.note?.let { note ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = note,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Results Section
            val result = uiState.result
            if (result != null && result.solutions.isNotEmpty()) {
                val solutions = result.solutions
                val selectedIndex = uiState.selectedSolutionIndex.coerceIn(0, solutions.lastIndex)
                val currentSolution = solutions[selectedIndex]

                // Multiple solutions tab selector (Ambiguous SSA case)
                if (solutions.size > 1) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        TabRow(selectedTabIndex = selectedIndex) {
                            solutions.indices.forEach { idx ->
                                Tab(
                                    selected = selectedIndex == idx,
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.setSelectedSolutionIndex(idx)
                                    },
                                    text = { Text("Solution ${idx + 1}", fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Triangle solution ${idx + 1} of ${solutions.size}"
                                    }
                                )
                            }
                        }
                    }
                }

                // Action Bar (Copy / Share / Save)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            triggerHaptic()
                            val shareText = TriangleFormatter.formatShareText(
                                currentSolution,
                                lengthUnit = uiState.lengthUnit,
                                areaUnit = uiState.areaUnit,
                                decimalPlaces = settings.decimalPlaces,
                                roundingMode = settings.roundingMode
                            )
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("TriangleKit Result", shareText))
                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard.") }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Copy triangle calculation" }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Copy")
                    }

                    OutlinedButton(
                        onClick = {
                            triggerHaptic()
                            val shareText = TriangleFormatter.formatShareText(
                                currentSolution,
                                lengthUnit = uiState.lengthUnit,
                                areaUnit = uiState.areaUnit,
                                decimalPlaces = settings.decimalPlaces,
                                roundingMode = settings.roundingMode
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Triangle Result"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = {
                            triggerHaptic()
                            viewModel.saveCurrentCalculation()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                }

                // Triangle Diagram
                if (settings.showDiagram && uiState.solveType != SolveType.BASE_HEIGHT) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "DIAGRAM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TriangleDiagram(result = currentSolution)
                        }
                    }
                }

                // Triangle Result Card (Sides, Angles, Area)
                ResultCard(
                    solution = currentSolution,
                    lengthUnit = uiState.lengthUnit,
                    areaUnit = uiState.areaUnit,
                    decimalPlaces = settings.decimalPlaces,
                    roundingMode = settings.roundingMode,
                    isBaseHeight = uiState.solveType == SolveType.BASE_HEIGHT
                )

                // Heights Card
                if (uiState.solveType != SolveType.BASE_HEIGHT) {
                    HeightsCard(
                        solution = currentSolution,
                        lengthUnit = uiState.lengthUnit,
                        decimalPlaces = settings.decimalPlaces,
                        roundingMode = settings.roundingMode
                    )
                }

                // Properties Card
                if (settings.showClassification && uiState.solveType != SolveType.BASE_HEIGHT) {
                    PropertiesCard(
                        solution = currentSolution,
                        lengthUnit = uiState.lengthUnit,
                        decimalPlaces = settings.decimalPlaces,
                        roundingMode = settings.roundingMode
                    )
                }

                // Formulas Card
                if (settings.showFormula && result.formulas.isNotEmpty()) {
                    FormulasCard(formulas = result.formulas)
                }

                // Calculation Steps Card
                if (settings.showCalculationSteps && currentSolution.steps.isNotEmpty()) {
                    CalculationStepsCard(steps = currentSolution.steps)
                }
            }
        }
    }
}

@Composable
private fun RenderInputsForMode(
    solveType: SolveType,
    inputs: Map<String, String>,
    inputUnits: Map<String, LengthUnit>,
    defaultLengthUnit: LengthUnit,
    onInputChange: (String, String) -> Unit,
    onUnitChange: (String, LengthUnit) -> Unit
) {
    when (solveType) {
        SolveType.SSS -> {
            NumericUnitInputField(
                label = "SIDE A",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) },
                contentDesc = "Side A value"
            )
            NumericUnitInputField(
                label = "SIDE B",
                value = inputs["b"] ?: "",
                onValueChange = { onInputChange("b", it) },
                unit = inputUnits["b"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("b", it) },
                contentDesc = "Side B value"
            )
            NumericUnitInputField(
                label = "SIDE C",
                value = inputs["c"] ?: "",
                onValueChange = { onInputChange("c", it) },
                unit = inputUnits["c"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("c", it) },
                contentDesc = "Side C value"
            )
        }
        SolveType.SAS -> {
            NumericUnitInputField(
                label = "SIDE A",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) },
                contentDesc = "Side A value"
            )
            NumericUnitInputField(
                label = "SIDE B",
                value = inputs["b"] ?: "",
                onValueChange = { onInputChange("b", it) },
                unit = inputUnits["b"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("b", it) },
                contentDesc = "Side B value"
            )
            NumericUnitInputField(
                label = "INCLUDED ANGLE C",
                value = inputs["angleC"] ?: "",
                onValueChange = { onInputChange("angleC", it) },
                isAngle = true,
                contentDesc = "Included Angle C value in degrees"
            )
        }
        SolveType.ASA -> {
            NumericUnitInputField(
                label = "ANGLE A",
                value = inputs["angleA"] ?: "",
                onValueChange = { onInputChange("angleA", it) },
                isAngle = true,
                contentDesc = "Angle A value in degrees"
            )
            NumericUnitInputField(
                label = "ANGLE B",
                value = inputs["angleB"] ?: "",
                onValueChange = { onInputChange("angleB", it) },
                isAngle = true,
                contentDesc = "Angle B value in degrees"
            )
            NumericUnitInputField(
                label = "INCLUDED SIDE C",
                value = inputs["c"] ?: "",
                onValueChange = { onInputChange("c", it) },
                unit = inputUnits["c"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("c", it) },
                contentDesc = "Included Side C value"
            )
        }
        SolveType.AAS -> {
            NumericUnitInputField(
                label = "ANGLE A",
                value = inputs["angleA"] ?: "",
                onValueChange = { onInputChange("angleA", it) },
                isAngle = true,
                contentDesc = "Angle A value in degrees"
            )
            NumericUnitInputField(
                label = "ANGLE B",
                value = inputs["angleB"] ?: "",
                onValueChange = { onInputChange("angleB", it) },
                isAngle = true,
                contentDesc = "Angle B value in degrees"
            )
            NumericUnitInputField(
                label = "KNOWN SIDE (a)",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) },
                contentDesc = "Known Side value"
            )
        }
        SolveType.SSA -> {
            NumericUnitInputField(
                label = "SIDE A",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) },
                contentDesc = "Side A value"
            )
            NumericUnitInputField(
                label = "SIDE B",
                value = inputs["b"] ?: "",
                onValueChange = { onInputChange("b", it) },
                unit = inputUnits["b"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("b", it) },
                contentDesc = "Side B value"
            )
            NumericUnitInputField(
                label = "NON-INCLUDED ANGLE A",
                value = inputs["angleA"] ?: "",
                onValueChange = { onInputChange("angleA", it) },
                isAngle = true,
                contentDesc = "Non-Included Angle A value in degrees"
            )
        }
        SolveType.RIGHT_TRIANGLE -> {
            Text(
                text = "Enter any 2 known values (e.g. Leg a + Leg b, or Leg + Hypotenuse, or Side + Angle):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NumericUnitInputField(
                label = "LEG A",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) }
            )
            NumericUnitInputField(
                label = "LEG B",
                value = inputs["b"] ?: "",
                onValueChange = { onInputChange("b", it) },
                unit = inputUnits["b"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("b", it) }
            )
            NumericUnitInputField(
                label = "HYPOTENUSE C",
                value = inputs["c"] ?: "",
                onValueChange = { onInputChange("c", it) },
                unit = inputUnits["c"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("c", it) }
            )
            NumericUnitInputField(
                label = "ACUTE ANGLE A",
                value = inputs["angleA"] ?: "",
                onValueChange = { onInputChange("angleA", it) },
                isAngle = true
            )
            NumericUnitInputField(
                label = "ACUTE ANGLE B",
                value = inputs["angleB"] ?: "",
                onValueChange = { onInputChange("angleB", it) },
                isAngle = true
            )
        }
        SolveType.BASE_HEIGHT -> {
            NumericUnitInputField(
                label = "BASE",
                value = inputs["base"] ?: "",
                onValueChange = { onInputChange("base", it) },
                unit = inputUnits["base"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("base", it) }
            )
            NumericUnitInputField(
                label = "HEIGHT",
                value = inputs["height"] ?: "",
                onValueChange = { onInputChange("height", it) },
                unit = inputUnits["height"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("height", it) }
            )
        }
        SolveType.AUTO -> {
            Text(
                text = "Enter known sides and/or angles. Empty fields are allowed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NumericUnitInputField(
                label = "SIDE A",
                value = inputs["a"] ?: "",
                onValueChange = { onInputChange("a", it) },
                unit = inputUnits["a"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("a", it) }
            )
            NumericUnitInputField(
                label = "SIDE B",
                value = inputs["b"] ?: "",
                onValueChange = { onInputChange("b", it) },
                unit = inputUnits["b"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("b", it) }
            )
            NumericUnitInputField(
                label = "SIDE C",
                value = inputs["c"] ?: "",
                onValueChange = { onInputChange("c", it) },
                unit = inputUnits["c"] ?: defaultLengthUnit,
                onUnitChange = { onUnitChange("c", it) }
            )
            NumericUnitInputField(
                label = "ANGLE A",
                value = inputs["angleA"] ?: "",
                onValueChange = { onInputChange("angleA", it) },
                isAngle = true
            )
            NumericUnitInputField(
                label = "ANGLE B",
                value = inputs["angleB"] ?: "",
                onValueChange = { onInputChange("angleB", it) },
                isAngle = true
            )
            NumericUnitInputField(
                label = "ANGLE C",
                value = inputs["angleC"] ?: "",
                onValueChange = { onInputChange("angleC", it) },
                isAngle = true
            )
        }
    }
}

@Composable
private fun ResultUnitsSelectorRow(
    currentLengthUnit: LengthUnit,
    currentAreaUnit: AreaUnit,
    onLengthUnitChange: (LengthUnit) -> Unit,
    onAreaUnitChange: (AreaUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var lengthExpanded by remember { mutableStateOf(false) }
    var areaExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Length Unit
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { lengthExpanded = true }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Length Unit: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentLengthUnit.displayName} (${currentLengthUnit.symbol})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = lengthExpanded,
                onDismissRequest = { lengthExpanded = false }
            ) {
                LengthUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text("${unit.displayName} (${unit.symbol})") },
                        onClick = {
                            onLengthUnitChange(unit)
                            lengthExpanded = false
                        }
                    )
                }
            }
        }

        // Area Unit
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { areaExpanded = true }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Area Unit: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentAreaUnit.symbol}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = areaExpanded,
                onDismissRequest = { areaExpanded = false }
            ) {
                AreaUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text("${unit.displayName} (${unit.symbol})") },
                        onClick = {
                            onAreaUnitChange(unit)
                            areaExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    solution: TriangleResult,
    lengthUnit: LengthUnit,
    areaUnit: AreaUnit,
    decimalPlaces: Int,
    roundingMode: java.math.RoundingMode,
    isBaseHeight: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TRIANGLE RESULT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(10.dp))

            if (!isBaseHeight) {
                // Sides
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ResultValueColumn("Side a", TriangleFormatter.formatLength(solution.sideA, lengthUnit, decimalPlaces, roundingMode))
                    ResultValueColumn("Side b", TriangleFormatter.formatLength(solution.sideB, lengthUnit, decimalPlaces, roundingMode))
                    ResultValueColumn("Side c", TriangleFormatter.formatLength(solution.sideC, lengthUnit, decimalPlaces, roundingMode))
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                // Angles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ResultValueColumn("Angle A", TriangleFormatter.formatAngle(solution.angleA, decimalPlaces, roundingMode))
                    ResultValueColumn("Angle B", TriangleFormatter.formatAngle(solution.angleB, decimalPlaces, roundingMode))
                    ResultValueColumn("Angle C", TriangleFormatter.formatAngle(solution.angleC, decimalPlaces, roundingMode))
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))
            }

            // Area
            val areaFormatted = TriangleFormatter.formatArea(solution.area, areaUnit, decimalPlaces, roundingMode)
            Column(
                modifier = Modifier.semantics {
                    contentDescription = "Triangle area, $areaFormatted"
                }
            ) {
                Text(
                    text = "AREA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = areaFormatted,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ResultValueColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HeightsCard(
    solution: TriangleResult,
    lengthUnit: LengthUnit,
    decimalPlaces: Int,
    roundingMode: java.math.RoundingMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HEIGHTS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            ResultRow("Height to side a (hₐ)", TriangleFormatter.formatLength(solution.heightA, lengthUnit, decimalPlaces, roundingMode))
            ResultRow("Height to side b (hᵦ)", TriangleFormatter.formatLength(solution.heightB, lengthUnit, decimalPlaces, roundingMode))
            ResultRow("Height to side c (h𝑐)", TriangleFormatter.formatLength(solution.heightC, lengthUnit, decimalPlaces, roundingMode))
        }
    }
}

@Composable
private fun PropertiesCard(
    solution: TriangleResult,
    lengthUnit: LengthUnit,
    decimalPlaces: Int,
    roundingMode: java.math.RoundingMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PROPERTIES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            ResultRow("Perimeter", TriangleFormatter.formatLength(solution.perimeter, lengthUnit, decimalPlaces, roundingMode))
            ResultRow("Semi-perimeter", TriangleFormatter.formatLength(solution.semiPerimeter, lengthUnit, decimalPlaces, roundingMode))
            ResultRow("Type by sides", solution.sideClassification.displayName)
            ResultRow("Type by angles", "${solution.angleClassification.displayName} Triangle")
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FormulasCard(formulas: List<String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FORMULAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            formulas.forEach { formula ->
                Text(
                    text = "• $formula",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun CalculationStepsCard(steps: List<com.example.trianglekit.model.CalculationStep>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SHOW CALCULATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse calculation steps" else "Expand calculation steps"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    steps.forEach { step ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = step.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = step.expression,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "= ${step.result}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
