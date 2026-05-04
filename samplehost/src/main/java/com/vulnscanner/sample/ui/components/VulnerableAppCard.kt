package com.vulnscanner.sample.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vulnscanner.analyzer.data.model.ComponentType
import com.vulnscanner.analyzer.data.model.VulnerableApp
import com.vulnscanner.analyzer.data.model.VulnerableComponent
import com.vulnscanner.sample.ui.theme.*
import androidx.compose.material.icons.filled.*
/**
 * Expandable card showing one vulnerable app and its components.
 * Tap header → expand/collapse component list (grouped by type).
 */
@Composable
fun VulnerableAppCard(
    app: VulnerableApp,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(0.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = app.packageName,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))

                // Vuln count badge
                Box(
                    modifier = Modifier
                        .background(
                            color = severityColor(app.totalVulnerabilities).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${app.totalVulnerabilities} vuln${if (app.totalVulnerabilities != 1) "s" else ""}",
                        color = severityColor(app.totalVulnerabilities),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Expandable Component List ────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    Spacer(Modifier.height(8.dp))

                    // Group by type
                    ComponentType.entries.forEach { type ->
                        val components = app.vulnerableComponents.filter { it.type == type }
                        if (components.isNotEmpty()) {
                            ComponentGroup(type = type, components = components)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentGroup(type: ComponentType, components: List<VulnerableComponent>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = iconFor(type),
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = type.label,
            color = AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "(${components.size})",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
    Spacer(Modifier.height(4.dp))
    components.forEach { component ->
        Row(
            modifier = Modifier.padding(start = 20.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(ColorOrange, shape = RoundedCornerShape(50))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = component.name,
                color = TextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "no permission",
                color = ColorRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun severityColor(count: Int): Color = when {
    count >= 5 -> ColorRed
    count >= 3 -> ColorOrange
    count >= 1 -> ColorYellow
    else       -> ColorGreen
}

private fun iconFor(type: ComponentType): ImageVector = when (type) {
    ComponentType.ACTIVITY -> Icons.Default.Phone  // Core icon (no extended lib needed)
    ComponentType.SERVICE  -> Icons.Default.Build       // Core icon
    ComponentType.RECEIVER -> Icons.Default.Notifications // Core icon
}
