package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

class RenameConfiguration : TransformerConfiguration() {
    @field:SerializedName("dictionary")
    val dictionary: String = "alphabetical"

    @field:SerializedName("name_prefix")
    val namePrefix: String = ""

    @field:SerializedName("prefix_repetitions")
    val prefixRepetitions: Int = 1
}
