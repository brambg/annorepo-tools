package nl.knaw.huc.di.annorepo.tools.republic_updater

import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.useLines
import kotlin.system.exitProcess
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.google.common.base.Stopwatch
import com.mongodb.MongoException
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import org.bson.Document
import org.bson.conversions.Bson
import org.slf4j.LoggerFactory

object RepublicUpdater {

    val size = 6479310

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

    private fun doUpdates(collection: MongoCollection<Document>, config: UpdaterConfig) {
        val provenanceDataFilePath = config.provenanceDataFilePath
        logger.logFileRead(provenanceDataFilePath)
        val progressKeeper = ProgressKeeper(size)

        Path(provenanceDataFilePath).useLines { lines ->
            lines.forEachIndexed { i, line ->
                progressKeeper.showProgress(i + 1)
                val (annotationId, provenanceNum) = line.split(";")
                val provUrl = "https://provenance.sd.di.huc.knaw.nl/prov/$provenanceNum"
                val query: Bson = Filters.eq("annotation_name", annotationId)
                val updates = Updates.set("annotation.provenance", provUrl)
                try {
                    val result = collection.updateOne(query, updates)
                    if (result.matchedCount == 1L) {
                        logger.info { "provenance set to $provUrl for ${config.annorepo.url}/w3c/${config.mongo.collection}/$annotationId" }
                    } else {
                        logger.warn { "annotation not found: ${config.annorepo.url}/w3c/${config.mongo.collection}/$annotationId" }
                    }
                } catch (me: MongoException) {
                    logger.error { "Unable to update due to an error: $me" }
                }
            }
        }
    }

    private fun KotlinLogger.logFileRead(path: String) {
        info("<= $path")
    }

    private fun KotlinLogger.logFileWrite(path: String) {
        info("=> $path")
    }

    class ProgressKeeper(private val total: Int) {
        private val totalDouble = total.toDouble()
        private val stopwatch: Stopwatch = Stopwatch.createStarted()

        fun showProgress(currentCount: Int) {
            if (currentCount % 100 == 0) {
                val microseconds = stopwatch.elapsed(TimeUnit.MICROSECONDS)
                val elapsedMicroseconds = formatMicroseconds(microseconds)
                val percentage = if (total > 0) {
                    (currentCount * 100) / totalDouble
                } else {
                    0.toDouble()
                }
                val etaString = if (currentCount > 0) {
                    val eta = (microseconds * total) / currentCount
                    formatMicroseconds(eta)
                } else {
                    "??:??:??"
                }

                logger.info { "updating record $currentCount/$size (${percentage.format(2)}%) | $elapsedMicroseconds eta: $etaString" }
            }
        }

        private fun Double.format(scale: Int) = "%.${scale}f".format(this)
        private fun formatMicroseconds(microseconds: Long): String {
            val totalSeconds = microseconds / 1_000_000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}

