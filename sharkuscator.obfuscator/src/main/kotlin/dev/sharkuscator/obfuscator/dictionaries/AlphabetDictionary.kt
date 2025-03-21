package dev.sharkuscator.obfuscator.dictionaries

class AlphabetDictionary : MappingDictionary("Alphabet") {
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private var index = 1

    override fun nextString(): String {
        return toBijective(charset, index++)
    }
}
