package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.isClInit
import dev.sharkuscator.obfuscator.extensions.isInit
import dev.sharkuscator.obfuscator.extensions.isMain
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.MethodTransformEvent
import meteordevelopment.orbit.EventHandler

class MethodRenamingTransformer : AbstractTransformer<RenamingConfiguration>("MethodRenaming", RenamingConfiguration::class.java) {
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.isNative || event.eventNode.isClInit() || event.eventNode.isInit() || event.eventNode.isMain()) {
            return
        }
    }
}
