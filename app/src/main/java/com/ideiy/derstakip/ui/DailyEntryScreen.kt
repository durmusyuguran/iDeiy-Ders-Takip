package com.ideiy.derstakip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ideiy.derstakip.data.DailyEntry
import com.ideiy.derstakip.data.DbProvider
import com.ideiy.derstakip.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun DailyEntryScreen() {
    val ctx = LocalContext.current
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()

    var date by remember { mutableStateOf(DateUtils.today()) }

    var turkce by remember { mutableStateOf("0") }
    var matematik by remember { mutableStateOf("0") }
    var sosyal by remember { mutableStateOf("0") }
    var fen by remember { mutableStateOf("0") }
    var din by remember { mutableStateOf("0") }
    var ingilizce by remember { mutableStateOf("0") }
    var readingPages by remember { mutableStateOf("0") }

    var info by remember { mutableStateOf<String?>(null) }

    fun toIntSafe(s: String): Int = s.toIntOrNull()?.coerceAtLeast(0) ?: 0

    suspend fun load(day: String) {
        val e = db.dailyDao().getByDate(day)
        turkce = (e?.turkce ?: 0).toString()
        matematik = (e?.matematik ?: 0).toString()
        sosyal = (e?.sosyal ?: 0).toString()
        fen = (e?.fen ?: 0).toString()
        din = (e?.din ?: 0).toString()
        ingilizce = (e?.ingilizce ?: 0).toString()
        readingPages = (e?.readingPages ?: 0).toString()
        info = null
    }

    LaunchedEffect(date) {
        load(date)
    }

    val totalQuestions = toIntSafe(turkce) + toIntSafe(matematik) + toIntSafe(sosyal) +
            toIntSafe(fen) + toIntSafe(din) + toIntSafe(ingilizce)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("iDeiy Ders Takip", style = MaterialTheme.typography.headlineSmall)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tarih: $date", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { date = DateUtils.addDays(date, -1) },
                        modifier = Modifier.weight(1f)
                    ) { Text("◀ Önceki") }
                    Button(
                        onClick = { date = DateUtils.today() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Bugün") }
                    Button(
                        onClick = { date = DateUtils.addDays(date, 1) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Sonraki ▶") }
                }
            }
        }

        NumberField("Türkçe", turkce) { turkce = it }
        NumberField("Matematik", matematik) { matematik = it }
        NumberField("Sosyal Bilgiler", sosyal) { sosyal = it }
        NumberField("Fen Bilgisi", fen) { fen = it }
        NumberField("Din Kültürü", din) { din = it }
        NumberField("İngilizce", ingilizce) { ingilizce = it }

        Divider()

        NumberField("Okunan Sayfa (hikâye/roman)", readingPages) { readingPages = it }

        Text("Toplam soru: $totalQuestions", style = MaterialTheme.typography.titleMedium)

        Button(
            onClick = {
                scope.launch {
                    db.dailyDao().upsert(
                        DailyEntry(
                            date = date,
                            turkce = toIntSafe(turkce),
                            matematik = toIntSafe(matematik),
                            sosyal = toIntSafe(sosyal),
                            fen = toIntSafe(fen),
                            din = toIntSafe(din),
                            ingilizce = toIntSafe(ingilizce),
                            readingPages = toIntSafe(readingPages)
                        )
                    )
                    info = "Kaydedildi ✅"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }

        info?.let { Text(it) }

        Spacer(Modifier.height(4.dp))
        Text(
            "İpucu: Tarihi değiştirmek için Önceki/Sonraki butonlarını kullanabilirsin.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            onChange(if (digits.isBlank()) "0" else digits)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
