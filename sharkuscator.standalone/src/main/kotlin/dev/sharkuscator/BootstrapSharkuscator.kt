package dev.sharkuscator

import com.xenomachina.argparser.ArgParser
import dev.sharkuscator.obfuscator.Sharkuscator
import java.io.File
import java.nio.file.Paths

object BootstrapSharkuscator {
    @JvmStatic
    fun main(args: Array<String>) {
        ArgParser(args).parseInto(::ObfuscatorArguments).run {
            val obfuscator = Sharkuscator(Paths.get(configJsonPath), File(inputJarPath), File(outputJarPath))
            obfuscator.obfuscate()
        }
    }
}
