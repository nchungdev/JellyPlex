package org.jellyplex.client.utils

import kotlin.random.Random

fun generateUuid(): String {
    val chars = "0123456789abcdef"
    return buildString {
        repeat(8) { append(chars[Random.nextInt(chars.length)]) }
        append("-")
        repeat(4) { append(chars[Random.nextInt(chars.length)]) }
        append("-4") // Version 4
        repeat(3) { append(chars[Random.nextInt(chars.length)]) }
        append("-")
        append(chars[8 + Random.nextInt(4)]) // Variant
        repeat(3) { append(chars[Random.nextInt(chars.length)]) }
        append("-")
        repeat(12) { append(chars[Random.nextInt(chars.length)]) }
    }
}
