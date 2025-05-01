package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncryptor
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assembling.ClassWriteEvent
import dev.sharkuscator.obfuscator.utilities.ResourceExtractor
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import java.nio.file.Paths

@Deprecated("Deprecated due to inconsistent behavior across different environments")
class ClassEncryptionTransformer : AbstractTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): ClassEncryptionConfiguration {
        if (!ResourceExtractor.useNativeLibrary("/class_encryptor.dll")) {
            SharedInstances.logger.error("Failed to load class_encryptor.dll")
        }
        ResourceExtractor.extractResource("/class_decryptor.dll", Paths.get("./class_decryptor.dll"), false)
        return super.initialization(configuration)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onClassWrite(event: ClassWriteEvent) {
        if (configuration.password.length != 16) {
            SharedInstances.logger.error("Encryption key is not 16 characters long")
            return
        }

        event.classData = ClassEncryptor.encrypt(event.classData, configuration.password.encodeToByteArray())
        SharedInstances.logger.debug("Encrypted ${event.classNode.name}")
    }
}
