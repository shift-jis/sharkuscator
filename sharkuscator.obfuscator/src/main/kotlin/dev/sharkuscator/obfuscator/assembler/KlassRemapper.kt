package dev.sharkuscator.obfuscator.assembler

import org.objectweb.asm.commons.Remapper


class KlassRemapper : Remapper() {
    val mappings = mutableMapOf<String, String>()

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return map("$owner.$name$descriptor") ?: name
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        return map(".$name$descriptor") ?: name
    }

    override fun mapAnnotationAttributeName(descriptor: String, name: String): String {
        return map("$descriptor.$name") ?: name
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return map("$owner.$name") ?: name
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
