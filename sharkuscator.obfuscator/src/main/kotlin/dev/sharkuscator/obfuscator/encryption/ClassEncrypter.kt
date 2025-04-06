package dev.sharkuscator.obfuscator.encryption

@Deprecated("Deprecated due to inconsistent behavior across different environments")
object ClassEncrypter {
    external fun encrypt(classData: ByteArray, keyData: ByteArray): ByteArray
}
