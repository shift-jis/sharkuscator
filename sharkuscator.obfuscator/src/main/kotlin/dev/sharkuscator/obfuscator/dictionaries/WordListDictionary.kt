package dev.sharkuscator.obfuscator.dictionaries

class WordListDictionary(
    private val generationStyle: GenerationStyle = GenerationStyle.WordConcatenation,
    private val charset: Set<String> = setOf("focus", "smile", "fun", "cook", "nice"),
    private val length: Int
) : MappingDictionary("WordList") {

    override fun nextString(): String {
        return when (generationStyle) {
            GenerationStyle.CharacterRepeat -> (0..length).joinToString("") { charset.joinToString("") { it.repeat((1..4).random()) } }
            GenerationStyle.WordConcatenation -> (0..length).joinToString("") { charset.random() }
        }
    }

    override fun isDangerous(): Boolean {
        return false
    }

    enum class GenerationStyle {
        CharacterRepeat, WordConcatenation
    }
}
