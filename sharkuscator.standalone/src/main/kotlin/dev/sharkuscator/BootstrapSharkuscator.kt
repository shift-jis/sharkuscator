package dev.sharkuscator

import dev.sharkuscator.obfuscator.SharkSession
import dev.sharkuscator.obfuscator.Sharkuscator
import java.nio.file.Paths

object BootstrapSharkuscator {
    @JvmStatic
    fun main(args: Array<String>) {
        Sharkuscator.obfuscate(SharkSession(Paths.get("./shark_config.json"), Paths.get("./input_jar.jar"), Paths.get("./output_jar.jar")))
    }
}
