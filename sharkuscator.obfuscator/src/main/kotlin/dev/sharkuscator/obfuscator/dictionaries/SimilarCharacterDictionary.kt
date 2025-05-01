package dev.sharkuscator.obfuscator.dictionaries

class SimilarCharacterDictionary(private val length: Int) : MappingDictionary("SimilarCharacter") {
    private val generatedStrings = mutableSetOf<String>()
    private val charset = "Il".toCharArray()

    override fun nextString(): String {
        while (true) {
            val nextString = (0..length).map { charset.random() }.joinToString("")
            if (generatedStrings.add(nextString)) {
                return nextString
            }
        }
    }

    override fun isDangerous(): Boolean {
        return false
    }
}
