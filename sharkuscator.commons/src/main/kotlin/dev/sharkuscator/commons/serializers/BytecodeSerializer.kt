package dev.sharkuscator.commons.serializers

interface BytecodeSerializer<T> {
    fun deserialize(byteArray: ByteArray): T

    fun serialize(nodeObject: T): ByteArray
}
