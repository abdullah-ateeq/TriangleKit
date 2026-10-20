package com.example.trianglekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.trianglekit.ui.TriangleKitViewModel
import com.example.trianglekit.ui.navigation.TriangleKitApp
import com.example.trianglekit.ui.theme.TriangleKitTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TriangleKitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            TriangleKitTheme(
                appTheme = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                TriangleKitApp(viewModel = viewModel)
            }
        }
    }
}
