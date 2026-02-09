package com.ideiy.derstakip.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ideiy.derstakip.data.DbProvider
import com.ideiy.derstakip.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun StatsScreen() {
    val ctx = LocalContext.current
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()

    var totalQuestions by remember { mutableStateOf(0) }
    var totalReading by remember { mutableStateOf(0) }
    var weeklyQuestions by remember { mutableStateOf(0) }
    var monthlyQuestions by remember { mutableStateOf(0) }

    var last14Questions by remember { mutableStateOf<List<Int>>(emptyList()) }
    var last14Reading by remember { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val all = db.dailyDao().getBetween("0000-01-01", "9999-12-31")
            totalQuestions = all.sumOf { it.totalQuestions }
            totalReading = all.sumOf { it.readingPages }

            val today = DateUtils.today()
            val weekStart = DateUtils.addDays(today, -6)
            weeklyQuestions = db.dailyDao().getBetween(weekStart, today).sumOf { it.totalQuestions }

            val monthStart = DateUtils.startOfMonth(today)
            val monthEnd = DateUtils.endOfMonth(today)
            monthlyQuestions = db.dailyDao().getBetween(monthStart, monthEnd).sumOf { it.totalQuestions }

            // Last 14 days series (oldest -> newest)
            val start14 = DateUtils.addDays(today, -13)
            val entries14 = db.dailyDao().getBetween(start14, today).associateBy { it.date }
            val q = mutableListOf<Int>()
            val r = mutableListOf<Int>()
            for (i in 0..13) {
                val d = DateUtils.addDays(start14, i.toLong())
                val e = entries14[d]
                q.add(e?.totalQuestions ?: 0)
                r.add(e?.readingPages ?: 0)
            }
            last14Questions = q
            last14Reading = r
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("İstatistik", style = MaterialTheme.typography.headlineSmall)

        StatCard("Toplam Çözülen Soru", totalQuestions.toString())
        StatCard("Toplam Okunan Sayfa", totalReading.toString())
        StatCard("Son 7 Gün Soru", weeklyQuestions.toString())
        StatCard("Bu Ay Soru", monthlyQuestions.toString())

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Son 14 Gün - Soru", style = MaterialTheme.typography.titleMedium)
                SimpleLineChart(values = last14Questions)
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Son 14 Gün - Okuma (sayfa)", style = MaterialTheme.typography.titleMedium)
                SimpleLineChart(values = last14Reading)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Not: Grafikler telefonda internet olmadan çalışır (veriler cihazda saklanır).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SimpleLineChart(values: List<Int>) {
    // Minimal, dependency-free chart.
    val maxV = (values.maxOrNull() ?: 0).coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height

        // axes
        drawLine(
            color = androidx.compose.ui.graphics.Color.Gray,
            start = Offset(0f, h),
            end = Offset(w, h),
            strokeWidth = 2f
        )
        drawLine(
            color = androidx.compose.ui.graphics.Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(0f, h),
            strokeWidth = 2f
        )

        if (values.isEmpty()) return@Canvas

        val stepX = if (values.size == 1) 0f else w / (values.size - 1)

        val path = Path()
        values.forEachIndexed { i, v ->
            val x = stepX * i
            val y = h - (v.toFloat() / maxV) * (h * 0.9f) // leave some top padding
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            // point
            drawCircle(
                color = androidx.compose.ui.graphics.Color.Black,
                radius = 4f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.Black,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}
