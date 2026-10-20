package com.example.trianglekit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trianglekit.domain.TriangleValidator
import com.example.trianglekit.model.LengthUnit

@Composable
fun NumericUnitInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    unit: LengthUnit? = null,
    onUnitChange: ((LengthUnit) -> Unit)? = null,
    isAngle: Boolean = false,
    contentDesc: String = label
) {
    var expanded by remember { mutableStateOf(false) }
    val isOverLimit = value.length > TriangleValidator.MAX_INPUT_DIGITS

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input)
        },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        isError = isOverLimit,
        supportingText = if (isOverLimit) {
            { Text("Number is too long. Maximum 500 digits.") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDesc },
        trailingIcon = {
            if (isAngle) {
                Text(
                    text = "°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else if (unit != null && onUnitChange != null) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .semantics { contentDescription = "$label unit, ${unit.displayName}" }
                    ) {
                        Text(
                            text = unit.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select unit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LengthUnit.entries.forEach { availableUnit ->
                            DropdownMenuItem(
                                text = {
                                    Text("${availableUnit.displayName} (${availableUnit.symbol})")
                                },
                                onClick = {
                                    onUnitChange(availableUnit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
