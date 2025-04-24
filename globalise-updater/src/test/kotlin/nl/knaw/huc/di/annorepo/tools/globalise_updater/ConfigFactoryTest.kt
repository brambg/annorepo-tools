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

//    @Test
//    fun `create and test 3 api-keys`() {
////        val serverURI = URI("http://localhost:2023")
////        val ac = AnnoRepoClient(
////            serverURI = serverURI,
////            apiKey = "root"
////        )
//        either {
//            val about = ac.getAbout().bind()
//            logger.info { about }
//            val apiKey1 = UUID.randomUUID().toString()
//            val apiKey2 = UUID.randomUUID().toString()
//            val apiKey3 = UUID.randomUUID().toString()
//            val userEntries = listOf(
//                UserEntry("jona_schlegel", apiKey1),
//                UserEntry("leon_van_wissen", apiKey2),
//                UserEntry("globalise_webapp", apiKey3)
//            )
//            val result = ac.addUsers(userEntries).bind()
//            logger.info { result }
//            listOf(apiKey1, apiKey2, apiKey3).forEach { apiKey ->
//                logger.info { "api-key=$apiKey" }
//                val arc = AnnoRepoClient(serverURI = serverURI, apiKey = apiKey)
//                val myContainers = arc.getMyContainers().bind()
//                logger.info { myContainers }
//            }
//        }
//
//    }

}