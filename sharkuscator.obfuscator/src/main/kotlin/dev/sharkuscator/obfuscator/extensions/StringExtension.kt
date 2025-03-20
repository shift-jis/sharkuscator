package dev.sharkuscator.obfuscator.extensions

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()

fun String.toSnakeCase() = replace(humps, "_").lowercase()
