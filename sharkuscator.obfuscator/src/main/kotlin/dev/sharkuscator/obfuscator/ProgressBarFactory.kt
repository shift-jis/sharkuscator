package dev.sharkuscator.obfuscator

import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle

object ProgressBarFactory {
    fun createAsciiBar(taskName: String, initialMax: Long): ProgressBar {
        return ProgressBar.builder()
            .setConsumer(ConsoleProgressBarConsumer(System.out))
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(10)
            .setInitialMax(initialMax)
            .setTaskName(taskName)
            .build()
    }
}
