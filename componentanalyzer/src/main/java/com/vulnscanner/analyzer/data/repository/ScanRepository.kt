package com.vulnscanner.analyzer.data.repository

import android.content.pm.PackageManager
import com.vulnscanner.analyzer.data.model.ScanResult
import com.vulnscanner.analyzer.data.model.ScanState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository that orchestrates the full scan pipeline.
 *
 * Returns a cold Flow<ScanState> so the caller (ViewModel) stays reactive:
 *   Idle → Scanning(progress, currentApp) → ... → Success(ScanResult)
 *
 * All heavy work is pushed to Dispatchers.IO via flowOn() — the UI thread
 * is never blocked.
 */
class ScanRepository(private val packageManager: PackageManager) {

    private val analyzer = ComponentAnalyzer(packageManager)

    /**
     * Emits real-time scan progress then final result.
     * flowOn(Dispatchers.IO) ensures this runs off the main thread.
     */
    fun startScan(): Flow<ScanState> = flow {
        emit(ScanState.Scanning(progress = 0, currentApp = "Initializing…"))

        val packages = analyzer.getInstalledUserApps()
        val total = packages.size.coerceAtLeast(1)
        val vulnerableApps = mutableListOf<com.vulnscanner.analyzer.data.model.VulnerableApp>()

        packages.forEachIndexed { index, packageName ->
            val progress = ((index + 1) * 100) / total
            emit(ScanState.Scanning(progress = progress, currentApp = packageName))

            analyzer.analyzePackage(packageName)?.let { vulnerableApps.add(it) }
        }

        val riskScore = RiskScoreCalculator.calculate(vulnerableApps)
        val totalVulns = vulnerableApps.sumOf { it.totalVulnerabilities }

        emit(
            ScanState.Success(
                ScanResult(
                    vulnerableApps = vulnerableApps.sortedByDescending { it.totalVulnerabilities },
                    riskScore = riskScore,
                    totalAppsScanned = packages.size,
                    totalVulnerabilities = totalVulns
                )
            )
        )
    }
        .flowOn(Dispatchers.IO) // ← Off main thread; UI never blocks
}
