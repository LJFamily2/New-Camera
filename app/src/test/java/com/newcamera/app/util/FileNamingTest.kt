package com.newcamera.app.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FileNamingTest {

    @Test
    fun `file name matches the IMG_yyyyMMdd_HHmmss jpg pattern`() {
        val name = FileNaming.buildJpegFileName(0L)
        val pattern = Regex("""^IMG_\d{8}_\d{6}\.jpg$""")
        assertTrue("'$name' did not match expected pattern", pattern.matches(name))
    }

    @Test
    fun `file name encodes the given timestamp`() {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(2024, Calendar.JANUARY, 15, 9, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val name = FileNaming.buildJpegFileName(calendar.timeInMillis)
        assertTrue(name.startsWith("IMG_20240115_0930"))
        assertTrue(name.endsWith(".jpg"))
    }

    @Test
    fun `different timestamps produce different file names`() {
        val first = FileNaming.buildJpegFileName(0L)
        val second = FileNaming.buildJpegFileName(TimeUnitDaysMillis)
        assertTrue(first != second)
    }

    private companion object {
        const val TimeUnitDaysMillis = 24L * 60 * 60 * 1000
    }
}
