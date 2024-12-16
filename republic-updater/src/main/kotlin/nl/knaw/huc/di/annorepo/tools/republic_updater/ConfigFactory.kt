package nl.knaw.huc.di.annorepo.tools.republic_updater

import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlinx.serialization.decodeFromString

object ConfigFactory {
    private val yamlDefault = com.charleskorn.kaml.Yaml.default

    fun fromPath(configPath: String): UpdaterConfig {
        val yaml = Path(configPath).readText()
        return yamlDefault.decodeFromString(yaml)
    }
}