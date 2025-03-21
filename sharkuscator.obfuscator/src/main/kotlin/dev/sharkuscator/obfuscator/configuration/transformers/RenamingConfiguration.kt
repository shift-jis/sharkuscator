package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

class RenamingConfiguration : TransformerConfiguration() {
    @field:SerializedName("prefix")
    val prefix: String = ""
}
