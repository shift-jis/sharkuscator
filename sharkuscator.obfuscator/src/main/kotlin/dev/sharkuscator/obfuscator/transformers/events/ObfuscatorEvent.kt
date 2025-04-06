package dev.sharkuscator.obfuscator.transformers.events

import java.io.File

open class ObfuscatorEvent(val context: EventContext, val inputJarFile: File, val outputJarFile: File) {
    class InitializationEvent(context: EventContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(context, inputJarFile, outputJarFile)
    class FinalizationEvent(context: EventContext, inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(context, inputJarFile, outputJarFile)
}
