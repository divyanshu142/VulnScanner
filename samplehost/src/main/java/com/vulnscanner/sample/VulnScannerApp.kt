package com.vulnscanner.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt entry point — triggers component generation at compile time.
 * Must be declared in AndroidManifest android:name=".VulnScannerApp"
 */
@HiltAndroidApp
class VulnScannerApp : Application()
