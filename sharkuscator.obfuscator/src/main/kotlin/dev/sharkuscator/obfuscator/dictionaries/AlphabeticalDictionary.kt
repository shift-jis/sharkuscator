package dev.sharkuscator.obfuscator.dictionaries

class AlphabeticalDictionary : MappingDictionary("Alphabetical") {
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private var index = 1

    override fun generateNextName(): String {
        return toBijective(charset, index++)
    }

    override fun generatesUnsafeNames(): Boolean {
        return false
    }

    override fun resetNameGenerator() {
        index = 1
    }
}
