package dev.sharkuscator.obfuscator.assembler

import dev.sharkuscator.obfuscator.extensions.isDeclaredAsInterface
import org.mapleir.app.service.ApplicationClassSource
import org.objectweb.asm.ClassWriter


// https://github.com/ItzSomebody/radon/blob/master/xyz.itzsomebody.radon/src/main/java/xyz/itzsomebody/radon/utils/asm/RadonClassWriter.java
class ClassWriter(private val classSource: ApplicationClassSource, flags: Int) : ClassWriter(flags) {
    override fun getCommonSuperClass(type1: String, type2: String): String {
        if ("java/lang/Object" == type1 || "java/lang/Object" == type2) {
            return "java/lang/Object"
        }

        val firstSuperName = deriveCommonSuperName(type1, type2)
        if ("java/lang/Object" != firstSuperName) {
            return firstSuperName
        }

        val secondSuperName = deriveCommonSuperName(type2, type1)
        if ("java/lang/Object" != secondSuperName) {
            return secondSuperName
        }

        return getCommonSuperClass(classSource.findClassNode(type1).node.superName, classSource.findClassNode(type2).node.superName)
    }

    private fun deriveCommonSuperName(type1: String, type2: String): String {
        if (isAssignableFrom(type1, type2)) {
            return type1
        }
        if (isAssignableFrom(type2, type1)) {
            return type2
        }

        if (classSource.findClassNode(type1).isDeclaredAsInterface() || classSource.findClassNode(type2).isDeclaredAsInterface()) {
            return "java/lang/Object"
        }

        var currentType1 = type1
        while (currentType1 != "java/lang/Object") {
            if (isAssignableFrom(currentType1, type2)) {
                return currentType1
            }
            currentType1 = classSource.findClassNode(currentType1).node.superName
        }

        return currentType1
    }

    private fun isAssignableFrom(type1: String, type2: String): Boolean {
        if ("java/lang/Object" == type1 || type1 == type2) {
            return true
        }

        val targetNode = classSource.findClassNode(type1) ?: return true
        var currentSuperName = targetNode.node.superName

        while (currentSuperName != null) {
            if (currentSuperName == type1) {
                return true
            }
            if (currentSuperName == "java/lang/Object") {
                return false
            }
            currentSuperName = classSource.findClassNode(currentSuperName).node.superName
        }

        return false
    }
}
