package dev.sharkuscator.obfuscator.dictionaries

class CjkUnifiedIdeographDictionary<T> : MappingDictionary<T>("CjkUnifiedIdeograph") {
    private val cjkIdeographCharacters = (0x4e00..0x9faf).map { it.toChar() }.toCharArray()
    private val elementIndexCache = mutableMapOf<T, Int>()
    private var nameSequenceCounter = 1

    override fun generateNextName(element: T?): String {
        if (element == null) {
            return toBijective(cjkIdeographCharacters, nameSequenceCounter++)
        }

        val uniqueIndexForElement = elementIndexCache.getOrPut(element) { 1 }
        elementIndexCache[element] = uniqueIndexForElement + 1

        return toBijective(cjkIdeographCharacters, uniqueIndexForElement)
    }

    override fun generatesUnsafeNames(): Boolean {
        return false
    }

    override fun resetNameGenerator() {
        elementIndexCache.clear()
        nameSequenceCounter = 1
    }
}
