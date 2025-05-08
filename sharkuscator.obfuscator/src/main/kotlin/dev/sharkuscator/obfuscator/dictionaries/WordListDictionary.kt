package dev.sharkuscator.obfuscator.dictionaries

class WordListDictionary<T>(
    private val generationStyle: GenerationStyle = GenerationStyle.WordConcatenation,
    private val sourceWordSet: Set<String>,
    private val segmentSeparator: String = "",
    private val namePrefix: String = "",
    private val numberOfSegments: Int
) : MappingDictionary<T>("WordList") {
    override fun generateNextName(element: T?): String {
        return "$namePrefix${
            when (generationStyle) {
                GenerationStyle.CharacterRepeat -> (0..numberOfSegments).joinToString(segmentSeparator) { sourceWordSet.joinToString("") { it.repeat((1..4).random()) } }
                GenerationStyle.WordConcatenation -> (0..numberOfSegments).joinToString(segmentSeparator) { sourceWordSet.random() }
            }
        }"
    }

    override fun generatesUnsafeNames(): Boolean {
        return segmentSeparator.contains("\\s".toRegex())
    }

    override fun resetNameGenerator() {
    }

    enum class GenerationStyle {
        CharacterRepeat, WordConcatenation
    }
}
