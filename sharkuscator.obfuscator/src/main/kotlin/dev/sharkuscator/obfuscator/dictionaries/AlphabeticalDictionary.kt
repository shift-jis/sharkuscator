package dev.sharkuscator.obfuscator.dictionaries

class AlphabeticalDictionary<T> : MappingDictionary<T>("Alphabetical") {
    private val alphabetCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private val elementIndexCache = mutableMapOf<T, Int>()
    private var nameSequenceCounter = 1

    override fun generateNextName(element: T?): String {
        if (element == null) {
            return toBijective(alphabetCharacters, nameSequenceCounter++)
        }

        val uniqueIndexForElement = elementIndexCache.getOrPut(element) { 1 }
        elementIndexCache[element] = uniqueIndexForElement + 1

        return toBijective(alphabetCharacters, uniqueIndexForElement)
    }

    override fun generatesUnsafeNames(): Boolean {
        return false
    }

    override fun resetNameGenerator() {
        elementIndexCache.clear()
        nameSequenceCounter = 1
    }
}
