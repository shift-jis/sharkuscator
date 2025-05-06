package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext
import java.io.File

open class ObfuscatorEvent(val context: ObfuscationContext, val inputJarFile: File, val outputJarFile: File) {
    class InitializationEvent(context: ObfuscationContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(context, inputJarFile, outputJarFile)
    class FinalizationEvent(context: ObfuscationContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(context, inputJarFile, outputJarFile)
}
