package com.ideiy.derstakip.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val fmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(fmt)

    fun addDays(date: String, days: Long): String {
        return LocalDate.parse(date, fmt).plusDays(days).format(fmt)
    }

    fun startOfMonth(date: String): String {
        val d = LocalDate.parse(date, fmt)
        return d.withDayOfMonth(1).format(fmt)
    }

    fun endOfMonth(date: String): String {
        val d = LocalDate.parse(date, fmt)
        return d.withDayOfMonth(d.lengthOfMonth()).format(fmt)
    }
}
