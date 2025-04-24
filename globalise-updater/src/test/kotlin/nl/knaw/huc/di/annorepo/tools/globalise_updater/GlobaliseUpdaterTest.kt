package nl.knaw.huc.di.annorepo.tools.globalise_updater

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class GlobaliseUpdaterTest {

    @Test
    fun main() {
        GlobaliseUpdater.main(arrayOf("conf/local.yml"))
    }

    @Test
    fun `show languages used`() {
        val languageRecords = Languages.loadLanguageRecords("data/pages.lang.tsv")
        val languageSet = languageRecords.flatMap { it.languages }.toSet()
        println(languageSet.sorted())
        println("languages not used in pages.lang: ${Languages.languageLabels.keys - languageSet}")
        println("languages without a label:        ${languageSet - Languages.languageLabels.keys}")
        assertTrue(Languages.languageLabels.keys.containsAll(languageSet))
    }

}