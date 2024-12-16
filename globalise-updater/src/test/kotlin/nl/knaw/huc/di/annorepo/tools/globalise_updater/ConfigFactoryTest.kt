package nl.knaw.huc.di.annorepo.tools.globalise_updater

import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.apache.logging.log4j.kotlin.logger

class ConfigFactoryTest {

    @Test
    fun `fromPath should load a config`() {
        val config = ConfigFactory.fromPath("conf/example.yml")
        assertNotNull(config)
        println(config)
    }

    @Test
    fun `all configs in conf should be valid`() {
        Path("conf").listDirectoryEntries("*.yml").forEach { confPath ->
            logger.info { "<= $confPath" }
            val config = ConfigFactory.fromPath(confPath.pathString)
            assertNotNull(config)
        }
    }

}