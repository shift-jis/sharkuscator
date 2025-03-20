package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

class ClassEncryptionConfiguration : TransformerConfiguration() {
    @field:SerializedName("package_name")
    val packageName: String = ""

    @field:SerializedName("encrypt_key")
    val encryptKey: String = ""
}
