package dev.sharkuscator.obfuscator.dictionaries

class ZeroWidthSpaceDictionary<T>(private val prefix: String = "dummy") : MappingDictionary<T>("ZeroWidthSpace") {
    private val generatedStrings = mutableSetOf<String>()

    override fun generateNextName(element: T?): String {
        while (true) {
            val separator = "\u200B".repeat((0..50).random())
            val nextString = prefix.toCharArray().joinToString(separator)
            if (generatedStrings.add(nextString)) {
                return nextString
            }
        }
    }

    override fun generatesUnsafeNames(): Boolean {
        return false
    }

    override fun resetNameGenerator() {
        generatedStrings.clear()
    }
}
