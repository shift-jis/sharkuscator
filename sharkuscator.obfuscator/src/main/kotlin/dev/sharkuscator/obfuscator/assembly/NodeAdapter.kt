package dev.sharkuscator.obfuscator.assembly

abstract class NodeAdapter<T>(val originalNode: T) {
    abstract fun fullyName(): String
}
