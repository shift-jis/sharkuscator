package dev.sharkuscator.obfuscator.extensions

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()

fun String.toSnakeCase() = replace(humps, "_").lowercase()

fun String.unicodify(): String {
    return toCharArray().joinToString("") { "\\u${String.format("%04x", it.code)}" }
}

infix fun String.xor(that: String) = mapIndexed { index, char ->
    that[index].code.xor(char.code)
}.joinToString(separator = "") {
    it.toChar().toString()
}
