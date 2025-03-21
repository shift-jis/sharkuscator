package dev.sharkuscator.obfuscator.dictionaries

abstract class MappingDictionary(private val name: String) {
    abstract fun nextString(): String

    protected fun toBijective(charset: CharArray, _decimal: Int): String {
        val builder = StringBuilder()
        var decimal = _decimal
        while (decimal-- > 0) {
            builder.insert(0, charset[decimal % charset.size])
            decimal /= charset.size
        }
        return builder.toString()
    }
}
