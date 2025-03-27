package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncrypter
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assemble.KlassWriteEvent
import dev.sharkuscator.obfuscator.utilities.ResourceExtractor
import meteordevelopment.orbit.EventHandler
import java.io.File

class KlassEncryptionTransformer : AbstractTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): ClassEncryptionConfiguration {
        if (!ResourceExtractor.useNativeLibrary("/class_encrypter.dll")) {
            SharedInstances.logger.error("Failed to load class_encrypter.dll")
        }
        ResourceExtractor.extractResource("/class_decrypter.dll", File("./class_decrypter.dll"))
        return super.initialization(configuration)
    }

    @EventHandler(priority = 1337)
    private fun onClassWrite(event: KlassWriteEvent) {
        if (configuration.password.length != 16) {
            SharedInstances.logger.error("Encryption key is not 16 characters long")
            return
        }

        event.classData = ClassEncrypter.encrypt(event.classData, configuration.password.encodeToByteArray())
        SharedInstances.logger.debug("Encrypted ${event.classNode.name}")
    }
}
