package nl.knaw.huc.di.annorepo.tools.updater

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ConfigFactoryTest {

    @Test
    fun `fromPath should load a config`() {
        val config = ConfigFactory.fromPath("conf/example.yml")
        assertNotNull(config)
        println(config)
    }
}