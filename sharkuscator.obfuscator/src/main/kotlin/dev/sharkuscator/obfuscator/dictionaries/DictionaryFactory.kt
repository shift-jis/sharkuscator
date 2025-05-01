package dev.sharkuscator.obfuscator.dictionaries

object DictionaryFactory {
    private val defaultEmojiSet = setOf("\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE01", "\uD83D\uDE06", "\uD83D\uDE02")

    fun forName(name: String, length: Int = 20): MappingDictionary {
        return when (name) {
            "space_varying_length" -> SpaceVaryingLengthDictionary()
            "similar_characters" -> SimilarCharacterDictionary(length)
            "alphabetical" -> AlphabeticalDictionary()

            "emoji_list" -> WordListDictionary(charset = defaultEmojiSet, length = length)
            "word_list" -> WordListDictionary(length = length)

            else -> defaultDictionary()
        }
    }

    fun defaultDictionary(): MappingDictionary {
        return AlphabeticalDictionary()
    }
}
