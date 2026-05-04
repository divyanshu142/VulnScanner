package com.vulnscanner.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vulnscanner.sample.ui.theme.*

@Composable
fun StatsRow(
    totalScanned: Int,
    vulnerableApps: Int,
    totalVulns: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(label = "Apps Scanned", value = "$totalScanned", color = AccentCyan, modifier = Modifier.weight(1f))
        StatCard(label = "Vulnerable Apps", value = "$vulnerableApps", color = ColorOrange, modifier = Modifier.weight(1f))
        StatCard(label = "Total Issues", value = "$totalVulns", color = ColorRed, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(BackgroundCard, shape = RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}
