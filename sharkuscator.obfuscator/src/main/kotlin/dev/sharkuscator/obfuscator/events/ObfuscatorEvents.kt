package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext
import java.io.File

open class ObfuscatorEvents(val context: ObfuscationContext, val inputJarFile: File, val outputJarFile: File) {
    class InitializationEvents(context: ObfuscationContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvents(context, inputJarFile, outputJarFile)
    class FinalizationEvents(context: ObfuscationContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvents(context, inputJarFile, outputJarFile)
}
