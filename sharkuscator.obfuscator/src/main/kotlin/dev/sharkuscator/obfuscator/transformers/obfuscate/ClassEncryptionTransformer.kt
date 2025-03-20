package dev.sharkuscator.obfuscator.transformers.obfuscate

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.ClassEncryptionConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncrypter
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.ClassWriteEvent
import meteordevelopment.orbit.EventHandler

class ClassEncryptionTransformer : AbstractTransformer<ClassEncryptionConfiguration>("ClassEncryption", ClassEncryptionConfiguration::class.java) {
    override fun initialization(configuration: GsonConfiguration): Boolean {
        if (!SharedInstances.useNativeLibrary("/class_encrypter.dll")) {
            SharedInstances.logger.error("Failed to load class_encrypter.dll")
        }
        return super.initialization(configuration)
    }

    @EventHandler(priority = 1337)
    private fun onClassWrite(classWriteEvent: ClassWriteEvent) {
        if (configuration.password.length != 16) {
            SharedInstances.logger.error("Encryption key is not 16 characters long")
            return
        }
        classWriteEvent.classData = ClassEncrypter.encrypt(classWriteEvent.classData, configuration.password.encodeToByteArray())
    }
}
