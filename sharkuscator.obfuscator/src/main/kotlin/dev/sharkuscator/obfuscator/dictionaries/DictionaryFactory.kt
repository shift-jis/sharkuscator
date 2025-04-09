package dev.sharkuscator.obfuscator.dictionaries

object DictionaryFactory {
    fun forName(name: String, length: Int = 20): MappingDictionary {
        return when (name) {
            "alphabet" -> AlphabetDictionary()
            "similar" -> SimilarDictionary(length)
            else -> defaultDictionary()
        }
    }

    fun defaultDictionary(): MappingDictionary {
        return AlphabetDictionary()
    }
}
