package com.vulnscanner.analyzer.data.model

/**
 * Represents a single exported component (Activity/Service/Receiver)
 * that has no permission protection — making it potentially vulnerable.
 */
data class VulnerableComponent(
    val name: String,
    val type: ComponentType,
    val isExported: Boolean,
    val permission: String? = null   // null means NO protection
)

enum class ComponentType(val label: String) {
    ACTIVITY("Activity"),
    SERVICE("Service"),
    RECEIVER("BroadcastReceiver")
}

/**
 * Aggregated result for one installed application.
 */
data class VulnerableApp(
    val packageName: String,
    val appName: String,
    val vulnerableComponents: List<VulnerableComponent>
) {
    val totalVulnerabilities: Int get() = vulnerableComponents.size
}

/**
 * Full scan result — list of apps + computed risk score.
 */
data class ScanResult(
    val vulnerableApps: List<VulnerableApp>,
    val riskScore: Int,           // 0–100
    val totalAppsScanned: Int,
    val totalVulnerabilities: Int
)

/** Risk level derived from score */
enum class RiskLevel(val label: String) {
    LOW("Low Risk"),
    MEDIUM("Medium Risk"),
    HIGH("High Risk"),
    CRITICAL("Critical Risk");

    companion object {
        fun fromScore(score: Int): RiskLevel = when (score) {
            in 0..25   -> LOW
            in 26..50  -> MEDIUM
            in 51..75  -> HIGH
            else       -> CRITICAL
        }
    }
}

/** Sealed class for reactive stream states */
sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val progress: Int, val currentApp: String) : ScanState()
    data class Success(val result: ScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}
