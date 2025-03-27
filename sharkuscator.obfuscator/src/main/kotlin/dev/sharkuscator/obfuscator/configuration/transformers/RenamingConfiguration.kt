package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

class RenamingConfiguration : TransformerConfiguration() {
    @field:SerializedName("dictionary")
    val dictionary: String = "alphabet"

    @field:SerializedName("prefix")
    val prefix: String = ""
}
