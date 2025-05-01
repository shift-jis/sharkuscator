package dev.sharkuscator.obfuscator.dictionaries

object DictionaryFactory {
    private val defaultEmojiSet = setOf("\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE01", "\uD83D\uDE06", "\uD83D\uDE02")

    fun createDictionary(dictionaryType: String, dictionaryLength: Int = 20): MappingDictionary {
        return when (dictionaryType) {
            "space_varying_length" -> SpaceVaryingLengthDictionary()
            "similar_characters" -> SimilarCharacterDictionary(dictionaryLength)
            "zero_width_space" -> ZeroWidthSpaceDictionary()
            "alphabetical" -> AlphabeticalDictionary()

            "emoji_list" -> WordListDictionary(charset = defaultEmojiSet, length = dictionaryLength)
            "word_list" -> WordListDictionary(length = dictionaryLength)

            else -> defaultDictionary()
        }
    }

    fun defaultDictionary(): MappingDictionary {
        return AlphabeticalDictionary()
    }
}
