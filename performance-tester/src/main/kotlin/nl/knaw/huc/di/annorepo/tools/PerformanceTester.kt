package nl.knaw.huc.di.annorepo.tools
//
//import java.io.File
//import java.net.URI
//import kotlin.system.measureTimeMillis
//import com.fasterxml.jackson.core.type.TypeReference
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.kotlinModule
//import kotlinx.coroutines.flow.count
//import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
//import nl.knaw.huc.annorepo.api.WebAnnotationAsMap
//import nl.knaw.huc.annorepo.client.AnnoRepoClient
//
object PerformanceTester {
    private val log = LoggerFactory.getLogger(PerformanceTester::class.java)
    private val restContainer = "rest-test-container"
    private val grpcContainer = "grpc-test-container"
//    private val mapper = ObjectMapper().apply { registerModules(kotlinModule()) }
//    private val path = "/Users/bram/workspaces/globalise/globalise-tools/out/px_textline_annotations.json"
//
    @JvmStatic
    fun main(args: Array<String>) {
        TODO()
//        runBlocking {
//            val annoRepoClient = AnnoRepoClient(serverURI = URI("http://localhost:2023"), apiKey = "dummy-api-key")
//            val annotations: List<WebAnnotationAsMap> =
//                loadAnnotations(path)
//            val total = annotations.size
//            println("uploading $total annotations:")
//
//            val restElapsed = measureTimeMillis {
//                uploadUsingRest(annoRepoClient, restContainer, annotations)
//            }
//            val restAvg: Float = restElapsed / (total).toFloat()
//            println("using rest: ${(restElapsed).toFloat() / 1000} seconds = $restAvg milliseconds/annotation")
//
//            val grpcElapsed = measureTimeMillis {
//                uploadUsingGrpc(annoRepoClient, grpcContainer, annotations)
//            }
//            val grpcAvg: Float = grpcElapsed / (total).toFloat()
//            println("using gRPC: ${(grpcElapsed).toFloat() / 1000} seconds = $grpcAvg milliseconds/annotation")
//        }
    }
//
//    private fun loadAnnotations(path: String): List<WebAnnotationAsMap> {
//        val jsonString = File(path).readText(Charsets.UTF_8)
//        val valueTypeRef = object : TypeReference<List<WebAnnotationAsMap>>() {}
//        return mapper.readValue(jsonString, valueTypeRef)
//    }
//
//    private fun uploadUsingRest(
//        annoRepoClient: AnnoRepoClient,
//        containerName: String,
//        annotations: List<WebAnnotationAsMap>
//    ) {
//        annoRepoClient.createNewContainer(containerName)
//        val result = annoRepoClient.batchUpload(containerName, annotations)
//        println("result size=${result.getOrNull()?.annotationData?.size}")
////        log.info("result={}", result)
//    }
//
//    private suspend fun uploadUsingGrpc(
//        annoRepoClient: AnnoRepoClient,
//        containerName: String,
//        annotations: List<WebAnnotationAsMap>
//    ) {
//        annoRepoClient.createNewContainer(containerName)
//        annoRepoClient.usingGrpc { grpc ->
//            val resultFlow = grpc.addContainerAnnotation(containerName = containerName, annotations = annotations)
//            println("result size=${resultFlow.count()}")
//        }
//    }
//
//    private fun AnnoRepoClient.createNewContainer(containerName: String) {
//        getContainer(containerName).fold(
//            {},
//            { result ->
//                deleteContainer(containerName = containerName, eTag = result.eTag, force = true)
//            }
//        )
//        createContainer(preferredName = containerName).fold(
//            { error -> throw RuntimeException(error.message) },
//            { result -> println("created ${result.location}") }
//        )
//    }
}