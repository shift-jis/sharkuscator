package dev.sharkuscator.obfuscator.classsource

import dev.sharkuscator.obfuscator.assembly.SharkClassNode

class ZipImportResult(
    val classNodes: MutableMap<String, SharkClassNode>,
    val resources: MutableMap<String, ByteArray>,
    val packages: MutableMap<String, ByteArray>,
)
