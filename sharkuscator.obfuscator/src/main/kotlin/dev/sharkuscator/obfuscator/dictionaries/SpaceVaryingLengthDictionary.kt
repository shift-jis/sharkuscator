package dev.sharkuscator.obfuscator.dictionaries

class SpaceVaryingLengthDictionary : MappingDictionary("SpaceVaryingLength") {
    private val generatedStrings = mutableSetOf<String>()

    override fun generateNextName(): String {
        while (true) {
            val nextString = " ".repeat((200..700).random())
            if (generatedStrings.add(nextString)) {
                return nextString
            }
        }
    }

    override fun generatesUnsafeNames(): Boolean {
        return true
    }

    override fun resetNameGenerator() {
        generatedStrings.clear()
    }
}
