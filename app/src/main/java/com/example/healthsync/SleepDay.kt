package com.example.healthsync
import java.util.Calendar

data class SleepDay(
    val dayOfWeek: Int,          // Calendar.MONDAY etc.
    val name: String,
    var bedMinutes: Int? = null,  // minutes since midnight, null = not set
    var wakeMinutes: Int? = null,
    var bedAlarm: Boolean = false,
    var wakeAlarm: Boolean = false
)

val WEEK = listOf(
    Calendar.MONDAY to "Monday",
    Calendar.TUESDAY to "Tuesday",
    Calendar.WEDNESDAY to "Wednesday",
    Calendar.THURSDAY to "Thursday",
    Calendar.FRIDAY to "Friday",
    Calendar.SATURDAY to "Saturday",
    Calendar.SUNDAY to "Sunday"
)