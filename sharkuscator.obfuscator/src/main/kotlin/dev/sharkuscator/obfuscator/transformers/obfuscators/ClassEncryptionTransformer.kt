package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncryptor
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.utilities.ResourceUtils
import meteordevelopment.orbit.EventHandler
import java.nio.file.Paths

@Deprecated("Deprecated due to inconsistent behavior across different environments")
class ClassEncryptionTransformer : BaseTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): ClassEncryptionConfiguration {
        if (!ResourceUtils.loadNativeLibraryFromResources("/class_encryptor.dll")) {
            ObfuscatorServices.sharkLogger.error("Failed to load class_encryptor.dll")
        }
        ResourceUtils.extractResource("/class_decryptor.dll", Paths.get("./class_decryptor.dll"), false)
        return super.initialization(configuration)
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassWrite(event: AssemblerEvents.ClassWriteEvent) {
        if (configuration.password.length != 16) {
            ObfuscatorServices.sharkLogger.error("Encryption key is not 16 characters long")
            return
        }

        event.classData = ClassEncryptor.encrypt(event.classData, configuration.password.encodeToByteArray())
        ObfuscatorServices.sharkLogger.debug("Encrypted ${event.classNode.name}")
    }
}
