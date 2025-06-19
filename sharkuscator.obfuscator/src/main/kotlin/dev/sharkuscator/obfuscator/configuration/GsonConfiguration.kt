package dev.sharkuscator.obfuscator.configuration

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.SharkTransformer

open class GsonConfiguration {
    @field:SerializedName("slash_class_entries")
    val slashClassEntries: Boolean = false

    @get:SerializedName("transformers")
    val transformers: JsonObject = JsonObject()

    @get:SerializedName("libraries")
    val libraries: Array<String> = emptyArray()

    @get:SerializedName("exclusions")
    val exclusions: Array<String> = emptyArray()

    fun <T : TransformerConfiguration> fromTransformer(transformer: SharkTransformer<T>, clazz: Class<T>): T {
        return ObfuscatorServices.prettyGson.fromJson(transformers.getAsJsonObject(transformer.getTransformerName().toSnakeCase()), clazz)
    }
}
