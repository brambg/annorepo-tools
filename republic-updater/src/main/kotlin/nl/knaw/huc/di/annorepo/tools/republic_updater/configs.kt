package nl.knaw.huc.di.annorepo.tools.republic_updater

import kotlinx.serialization.Serializable

@Serializable
data class UpdaterConfig(
    val mongo: MongoConfig,
    val annorepo: AnnoRepoConfig,
    val provenanceDataFilePath: String,
)

@Serializable
data class MongoConfig(
    val url: String,
    val database: String,
    val collection: String
)

@Serializable
data class AnnoRepoConfig(
    val url: String
)