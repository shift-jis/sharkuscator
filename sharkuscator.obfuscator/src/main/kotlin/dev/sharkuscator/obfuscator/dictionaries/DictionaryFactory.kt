package dev.sharkuscator.obfuscator.dictionaries

object DictionaryFactory {
    private val defaultSampleInvalidSet = setOf(
        "0", "1", "new", "this", "throw", "static", "try", "continue", "`", "instanceof", "const", "transient", "finally",
        "catch", "return", "class", "void", "while", "for", "private", "public", "extends", "throws"
    )
    private val defaultSampleEmojiSet = setOf("\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE01", "\uD83D\uDE06", "\uD83D\uDE02")
    private val defaultSampleWordSet = setOf("focus", "smile", "fun", "cook", "nice")

    fun <T> createDictionary(dictionaryType: String, dictionaryLength: Int = 5): MappingDictionary<T> {
        return when (dictionaryType) {
            "cjk_unified_ideograph" -> CjkUnifiedIdeographDictionary()
            "space_varying_length" -> SpaceVaryingLengthDictionary()
            "similar_characters" -> SimilarCharacterDictionary(dictionaryLength)
            "zero_width_space" -> ZeroWidthSpaceDictionary()
            "alphabetical" -> AlphabeticalDictionary()

            "sample_invalids" -> WordListDictionary(sourceWordSet = defaultSampleInvalidSet, numberOfSegments = dictionaryLength, segmentSeparator = " ")
            "sample_emojis" -> WordListDictionary(sourceWordSet = defaultSampleEmojiSet, numberOfSegments = dictionaryLength)
            "sample_words" -> WordListDictionary(sourceWordSet = defaultSampleWordSet, numberOfSegments = dictionaryLength)

            else -> createDefaultDictionary()
        }
    }

    fun <T> createDefaultDictionary(): MappingDictionary<T> {
        return AlphabeticalDictionary()
    }
}
