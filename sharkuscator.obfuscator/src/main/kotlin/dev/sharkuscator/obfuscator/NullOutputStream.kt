package dev.sharkuscator.obfuscator

import java.io.OutputStream

class NullOutputStream : OutputStream() {
    override fun write(b: Int) {
    }
}
