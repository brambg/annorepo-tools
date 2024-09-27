package nl.knaw.huc.di.annorepo.tools.updater

import kotlin.system.exitProcess
import com.mongodb.MongoException
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.conversions.Bson

object GlobaliseUpdater {

    fun main(args: Array<String>) {
        if (args.size < 2) {
            println("usage: ${args[0]} config-file")
            exitProcess(-1)
        }
        val configPath = args[0]
        val config = ConfigFactory.fromPath(configPath)
//        val mongoClient = KMongo.createClient(config.mongo.url)
//        val mdb = mongoClient.getDatabase(config.mongo.database)
//        val collection = mdb.getCollection<Document>(config.mongo.collection)
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

    private fun doUpdates(collection: MongoCollection<Document>, config: UpdaterConfig) {
        val updateGroups = loadUpdateGroups(config.languageDataFilePath)
        runBlocking {
            updateGroups.forEach {
                launch {
                    val query: Bson = Filters.`in`("annotation.body.metadata.document", it.pageIds)
                    val updates = Updates.combine(
                        Updates.set("annotation.body.metadata.lang", it.languages),
                        Updates.set("annotation.body.metadata.langCorrected", it.corrected)
                    )
                    try {
                        val result = collection.updateMany(query, updates)
                        println("Modified document count: ${result.modifiedCount}/${it.pageIds.size}")
                    } catch (me: MongoException) {
                        System.err.println("Unable to update due to an error: $me")
                    }
                }
            }
        }
    }

    private fun loadUpdateGroups(languageDataFilePath: String): List<UpdateGroup> {

        return listOf()

    }
}