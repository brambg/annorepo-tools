package nl.knaw.huc.di.annorepo.tools.updater

import org.junit.jupiter.api.Test
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

class GlobaliseUpdaterTest {

    @Test
    fun main() {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.getLogger("org.mongodb").level = Level.OFF
        GlobaliseUpdater.main(arrayOf("conf/local.yml"))
    }

}