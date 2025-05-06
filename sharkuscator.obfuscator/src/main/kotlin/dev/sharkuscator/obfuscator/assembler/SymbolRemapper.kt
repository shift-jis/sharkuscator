package dev.sharkuscator.obfuscator.assembler

import org.objectweb.asm.commons.Remapper


class SymbolRemapper : Remapper() {
    val symbolMappings = mutableMapOf<String, String>()

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return symbolMappings["$owner.$name$descriptor"] ?: name
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        return symbolMappings[".$name$descriptor"] ?: name
    }

    override fun mapAnnotationAttributeName(descriptor: String, name: String): String {
        return symbolMappings["$descriptor.$name"] ?: name
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return symbolMappings["$owner.$name"] ?: name
    }

    override fun map(internalName: String): String? {
        return symbolMappings[internalName]
    }

    fun applyMappingsToText(inputText: String): String {
        var currentText = inputText
        for (className in symbolMappings.keys.filter { currentText.contains(it) }) {
            currentText = currentText.replace(className, symbolMappings[className]!!)
        }
        return currentText
    }

    fun setMapping(previous: String, newest: String) {
        symbolMappings[previous] = newest
    }
}
