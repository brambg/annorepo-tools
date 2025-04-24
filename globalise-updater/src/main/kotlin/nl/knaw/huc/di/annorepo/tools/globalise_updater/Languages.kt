package nl.knaw.huc.di.annorepo.tools.globalise_updater

import kotlin.io.path.Path
import kotlin.io.path.useLines
import org.apache.logging.log4j.kotlin.logger

object Languages {
    val languageLabels = mapOf(
        "ben" to "Bengali",
        "dan" to "Danish",
        "deu" to "German",
        "eng" to "English",
        "fra" to "French",
        "nld" to "Dutch",
        "bug" to "Buginese",
        "jpn" to "Japanese",
        "ota" to "Ottoman Turkish",
        "fas" to "Persian",
        "lat" to "Latin",
        "hbo" to "Ancient Hebrew",  // changed code from ‘heb’ which is modern Hebrew
        "gre" to "Ancient Greek",
        "ita" to "Italian",
        "msa" to "Malay",
        "por" to "Portuguese",
        "spa" to "Spanish",
        "art" to "Cipher",
        "chu" to "Old Church Slavonic",
        "lzh" to "Classical Chinese",  // changed label from Literary to Classical Chinese
        "sin" to "Sinhala",
        "tam" to "Tamil",
        "unknown" to "Unknown" // new; language not yet identified
    )

    data class LanguageRecord(
        val pageId: String,
        val languages: List<String>,
        val corrected: Boolean,
    )

    fun loadLanguageRecords(languageDataFilePath: String): List<LanguageRecord> {
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
                .toList()
        }
    }

}