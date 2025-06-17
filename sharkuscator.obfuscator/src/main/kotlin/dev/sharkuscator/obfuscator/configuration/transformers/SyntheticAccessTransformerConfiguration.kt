package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

class SyntheticAccessTransformerConfiguration : TransformerConfiguration() {

    @field:SerializedName("excludeAnnotations")
    val excludeAnnotations: Array<String> = emptyArray()
}
