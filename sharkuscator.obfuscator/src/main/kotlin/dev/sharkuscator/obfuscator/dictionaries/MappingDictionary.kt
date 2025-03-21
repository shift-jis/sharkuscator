package dev.sharkuscator.obfuscator.dictionaries

interface MappingDictionary {
    fun nextString(): String

    fun toBijective(charset: CharArray, _decimal: Int): String {
        val builder = StringBuilder()
        var decimal = _decimal
        while (decimal-- > 0) {
            builder.insert(0, charset[decimal % charset.size])
            decimal /= charset.size
        }
        return builder.toString()
    }
}
