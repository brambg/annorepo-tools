package nl.knaw.huc.di.annorepo.tools

import java.net.URI
import kotlin.test.fail
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import arrow.core.raise.either
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import nl.knaw.huc.annorepo.api.WebAnnotation
import nl.knaw.huc.annorepo.client.AnnoRepoClient
import nl.knaw.huc.annorepo.client.RequestError

@Disabled
class PerformanceTesterTest {
    private val log = LoggerFactory.getLogger(PerformanceTesterTest::class.java)
    private val annorepoUrl = "http://localhost:2023"

    private val rootClient = AnnoRepoClient(
        serverURI = URI(annorepoUrl),
        apiKey = "root"
    )
    private val userClient = AnnoRepoClient(
        serverURI = URI(annorepoUrl),
        apiKey = "user"
    )
    private val anonymousClient = AnnoRepoClient(
        serverURI = URI(annorepoUrl)
    )

    @Test
    fun `on a container with authorization on and read-only access for anonymous users, reading should be allowed for any user`() {
        val readableContainerName = "container-with-read-access-for-anonymous-users"
        either {
            rootClient.createNewContainer(containerName = readableContainerName, readOnlyForAnonymousUsers = true)
            val annotation = WebAnnotation.Builder().withBody("my-body").withTarget("my-target").build()
            val addResponse = rootClient.createAnnotation(readableContainerName, annotation).bind()
            log.info("annotation location: {}", addResponse.location)

            val rootReadResponse = rootClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            assertThat(rootReadResponse.isRight())
                .withFailMessage { "annotation should be readable by root user" }
                .isTrue()

            val userReadResponse = userClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            assertThat(userReadResponse.isRight())
                .withFailMessage { "annotation should be readable by authorized user" }
                .isTrue()

            val anonymousReadResponse =
                anonymousClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            assertThat(anonymousReadResponse.isRight())
                .withFailMessage { "annotation should be readable by anonymous user" }
                .isTrue()

        }.mapLeft {
            log.error("error: {}", it.message)
            fail()
        }
    }

    @Test
    fun `on a container with authorization on and no read-only access for anonymous users, reading should be allowed authorized users only`() {
        val protectedContainerName = "protected-container"
        either {
            rootClient.createNewContainer(containerName = protectedContainerName, readOnlyForAnonymousUsers = false)
            val annotation = WebAnnotation.Builder().withBody("my-body").withTarget("my-target").build()
            val addResponse = rootClient.createAnnotation(protectedContainerName, annotation).bind()
            log.info("annotation location: {}", addResponse.location)

            val rootReadResponse = rootClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            log.info("response={}", rootReadResponse)
            assertThat(rootReadResponse.isRight())
                .withFailMessage { "annotation should be readable by root user" }
                .isTrue()

            val userReadResponse = userClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            log.info("response={}", userReadResponse)
            assertThat((userReadResponse.isLeft() and (userReadResponse.leftOrNull() is RequestError.NotAuthorized)))
                .withFailMessage { "annotation should not be readable by other authorized user" }
                .isTrue()

            val anonymousReadResponse =
                anonymousClient.getAnnotation(addResponse.containerName, addResponse.annotationName)
            log.info("response={}", anonymousReadResponse)
            assertThat((anonymousReadResponse.isLeft() and (anonymousReadResponse.leftOrNull() is RequestError.NotAuthorized)))
                .withFailMessage { "annotation should not be readable by anonymous user" }
                .isTrue()

        }.mapLeft {
            log.error("error: {}", it.message)
            fail()
        }
    }

    private fun AnnoRepoClient.createNewContainer(containerName: String, readOnlyForAnonymousUsers: Boolean) {
        getContainer(containerName).fold(
            {},
            { result ->
                deleteContainer(containerName = containerName, eTag = result.eTag, force = true)
            }
        )
        createContainer(preferredName = containerName, readOnlyForAnonymousUsers = readOnlyForAnonymousUsers).fold(
            { error -> throw RuntimeException(error.message) },
            { result -> println("created ${result.location}") }
        )
    }

}
