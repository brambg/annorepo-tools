package nl.knaw.huc.di.annorepo.tools.updater

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import com.google.common.base.Stopwatch
import kotlinx.coroutines.delay
import org.apache.logging.log4j.kotlin.logger

class ProgressKeeper(private val total: Long, private val delay: Long) {
    private val recordsProcessed: AtomicInteger = AtomicInteger(0)
    private val modifiedDocuments: AtomicLong = AtomicLong(0)
    private val errors: AtomicInteger = AtomicInteger(0)
    private val updateIsFinished: AtomicBoolean = AtomicBoolean(false)
    private val stopwatch: Stopwatch = Stopwatch.createStarted()
    private val totalDouble = total.toDouble()

    suspend fun showProgress() {
        while (!updateIsFinished.get()) {
            showProgressLine()
            delay(timeMillis = delay)
        }
        showProgressLine()
    }

    fun incRecordsProcessed(delta: Int) {
        recordsProcessed.addAndGet(delta)
    }

    fun incModifiedDocuments(delta: Long) {
        modifiedDocuments.addAndGet(delta)
    }

    fun incErrors() {
        errors.incrementAndGet()
    }

    fun stop() {
        updateIsFinished.set(true)
        stopwatch.stop()
    }

    private fun showProgressLine() {
        val currentCount = recordsProcessed.get()
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
        logger.info { "${currentCount}/$total page records processed ( ${percentage.format(2)}% ) | $elapsedMicroseconds eta: $etaString | ${modifiedDocuments.get()} page annotations modified | ${errors.get()} errors" }
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