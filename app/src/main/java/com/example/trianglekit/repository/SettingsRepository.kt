package com.example.trianglekit.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.trianglekit.model.AreaUnit
import com.example.trianglekit.model.LengthUnit
import com.example.trianglekit.model.SolveType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.RoundingMode

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val decimalPlaces: Int = 6,
    val roundingMode: RoundingMode = RoundingMode.HALF_UP,
    val showFormula: Boolean = true,
    val showCalculationSteps: Boolean = true,
    val showDiagram: Boolean = true,
    val showClassification: Boolean = true,
    val confirmDelete: Boolean = true,
    val defaultLengthUnit: LengthUnit = LengthUnit.CENTIMETER,
    val defaultAreaUnit: AreaUnit = AreaUnit.SQUARE_CENTIMETER
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("trianglekit_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try { AppTheme.valueOf(themeStr) } catch (e: Exception) { AppTheme.SYSTEM }

        val roundingStr = prefs.getString(KEY_ROUNDING_MODE, RoundingMode.HALF_UP.name) ?: RoundingMode.HALF_UP.name
        val roundingMode = try { RoundingMode.valueOf(roundingStr) } catch (e: Exception) { RoundingMode.HALF_UP }

        val lengthUnitStr = prefs.getString(KEY_DEFAULT_LENGTH_UNIT, LengthUnit.CENTIMETER.name) ?: LengthUnit.CENTIMETER.name
        val defaultLengthUnit = try { LengthUnit.valueOf(lengthUnitStr) } catch (e: Exception) { LengthUnit.CENTIMETER }

        val areaUnitStr = prefs.getString(KEY_DEFAULT_AREA_UNIT, AreaUnit.SQUARE_CENTIMETER.name) ?: AreaUnit.SQUARE_CENTIMETER.name
        val defaultAreaUnit = try { AreaUnit.valueOf(areaUnitStr) } catch (e: Exception) { AreaUnit.SQUARE_CENTIMETER }

        return AppSettings(
            theme = theme,
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            decimalPlaces = prefs.getInt(KEY_DECIMAL_PLACES, 6),
            roundingMode = roundingMode,
            showFormula = prefs.getBoolean(KEY_SHOW_FORMULA, true),
            showCalculationSteps = prefs.getBoolean(KEY_SHOW_CALC_STEPS, true),
            showDiagram = prefs.getBoolean(KEY_SHOW_DIAGRAM, true),
            showClassification = prefs.getBoolean(KEY_SHOW_CLASSIFICATION, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            defaultLengthUnit = defaultLengthUnit,
            defaultAreaUnit = defaultAreaUnit
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, newSettings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, newSettings.dynamicColor)
            .putBoolean(KEY_HAPTIC_FEEDBACK, newSettings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, newSettings.animations)
            .putInt(KEY_DECIMAL_PLACES, newSettings.decimalPlaces)
            .putString(KEY_ROUNDING_MODE, newSettings.roundingMode.name)
            .putBoolean(KEY_SHOW_FORMULA, newSettings.showFormula)
            .putBoolean(KEY_SHOW_CALC_STEPS, newSettings.showCalculationSteps)
            .putBoolean(KEY_SHOW_DIAGRAM, newSettings.showDiagram)
            .putBoolean(KEY_SHOW_CLASSIFICATION, newSettings.showClassification)
            .putBoolean(KEY_CONFIRM_DELETE, newSettings.confirmDelete)
            .putString(KEY_DEFAULT_LENGTH_UNIT, newSettings.defaultLengthUnit.name)
            .putString(KEY_DEFAULT_AREA_UNIT, newSettings.defaultAreaUnit.name)
            .apply()

        _settings.value = newSettings
    }

    fun resetToDefaults() {
        updateSettings(AppSettings())
    }

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_ROUNDING_MODE = "rounding_mode"
        private const val KEY_SHOW_FORMULA = "show_formula"
        private const val KEY_SHOW_CALC_STEPS = "show_calc_steps"
        private const val KEY_SHOW_DIAGRAM = "show_diagram"
        private const val KEY_SHOW_CLASSIFICATION = "show_classification"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_DEFAULT_LENGTH_UNIT = "default_length_unit"
        private const val KEY_DEFAULT_AREA_UNIT = "default_area_unit"
    }
}
