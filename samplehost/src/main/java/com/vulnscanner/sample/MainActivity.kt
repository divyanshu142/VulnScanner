package com.vulnscanner.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vulnscanner.sample.ui.screens.ScanScreen
import com.vulnscanner.sample.ui.theme.VulnScannerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity entry point.
 * @AndroidEntryPoint enables Hilt injection into this Activity.
 */

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VulnScannerTheme {
                ScanScreen()
            }
        }
    }
}
