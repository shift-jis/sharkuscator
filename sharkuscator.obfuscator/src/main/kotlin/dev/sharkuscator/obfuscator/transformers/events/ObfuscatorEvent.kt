package dev.sharkuscator.obfuscator.transformers.events

import java.io.File

open class ObfuscatorEvent(val inputJarFile: File, val outputJarFile: File) {
    class InitializationEvent(inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(inputJarFile, outputJarFile)
    class FinalizationEvent(inputJarFile: File, outputJarFile: File) : ObfuscatorEvent(inputJarFile, outputJarFile)
}
