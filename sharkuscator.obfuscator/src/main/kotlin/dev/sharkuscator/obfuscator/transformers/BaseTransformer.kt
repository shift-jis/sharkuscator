package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.annotations.LightObfuscation
import dev.sharkuscator.commons.extensions.isDeclaredNative
import dev.sharkuscator.commons.extensions.isDeclaredVolatile
import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.AnnotationExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

abstract class BaseTransformer<T : TransformerConfiguration>(private val transformerName: String, private val configurationClass: Class<T>) : SharkTransformer<T> {
    lateinit var configuration: T
    lateinit var exclusions: ExclusionRule
    lateinit var excludeAnnotations: List<Regex>
    var transformed = false

    override fun initialization(configuration: GsonConfiguration): T {
        this.configuration = configuration.fromTransformer(this, configurationClass)
        this.exclusions = MixedExclusionRule(buildList {
            addAll(this@BaseTransformer.configuration.exclusions.map { StringExclusionRule(it.toExclusionRegexPattern()) })
            add(AnnotationExclusionRule())
        })
        this.excludeAnnotations = this.configuration.excludeAnnotations.map { it.toExclusionRegexPattern() }
        return this.configuration
    }

    override fun isEligibleForExecution(): Boolean {
        return configuration.enabled && !transformed
    }

    override fun executionPriority(): Int {
        return TransformerPriority.ZERO
    }

    override fun transformerName(): String {
        return transformerName
    }

    protected fun shouldTransformClass(obfuscationContext: ObfuscationContext, classNode: ClassNode): Boolean {
        val visibleAnnotations = classNode.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(classNode) && !exclusions.excluded(classNode) && !obfuscationContext.classNodeProvider.isLibraryClass(classNode.name)
    }

    protected fun shouldTransformMethod(obfuscationContext: ObfuscationContext, methodNode: MethodNode): Boolean {
        val visibleAnnotations = methodNode.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(methodNode) && !exclusions.excluded(methodNode) && !methodNode.isDeclaredNative()
    }

    protected fun shouldTransformField(obfuscationContext: ObfuscationContext, fieldNode: FieldNode): Boolean {
        val visibleAnnotations = fieldNode.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(fieldNode) && !exclusions.excluded(fieldNode) && !fieldNode.isDeclaredVolatile()
    }

    private fun String.toExclusionRegexPattern(): Regex {
        return this.replace("**", ".*") // Match any characters (0 or more)
            .replace("$", "\\$")     // Escape '$' as it's a regex metacharacter
            .replace("/", "\\/")     // Escape '/' as it's a regex metacharacter
            .toRegex()
    }
}
