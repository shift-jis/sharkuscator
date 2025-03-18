package dev.sharkuscator.obfuscator.encryption

object ClassEncrypter {
    external fun encrypt(classData: ByteArray, keyData: ByteArray): ByteArray
}
