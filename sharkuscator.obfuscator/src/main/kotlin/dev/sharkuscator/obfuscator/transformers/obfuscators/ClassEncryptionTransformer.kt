package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncrypter
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.writes.ClassWriteEvent
import meteordevelopment.orbit.EventHandler
import java.io.File

class ClassEncryptionTransformer : AbstractTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): Boolean {
        if (!SharedInstances.useNativeLibrary("/class_encrypter.dll")) {
            SharedInstances.logger.error("Failed to load class_encrypter.dll")
        }
        SharedInstances.extractResource("/class_decrypter.dll", File("./class_decrypter.dll"))
        return super.initialization(configuration)
    }

    @EventHandler(priority = 1337)
    private fun onClassWrite(writeEvent: ClassWriteEvent) {
        if (configuration.password.length != 16) {
            SharedInstances.logger.error("Encryption key is not 16 characters long")
            return
        }

        writeEvent.classData = ClassEncrypter.encrypt(writeEvent.classData, configuration.password.encodeToByteArray())
        SharedInstances.logger.debug("Encrypted ${writeEvent.eventNode.name}")
    }
}
