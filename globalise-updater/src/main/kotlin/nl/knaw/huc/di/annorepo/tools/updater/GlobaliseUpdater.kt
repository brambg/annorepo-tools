package nl.knaw.huc.di.annorepo.tools.updater

import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.useLines
import kotlin.system.exitProcess
import com.mongodb.MongoException
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.tongfei.progressbar.DelegatingProgressBarConsumer
import me.tongfei.progressbar.ProgressBarBuilder
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import org.bson.Document
import org.bson.conversions.Bson

object GlobaliseUpdater {

    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("usage: <cmd> config-file")
            exitProcess(-1)
        }
        val configPath = args[0]
        val config = ConfigFactory.fromPath(configPath)

        MongoClients.create(config.mongo.url).use { mongoClient ->
            val database: MongoDatabase = mongoClient.getDatabase(config.mongo.database)
            val collection = database.getCollection(config.mongo.collection)
            doUpdates(collection, config)
        }
    }

    data class UpdateGroup(
        val pageIds: List<String>,
        val languages: List<String>,
        val corrected: Boolean
    )

    data class LanguageRecord(
        val pageId: String,
        val languages: List<String>,
        val corrected: Boolean,
    )

    data class GroupKey(
        val languages: List<String>,
        val corrected: Boolean,
    )

    private fun doUpdates(collection: MongoCollection<Document>, config: UpdaterConfig) {
        val languageRecords = loadLanguageRecords(config.languageDataFilePath)
        ProgressBarBuilder()
//            .setStyle(ProgressBarStyle.ASCII)
            .setConsumer(DelegatingProgressBarConsumer(logger::info))
            .setInitialMax(languageRecords.size.toLong())
            .setTaskName("Updating...")
            .showSpeed()
            .build()
            .use { pb ->
                runBlocking {
                    languageRecords
                        .toUpdateGroupSequence()
                        .forEach {
                            launch {
//                            println(it)
                                doUpdateMany(it, collection)
                                pb.stepBy(it.pageIds.size.toLong())
                            }
                        }
                }
            }
    }

    private fun doUpdateMany(
        updateGroup: UpdateGroup,
        collection: MongoCollection<Document>
    ) {
        val query: Bson = Filters.`in`("annotation.body.metadata.document", updateGroup.pageIds)
        val updates = Updates.combine(
            Updates.set("annotation.body.metadata.lang", updateGroup.languages),
            Updates.set("annotation.body.metadata.langCorrected", updateGroup.corrected)
        )
        try {
            val result = collection.updateMany(query, updates)
            if (result.modifiedCount > 0) {
                logger.info { "Modified document count: ${result.modifiedCount}/${updateGroup.pageIds.size}" }
                logger.info { updateGroup }
            }
        } catch (me: MongoException) {
            System.err.println("Unable to update due to an error: $me")
        }
    }

    private const val MAX_GROUP_SIZE = 25
    private fun List<LanguageRecord>.toUpdateGroupSequence(): Sequence<UpdateGroup> {
        val updateGroupMap: MutableMap<GroupKey, MutableList<String>> =
            mutableMapOf<GroupKey, MutableList<String>>()
        return sequence {
            forEach {
                val key = GroupKey(it.languages, it.corrected)
                val pageIds = updateGroupMap.getOrDefault(key, mutableListOf()).apply { add(it.pageId) }
                updateGroupMap[key] = pageIds
                if (pageIds.size ?: 0 >= MAX_GROUP_SIZE) {
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

    private fun loadLanguageRecords(languageDataFilePath: String): List<LanguageRecord> {
        logger.logFileRead(languageDataFilePath)
        return Path(languageDataFilePath).readLines()
            .drop(1)
            .map { it.split("\t") }
            .map {
                LanguageRecord(
                    pageId = "NL-HaNA_1.04.02_${it[0]}_${it[1]}",
                    languages = it[2].split(","),
                    corrected = it[3] != "0"
                )
            }
    }

    private fun loadLanguageRecords0(languageDataFilePath: String): List<LanguageRecord> {
        logger.logFileRead(languageDataFilePath)
        return Path(languageDataFilePath).useLines { lines ->
            lines
                .drop(1)
                .map { it.split("\t") }
                .map {
                    LanguageRecord(
                        pageId = "NL-HaNA_1.04.02_${it[0]}_${it[1]}",
                        languages = it[2].split(","),
                        corrected = it[3] != "0"
                    )
                }
        }.toList()
    }

    private fun KotlinLogger.logFileRead(path: String) {
        info("<= $path")
    }

    private fun KotlinLogger.logFileWrite(path: String) {
        info("=> $path")
    }
}

