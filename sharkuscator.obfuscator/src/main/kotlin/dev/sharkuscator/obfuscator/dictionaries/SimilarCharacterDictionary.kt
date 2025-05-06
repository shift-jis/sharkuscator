package dev.sharkuscator.obfuscator.dictionaries

class SimilarCharacterDictionary(private val length: Int) : MappingDictionary("SimilarCharacter") {
    private val generatedStrings = mutableSetOf<String>()
    private val charset = "Il".toCharArray()

    override fun generateNextName(): String {
        while (true) {
            val nextString = (0..length).map { charset.random() }.joinToString("")
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
