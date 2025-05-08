package dev.sharkuscator.obfuscator.dictionaries

abstract class MappingDictionary<T>(val name: String) {
    abstract fun generateNextName(element: T?): String

    abstract fun generatesUnsafeNames(): Boolean

    abstract fun resetNameGenerator()

    protected fun toBijective(charset: CharArray, decimal: Int): String {
        val builder = StringBuilder()
        var currentDecimal = decimal
        while (currentDecimal-- > 0) {
            builder.insert(0, charset[currentDecimal % charset.size])
            currentDecimal /= charset.size
        }
        return builder.toString()
    }
}
