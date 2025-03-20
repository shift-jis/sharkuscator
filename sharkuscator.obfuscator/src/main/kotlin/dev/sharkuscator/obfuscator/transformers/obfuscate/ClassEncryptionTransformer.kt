package dev.sharkuscator.obfuscator.transformers.obfuscate

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer

class ClassEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("ClassEncryption", TransformerConfiguration::class.java) {
}
