package com.ideiy.derstakip.data

import java.time.LocalDate

object DateUtils {
    fun today(): String = LocalDate.now().toString()

    fun lastNDaysRange(days: Int): Pair<String, String> {
        val end = LocalDate.now()
        val start = end.minusDays((days - 1).toLong())
        return start.toString() to end.toString()
    }
}
