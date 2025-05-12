package dev.sharkuscator

import com.xenomachina.argparser.ArgParser

class ObfuscatorArguments(parser: ArgParser) {
    val configurationFile by parser.storing("--config", help = "config file path")
    val outputJarFile by parser.storing("--output", help = "output path of obfuscated jar")
    val inputJarFile by parser.storing("--input", help = "path of jar to obfuscate")
}
