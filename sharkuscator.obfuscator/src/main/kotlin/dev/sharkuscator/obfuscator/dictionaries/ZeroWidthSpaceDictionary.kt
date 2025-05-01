package dev.sharkuscator.obfuscator.dictionaries

class ZeroWidthSpaceDictionary(private val prefix: String = "dummy") : MappingDictionary("ZeroWidthSpace") {
    private val generatedStrings = mutableSetOf<String>()

    override fun nextString(): String {
        while (true) {
            val separator = "\u200B".repeat((0..50).random())
            val nextString = prefix.toCharArray().joinToString(separator)
            if (generatedStrings.add(nextString)) {
                return nextString
            }
        }
    }

    override fun isDangerous(): Boolean {
        return false
    }
}
