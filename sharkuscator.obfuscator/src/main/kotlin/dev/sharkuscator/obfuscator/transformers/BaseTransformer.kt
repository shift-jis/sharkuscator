package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.annotations.LightObfuscation
import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.AnnotationExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.isDeclaredSynthetic
import dev.sharkuscator.obfuscator.extensions.isDeclaredVolatile
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Type

abstract class BaseTransformer<T : TransformerConfiguration>(
    private val transformerName: String,
    private val configurationClass: Class<T>
) : SharkTransformer<T> {
    lateinit var configuration: T
    lateinit var exclusions: ExclusionRule
    lateinit var excludeAnnotations: List<Regex>
    var transformed = false

    override fun initialization(configuration: GsonConfiguration): T {
        this.configuration = configuration.fromTransformer(this, configurationClass)
        this.exclusions = MixedExclusionRule(buildList {
            addAll(this@BaseTransformer.configuration.exclusions.map {
                StringExclusionRule(it.replace("**", ".*").replace("/", "\\/").toRegex())
            })
            add(AnnotationExclusionRule())
        })
        this.excludeAnnotations = this.configuration.excludeAnnotations.map {
            println(it)
            it.replace("**", ".*").replace("/", "\\/").toRegex()
        }
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
        val visibleAnnotations = classNode.node.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(classNode) && !exclusions.excluded(classNode) && !obfuscationContext.classSource.isLibraryClass(classNode.name)
    }

    protected fun shouldTransformMethod(obfuscationContext: ObfuscationContext, methodNode: MethodNode): Boolean {
        val visibleAnnotations = methodNode.node.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(methodNode) && !exclusions.excluded(methodNode) && !methodNode.isNative
    }

    protected fun shouldTransformField(obfuscationContext: ObfuscationContext, fieldNode: FieldNode): Boolean {
        val visibleAnnotations = fieldNode.node.visibleAnnotations
        if (visibleAnnotations?.any { annotationNode -> excludeAnnotations.any { it.matches(Type.getType(annotationNode.desc).internalName) } } ?: false) {
            return false
        }
        if (visibleAnnotations?.any { annotationNode -> Type.getInternalName(LightObfuscation::class.java) == Type.getType(annotationNode.desc).internalName } ?: false) {
            return transformerStrength() == TransformerStrength.LIGHT
        }
        return !obfuscationContext.exclusions.excluded(fieldNode) && !exclusions.excluded(fieldNode) && !fieldNode.isDeclaredVolatile() && !fieldNode.isDeclaredSynthetic()
    }
}
