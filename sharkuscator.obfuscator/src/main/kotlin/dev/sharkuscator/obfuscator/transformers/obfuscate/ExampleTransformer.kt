package dev.sharkuscator.obfuscator.transformers.obfuscate

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer

class ExampleTransformer : AbstractTransformer<TransformerConfiguration>("Example", TransformerConfiguration::class.java)
