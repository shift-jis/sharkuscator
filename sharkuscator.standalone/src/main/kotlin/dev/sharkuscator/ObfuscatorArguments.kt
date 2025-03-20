package dev.sharkuscator

import com.xenomachina.argparser.ArgParser

class ObfuscatorArguments(parser: ArgParser) {
    val configJsonPath by parser.storing("--config", help = "config file path")
    val outputJarPath by parser.storing("--output", help = "output path of obfuscated jar")
    val inputJarPath by parser.storing("--input", help = "path of jar to obfuscate")
}
