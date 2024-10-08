package nl.knaw.huc.di.annorepo.tools.updater

import org.junit.jupiter.api.Test

class GlobaliseUpdaterTest {

    @Test
    fun main() {
        GlobaliseUpdater.main(arrayOf("conf/local.yml"))
    }

}