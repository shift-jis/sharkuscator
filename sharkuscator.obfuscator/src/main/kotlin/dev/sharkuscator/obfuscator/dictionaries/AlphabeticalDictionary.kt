package dev.sharkuscator.obfuscator.dictionaries

import java.util.Arrays

class AlphabeticalDictionary<T> : MappingDictionary<T>("Alphabetical") {
    private val alphabetCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private val elementIndexCache = mutableMapOf<T, CharArray>()
    private var nameSequenceCounter = 1

    override fun generateNextName(element: T?): String {
        return if (element == null) {
            toBijective(alphabetCharacters, nameSequenceCounter++)
        } else {
            val uniqueCharsForElement = elementIndexCache.getOrPut(element) { CharArray(0) }
            for (index in uniqueCharsForElement.size - 1 downTo 0) {
                if (uniqueCharsForElement[index] < 'z') { // Simplified condition
                    uniqueCharsForElement[index]++
                    return String(uniqueCharsForElement)
                }
                uniqueCharsForElement[index] = 'a'
            }

            elementIndexCache[element] = CharArray(uniqueCharsForElement.size + 1).apply {
                Arrays.fill(this, 'a')
            }

            String(elementIndexCache[element]!!)
        }
    }

    override fun generatesUnsafeNames(): Boolean {
        return false
    }

    override fun resetNameGenerator() {
        elementIndexCache.clear()
        nameSequenceCounter = 1
    }
}
