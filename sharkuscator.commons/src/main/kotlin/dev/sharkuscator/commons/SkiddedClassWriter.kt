package dev.sharkuscator.commons

import dev.sharkuscator.commons.extensions.isDeclaredAsInterface
import dev.sharkuscator.commons.providers.ApplicationClassProvider
import org.objectweb.asm.ClassWriter


// https://github.com/ItzSomebody/radon/blob/master/xyz.itzsomebody.radon/src/main/java/xyz/itzsomebody/radon/utils/asm/RadonClassWriter.java
class SkiddedClassWriter(private val classNodeProvider: ApplicationClassProvider, writeFlags: Int) : ClassWriter(writeFlags) {
    override fun getCommonSuperClass(firstType: String, secondType: String): String {
        if ("java/lang/Object" == firstType || "java/lang/Object" == secondType) {
            return "java/lang/Object"
        }

        val firstSuperName = deriveCommonSuperName(firstType, secondType)
        if ("java/lang/Object" != firstSuperName) {
            return firstSuperName
        }

        val secondSuperName = deriveCommonSuperName(secondType, firstType)
        if ("java/lang/Object" != secondSuperName) {
            return secondSuperName
        }

        return getCommonSuperClass(classNodeProvider.getClassNode(firstType)!!.superName, classNodeProvider.getClassNode(secondType)!!.superName)
    }

    private fun deriveCommonSuperName(firstType: String, secondType: String): String {
        if (isAssignableFrom(firstType, secondType)) {
            return firstType
        }

        if (isAssignableFrom(secondType, firstType)) {
            return secondType
        }

        if (classNodeProvider.getClassNode(firstType)!!.isDeclaredAsInterface() || classNodeProvider.getClassNode(secondType)!!.isDeclaredAsInterface()) {
            return "java/lang/Object"
        }

        var currentTypeName = firstType
        while (currentTypeName != "java/lang/Object") {
            if (isAssignableFrom(currentTypeName, secondType)) {
                return currentTypeName
            }
            currentTypeName = classNodeProvider.getClassNode(currentTypeName)!!.superName
        }

        return currentTypeName
    }

    private fun isAssignableFrom(firstType: String, secondType: String): Boolean {
        if ("java/lang/Object" == firstType || firstType == secondType) {
            return true
        }

        val firstClassNode = classNodeProvider.getClassNode(firstType) ?: return true
        var currentSuperName = firstClassNode.superName

        while (currentSuperName != null) {
            if (currentSuperName == "java/lang/Object") {
                return false
            }

            if (currentSuperName == firstType) {
                return true
            }

            currentSuperName = classNodeProvider.getClassNode(currentSuperName)?.superName
        }

        return false
    }
}
