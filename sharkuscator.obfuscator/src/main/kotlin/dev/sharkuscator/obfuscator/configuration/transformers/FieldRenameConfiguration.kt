package dev.sharkuscator.obfuscator.configuration.transformers

import com.google.gson.annotations.SerializedName

open class FieldRenameConfiguration : RenameConfiguration() {

    @field:SerializedName("excludeAnnotations")
    val excludeAnnotations: Array<String> = emptyArray()
}
