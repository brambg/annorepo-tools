package nl.knaw.huc.di.annorepo.tools.globalise_updater

import org.apache.logging.log4j.kotlin.KotlinLogger

fun KotlinLogger.logFileRead(path: String) {
    info("<= $path")
}

fun KotlinLogger.logFileWrite(path: String) {
    info("=> $path")
}
