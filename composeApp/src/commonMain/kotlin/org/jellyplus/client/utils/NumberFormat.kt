package org.jellyplus.client.utils

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Multiplatform-safe fixed-decimal formatting. `String.format` / `"%.1f"`
 * are JVM-only and fail on non-JVM targets (wasmJs, native), so use this
 * everywhere instead.
 */
fun Double.format(decimals: Int): String {
    if (decimals <= 0) return this.roundToLong().toString()
    val factor = 10.0.pow(decimals)
    val scaled = abs((this * factor).roundToLong())
    val digits = scaled.toString().padStart(decimals + 1, '0')
    val intPart = digits.dropLast(decimals)
    val fracPart = digits.takeLast(decimals)
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    return "$sign$intPart.$fracPart"
}

fun Float.format(decimals: Int): String = this.toDouble().format(decimals)
