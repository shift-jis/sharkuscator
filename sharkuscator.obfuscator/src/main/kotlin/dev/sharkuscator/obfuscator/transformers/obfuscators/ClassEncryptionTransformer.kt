package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncrypter
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assembling.ClassWriteEvent
import dev.sharkuscator.obfuscator.utilities.ResourceExtractor
import meteordevelopment.orbit.EventHandler
import java.io.File
import java.nio.file.Paths

@Deprecated("Because it does not work depending on the environment")
class ClassEncryptionTransformer : AbstractTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): ClassEncryptionConfiguration {
        if (!ResourceExtractor.useNativeLibrary("/class_encryptor.dll")) {
            SharedInstances.logger.error("Failed to load class_encryptor.dll")
        }
        ResourceExtractor.extractResource("/class_decryptor.dll", Paths.get("./class_decryptor.dll"), false)
        return super.initialization(configuration)
    }

    @EventHandler(priority = 1337)
    private fun onClassWrite(event: ClassWriteEvent) {
        if (configuration.password.length != 16) {
            SharedInstances.logger.error("Encryption key is not 16 characters long")
            return
        }

        event.classData = ClassEncrypter.encrypt(event.classData, configuration.password.encodeToByteArray())
        SharedInstances.logger.debug("Encrypted ${event.classNode.name}")
    }
}
