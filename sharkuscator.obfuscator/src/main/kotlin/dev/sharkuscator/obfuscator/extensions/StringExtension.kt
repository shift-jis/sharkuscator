package dev.sharkuscator.obfuscator.extensions

private val CAMEL_CASE_REGEX = "(?<=.)(?=\\p{Upper})".toRegex()

fun String.asSnakeCase() = replace(CAMEL_CASE_REGEX, "_").lowercase()

fun String.toUnicodeEscapes(): String {
    return this.map { char -> "\\u%04x".format(char.code) }.joinToString("")
}

infix fun String.xor(that: String): String {
    return this.zip(that) { char1, char2 -> char1.code.xor(char2.code).toChar() }.joinToString("")
}
