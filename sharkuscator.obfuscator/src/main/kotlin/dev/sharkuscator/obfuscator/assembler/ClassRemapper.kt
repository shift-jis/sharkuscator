package dev.sharkuscator.obfuscator.assembler

import org.objectweb.asm.commons.Remapper


class ClassRemapper : Remapper() {
    val mappings = mutableMapOf<String, String>()

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return mappings["$owner.$name$descriptor"] ?: name
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        return mappings[".$name$descriptor"] ?: name
    }

    override fun mapAnnotationAttributeName(descriptor: String, name: String): String {
        return mappings["$descriptor.$name"] ?: name
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return mappings["$owner.$name"] ?: name
    }

    override fun map(internalName: String): String? {
        return mappings[internalName]
    }

    fun contains(internalName: String): Boolean {
        return mappings.containsKey(internalName)
    }

    fun setMapping(previous: String, newest: String) {
        mappings[previous] = newest
    }
}
