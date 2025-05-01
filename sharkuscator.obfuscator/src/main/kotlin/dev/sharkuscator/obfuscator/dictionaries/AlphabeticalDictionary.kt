package dev.sharkuscator.obfuscator.dictionaries

class AlphabeticalDictionary : MappingDictionary("Alphabetical") {
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private var index = 1

    override fun nextString(): String {
        return toBijective(charset, index++)
    }

    override fun isDangerous(): Boolean {
        return false
    }
}
