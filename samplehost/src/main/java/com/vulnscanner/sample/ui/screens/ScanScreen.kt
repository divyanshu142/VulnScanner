package com.vulnscanner.sample.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
//import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vulnscanner.analyzer.data.model.ScanState
import com.vulnscanner.analyzer.presentation.RiskGaugeView
import com.vulnscanner.sample.ui.components.StatsRow
import com.vulnscanner.sample.ui.components.VulnerableAppCard
import com.vulnscanner.sample.ui.theme.*
import com.vulnscanner.sample.ui.viewmodel.ScanViewModel

/**
 * Main screen of the sample host app.
 *
 * State machine:
 *   Idle     → Shows hero card + "Start Scan" button
 *   Scanning → Linear progress bar + current app name
 *   Success  → RiskGaugeView + stats + scrollable LazyColumn of VulnerableAppCards
 *   Error    → Error message + retry button
 */

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val expandedPackages by viewModel.expandedPackages.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .systemBarsPadding()
    ) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        TopBar(
            showReset = scanState is ScanState.Success,
            onReset = viewModel::resetScan
        )

        // ── Body ─────────────────────────────────────────────────────────────
        when (val state = scanState) {
            is ScanState.Idle    -> IdleContent(onStartScan = viewModel::startScan)
            is ScanState.Scanning -> ScanningContent(state)
            is ScanState.Success  -> ResultContent(
                state = state,
                expandedPackages = expandedPackages,
                onToggle = viewModel::toggleExpanded
            )
            is ScanState.Error   -> ErrorContent(
                message = state.message,
                onRetry = viewModel::startScan
            )
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(showReset: Boolean, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "VulnScanner",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Android Component Analyzer",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
        if (showReset) {
            IconButton(onClick = onReset) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextSecondary
                )
            }
        }
    }
}

// ─── Idle ─────────────────────────────────────────────────────────────────────

@Composable
private fun IdleContent(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative shield icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(AccentCyan.copy(alpha = 0.08f), shape = RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(52.dp)
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Security Audit",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Scan all installed apps for exported\ncomponents without permission protection.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Start Scan",
                color = BackgroundDeep,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Checks Activities, Services & BroadcastReceivers",
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Scanning ────────────────────────────────────────────────────────────────

@Composable
private fun ScanningContent(state: ScanState.Scanning) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier.size(80.dp),
            color = AccentCyan,
            strokeWidth = 6.dp,
            trackColor = BackgroundSurface
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Scanning… ${state.progress}%",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.currentApp,
            color = TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(Modifier.height(24.dp))
        LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = AccentCyan,
            trackColor = BackgroundSurface
        )
    }
}

// ─── Results ─────────────────────────────────────────────────────────────────

@Composable
private fun ResultContent(
    state: ScanState.Success,
    expandedPackages: Set<String>,
    onToggle: (String) -> Unit
) {
    val result = state.result

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Gauge
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                RiskGaugeView(score = result.riskScore)
            }
        }

        // Stats row
        item {
            StatsRow(
                totalScanned = result.totalAppsScanned,
                vulnerableApps = result.vulnerableApps.size,
                totalVulns = result.totalVulnerabilities
            )
        }

        // Section header
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Vulnerable Apps (${result.vulnerableApps.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (result.vulnerableApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 No vulnerable apps found!",
                        color = ColorGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            items(
                items = result.vulnerableApps,
                key = { it.packageName }
            ) { app ->
                VulnerableAppCard(
                    app = app,
                    isExpanded = app.packageName in expandedPackages,
                    onToggle = { onToggle(app.packageName) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─── Error ───────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️ Scan Failed", color = ColorRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(text = message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ColorOrange)) {
            Text("Retry", color = BackgroundDeep, fontWeight = FontWeight.Bold)
        }
    }
}
