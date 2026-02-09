package com.ideiy.derstakip.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ideiy.derstakip.data.DbProvider
import com.ideiy.derstakip.data.TimetableSlot
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

private val dayLabels = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma")

private val defaultSubjects = listOf(
    "Boş",
    "Türkçe",
    "Matematik",
    "Sosyal Bilgiler",
    "Fen Bilgisi",
    "Din Kültürü",
    "İngilizce"
)

@Composable
fun TimetableScreen() {
    val ctx = LocalContext.current
    val db = remember { DbProvider.get(ctx) }
    val scope = rememberCoroutineScope()

    // grid[lessonIndex-1][dayIndex] = subject
    val grid = remember {
        mutableStateListOf<MutableList<String>>().apply {
            repeat(6) { add(MutableList(5) { "Boş" }) }
        }
    }

    var info by remember { mutableStateOf<String?>(null) }

    // Collect as state manually (without adding another dependency)
    var loadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(loadedOnce) {
        if (loadedOnce) return@LaunchedEffect
        loadedOnce = true
        scope.launch {
            // There is no direct "getAllOnce" in DAO; use observeAll and collect once.
            db.timetableDao().observeAll().collect { slots ->
                // reset
                for (l in 0 until 6) for (d in 0 until 5) grid[l][d] = "Boş"
                slots.forEach { s ->
                    val l = s.lessonIndex - 1
                    val d = s.dayOfWeek - 1
                    if (l in 0..5 && d in 0..4) {
                        grid[l][d] = if (s.subject.isBlank()) "Boş" else s.subject
                    }
                }
                // keep collecting so UI updates if changed
            }
        }
    }

    val scroll = rememberScrollState()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ders Programı (Hafta içi 5 gün / günde 6 ders)", style = MaterialTheme.typography.headlineSmall)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Hücrelere ders seç. Sonra Kaydet'e bas.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(Modifier.horizontalScroll(scroll), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Header row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ders", modifier = Modifier.padding(top = 14.dp))
                        dayLabels.forEach { Text(it, modifier = Modifier.padding(top = 14.dp)) }
                    }

                    // 6 lessons
                    for (lesson in 1..6) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$lesson", modifier = Modifier.padding(top = 14.dp))
                            for (dayIndex in 0 until 5) {
                                SubjectDropdown(
                                    value = grid[lesson - 1][dayIndex],
                                    onValue = { grid[lesson - 1][dayIndex] = it }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        scope.launch {
                            db.timetableDao().clearAll()
                            for (lesson in 1..6) {
                                for (day in 1..5) {
                                    val subject = grid[lesson - 1][day - 1]
                                    if (subject != "Boş" && subject.isNotBlank()) {
                                        db.timetableDao().upsert(
                                            TimetableSlot(
                                                dayOfWeek = day,
                                                lessonIndex = lesson,
                                                subject = subject
                                            )
                                        )
                                    }
                                }
                            }
                            info = "Kaydedildi ✅"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Kaydet") }

                Button(
                    onClick = {
                        for (l in 0 until 6) for (d in 0 until 5) grid[l][d] = "Boş"
                        info = "Temizlendi"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Temizle") }

                info?.let { Text(it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(value: String, onValue: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValue(it.ifBlank { "Boş" }) },
            label = { Text("Ders") },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            defaultSubjects.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        onValue(s)
                        expanded = false
                    }
                )
            }
        }
    }
}
