package dev.sharkuscator.obfuscator.dictionaries

class SimilarDictionary(private val length: Int) : MappingDictionary("Similar") {
    private val charset = "Il".toCharArray()

    override fun nextString(): String {
        return (0..length).map { charset.random() }.joinToString("")
    }
}
