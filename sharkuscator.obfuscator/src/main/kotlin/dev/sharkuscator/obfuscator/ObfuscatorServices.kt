package dev.sharkuscator.obfuscator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.commons.SymbolRemapper
import meteordevelopment.orbit.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ObfuscatorServices {
    val sharkLogger: Logger = LoggerFactory.getLogger("(Sharkuscator)")
    val prettyGson: Gson = GsonBuilder().setPrettyPrinting().create()

    val symbolRemapper: SymbolRemapper = SymbolRemapper()
    val mainEventBus: EventBus = EventBus()
}
