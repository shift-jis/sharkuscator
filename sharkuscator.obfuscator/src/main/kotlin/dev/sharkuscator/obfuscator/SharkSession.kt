package dev.sharkuscator.obfuscator

import java.nio.file.Path

data class SharkSession(val configurationFile: Path, val inputJarPath: Path, val outputJarPath: Path)
