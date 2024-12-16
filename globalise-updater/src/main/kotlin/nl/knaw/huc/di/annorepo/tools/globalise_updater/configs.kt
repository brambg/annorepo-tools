package nl.knaw.huc.di.annorepo.tools.globalise_updater

import kotlinx.serialization.Serializable

@Serializable
data class UpdaterConfig(
    val mongo: MongoConfig,
    val languageDataFilePath: String
)

@Serializable
data class MongoConfig(
    val url: String,
    val database: String,
    val collection: String
)
