package dev.sharkuscator.obfuscator.configuration

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.SharkTransformer

open class GsonConfiguration {
    @get:SerializedName("transformers")
    val transformers: JsonObject = JsonObject()

    @get:SerializedName("exclusions")
    val exclusions: Array<String> = emptyArray()

    fun <T : TransformerConfiguration> fromTransformer(transformer: SharkTransformer<T>, clazz: Class<T>): T {
        return ObfuscatorServices.jsonProcessor.fromJson(transformers.getAsJsonObject(transformer.getTransformerName().toSnakeCase()), clazz)
    }
}
