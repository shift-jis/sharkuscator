package dev.sharkuscator.commons

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

    fun findClosestMethodMapping(owner: String, name: String, parametersHint: Array<Any>? = null): String {
        var fallbackMatchKey: String? = null
        val methodPrefix = "$owner.$name"

        for (originalMappingKey in symbolMappings.keys) {
            if (originalMappingKey.startsWith(methodPrefix)) {
                val descriptorPart = originalMappingKey.substring(methodPrefix.length)
                if (descriptorPart.startsWith("(")) {
                    if ((parametersHint == null || parametersHint.isEmpty()) && descriptorPart.startsWith("()")) {
                        return symbolMappings.getValue(originalMappingKey)
                    } else if (fallbackMatchKey == null) {
                        fallbackMatchKey = originalMappingKey
                    }
                }
            }
        }

        return fallbackMatchKey?.let { symbolMappings.getValue(it) } ?: name
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
