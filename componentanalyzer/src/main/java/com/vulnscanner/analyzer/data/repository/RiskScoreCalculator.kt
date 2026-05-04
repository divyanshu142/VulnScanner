package com.vulnscanner.analyzer.data.repository

import com.vulnscanner.analyzer.data.model.ComponentType
import com.vulnscanner.analyzer.data.model.VulnerableApp
import kotlin.math.min

/**
 * Calculates a 0–100 risk score from vulnerability data.
 *
 * Scoring logic (weighted):
 *   - Each vulnerable Activity  → +6 points  (high impact: can launch UI)
 *   - Each vulnerable Service   → +8 points  (high impact: background execution)
 *   - Each vulnerable Receiver  → +4 points  (medium impact: broadcast interception)
 *
 * Score is capped at 100.
 */
internal object RiskScoreCalculator {

    private const val ACTIVITY_WEIGHT = 6
    private const val SERVICE_WEIGHT = 8
    private const val RECEIVER_WEIGHT = 4

    fun calculate(vulnerableApps: List<VulnerableApp>): Int {
        var raw = 0
        vulnerableApps.forEach { app ->
            app.vulnerableComponents.forEach { component ->
                raw += when (component.type) {
                    ComponentType.ACTIVITY -> ACTIVITY_WEIGHT
                    ComponentType.SERVICE  -> SERVICE_WEIGHT
                    ComponentType.RECEIVER -> RECEIVER_WEIGHT
                }
            }
        }
        return min(100, raw)
    }
}
