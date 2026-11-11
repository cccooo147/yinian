package com.yinian.app.ui.components

import com.yinian.app.data.Step
import com.yinian.app.data.StepStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthDay = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)

fun todayLine(): String {
    val today = LocalDate.now()
    val weekday = when (today.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }
    return "${today.format(monthDay)} · $weekday"
}

fun greetingFor(hour: Int): String = when {
    hour < 5 -> "现在是凌晨"
    hour < 11 -> "早上好"
    hour < 13 -> "中午好"
    hour < 18 -> "下午好"
    else -> "晚上好"
}

fun statsLine(groupCount: Int, ideaCount: Int): String =
    "共 $groupCount 个组，$ideaCount 个想法"

fun ideaShareText(title: String, steps: List<Step>): String {
    val body = steps.joinToString("\n") { step ->
        val mark = when (step.status) {
            StepStatus.DONE -> "✓ 已实现"
            StepStatus.DOING -> "▸ 正在做"
            StepStatus.WAITING -> "… 等待机会"
            StepStatus.FAILED -> "× 失败"
            StepStatus.THINKING -> "○ 还在考虑中"
        }
        val label = step.customStatus?.takeIf { it.isNotBlank() } ?: mark
        "$label  ${step.text}"
    }
    return buildString {
        append("$title\n")
        if (body.isNotBlank()) append("\n$body\n")
        append("\n—— 来自「一念」")
    }
}
