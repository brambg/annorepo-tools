package nl.knaw.huc.di.annorepo.tools.globalise_updater

import kotlin.system.exitProcess
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.mongodb.MongoException
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.logger
import org.bson.Document
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory
import nl.knaw.huc.di.annorepo.tools.globalise_updater.Languages.LanguageRecord
import nl.knaw.huc.di.annorepo.tools.globalise_updater.Languages.languageLabels
import nl.knaw.huc.di.annorepo.tools.globalise_updater.Languages.loadLanguageRecords

object GlobaliseUpdater {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("usage: <cmd> config-file")
            exitProcess(-1)
        }
        disableMongoLogging()

        val config = ConfigFactory.fromPath(args[0])

        MongoClients.create(config.mongo.url).use { mongoClient ->
            val database: MongoDatabase = mongoClient.getDatabase(config.mongo.database)
            val collection = database.getCollection(config.mongo.collection)
            doUpdates(collection, config)
        }
        logger.info { "done!" }
    }

    private fun disableMongoLogging() {
        val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.getLogger("org.mongodb").level = Level.OFF
    }

    data class UpdateGroup(
        val pageIds: List<String>,
        val languages: List<String>,
        val corrected: Boolean
    )

    data class GroupKey(
        val languages: List<String>,
        val corrected: Boolean,
    )

    private fun doUpdates(collection: MongoCollection<Document>, config: UpdaterConfig) {
        val languageRecords = loadLanguageRecords(config.languageDataFilePath)
        logger.info { "${languageRecords.size} records loaded" }
        val progressKeeper = ProgressKeeper(total = languageRecords.size.toLong(), delay = 10_000)
        runBlocking {
            launch {
                progressKeeper.showProgress()
            }
            launch {
                languageRecords
                    .toUpdateGroupSequence()
                    .forEach {
                        launch {
                            doUpdateMany(it, collection, progressKeeper)
                            progressKeeper.incRecordsProcessed(it.pageIds.size)
                            delay(1)
                        }
                        delay(1)
                    }
                progressKeeper.stop()
            }
        }
    }

    private fun doUpdateMany(
        updateGroup: UpdateGroup,
        collection: MongoCollection<Document>,
        progressKeeper: ProgressKeeper
    ) {
        val query: Bson = Filters.`in`("annotation.body.metadata.document", updateGroup.pageIds)
        val updates = Updates.combine(
            Updates.set(
                "annotation.body.metadata.lang",
                updateGroup.languages.map { ol ->
                    val newL = ol.replace("heb", "hbo")
                    mapOf(
                        "iso" to newL,
                        "label" to languageLabels[newL]
                    )
                }),
            Updates.set("annotation.body.metadata.langCorrected", updateGroup.corrected)
        )
        try {
            val result = collection.updateMany(query, updates)
            if (result.modifiedCount > 0) {
                progressKeeper.incModifiedDocuments(result.modifiedCount)
            }
        } catch (me: MongoException) {
            progressKeeper.incErrors()
            logger.error { "Unable to update due to an error: $me" }
        }
    }

    private const val MAX_GROUP_SIZE = 100
    private fun List<LanguageRecord>.toUpdateGroupSequence(): Sequence<UpdateGroup> {
        val updateGroupMap: MutableMap<GroupKey, MutableList<String>> =
            mutableMapOf()
        return sequence {
            forEach {
                val key = GroupKey(it.languages, it.corrected)
                val pageIds = updateGroupMap.getOrDefault(key, mutableListOf()).apply { add(it.pageId) }
                updateGroupMap[key] = pageIds
                if (pageIds.size >= MAX_GROUP_SIZE) {
                    yield(
                        UpdateGroup(
                            pageIds = updateGroupMap[key]!!,
                            languages = key.languages,
                            corrected = key.corrected
                        )
                    )
                    updateGroupMap[key]?.clear()
                }
            }
            updateGroupMap.forEach { (key, pageIds) ->
                if (pageIds.isNotEmpty()) {
                    yield(
                        UpdateGroup(
                            pageIds = pageIds,
                            languages = key.languages,
                            corrected = key.corrected
                        )
                    )

                }
            }
        }
    }

}

