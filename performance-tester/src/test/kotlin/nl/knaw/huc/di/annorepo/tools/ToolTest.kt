package nl.knaw.huc.di.annorepo.tools

import java.net.URI
import kotlin.test.fail
import org.junit.jupiter.api.Test
import arrow.core.raise.either
import org.apache.logging.log4j.kotlin.logger
import nl.knaw.huc.annorepo.client.AnnoRepoClient

class ToolTest {

    @Test
    fun `test tool`() {
        val c = AnnoRepoClient(serverURI = URI("http://localhost:8080"))
//        val c = AnnoRepoClient(serverURI = URI("https://annorepo.globalise.huygens.knaw.nl"))
        val query = mapOf("body.type" to "px:Page", "body.metadata.inventoryNumber" to "9986")
        val result = c.filterContainerAnnotations(containerName = "globalise-2024-03-18", query = query)
        var l = 0
        result.fold(
            { e -> println(e.message) },
            { r ->
                r.annotations.forEach {
                    println(it.getOrNull()?.length)
                    l += 1
                }
            }
        )
        println("$l results")
    }

//    val containersToRemove = listOf(
//        ""
//    )
//
//    @Test
//    fun `container cleanup`() {
//        val c = AnnoRepoClient(
//            serverURI = URI("https://annorepo.republic-caf.diginfra.org"),
//            apiKey = ""
//        )
//        containersToRemove.forEach { containerName ->
//            c.getContainer(containerName).getOrNull()?.eTag?.let { eTag ->
//                val result = c.deleteContainer(containerName = containerName, eTag = eTag, force = true)
//                println(result)
//            }
//        }
//    }

    @Test
    fun `test query`() {
        val c = AnnoRepoClient(serverURI = URI("http://localhost:8080"), apiKey = "root")
        val query = mapOf("body.type" to "Page"
        )
        val containerName = "republic-2024.02.23"
        either {
//            val myContainers = c.getMyContainers().bind().containers
//            println(myContainers.flatMap { it.value }.sorted().joinToString("\n"))
//            val types = c.getDistinctFieldValues(containerName = containerName, fieldName = "body.type").bind()
//            println(types.distinctValues.map { it.toString() }.sorted())
            val meta = c.getContainerMetadata((containerName)).bind()
            println(meta.metadata)
            val result =
                c.filterContainerAnnotations(containerName = containerName, query = query)
                    .bind()
            var l = 0
            result.annotations.forEach {
                println(it.getOrNull()?.length)
                l += 1
            }
            println("$l results")
        }.mapLeft {
            logger.error { "error: ${it.message}" }
            fail()
        }
    }

}