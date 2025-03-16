package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

open class TransformerConfiguration {
    @field:SerializedName("enabled")
    val enabled: Boolean = false

    @field:SerializedName("exclusions")
    val exclusions: Array<String> = emptyArray()
}
