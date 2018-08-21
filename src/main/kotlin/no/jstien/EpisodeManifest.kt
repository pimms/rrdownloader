package no.jstien

import java.text.SimpleDateFormat
import java.util.*

public class EpisodeManifest(url: String, published: String) {
    val url: String = url
    val published: Calendar = parseDate(published)

    fun getFormattedName() : String {
        val date = published.time
        val format = SimpleDateFormat("yyyyMMdd")
        return format.format(date) + ".mp3"
    }

    private fun parseDate(date: String) : Calendar {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        calendar.time = format.parse(date)
        return calendar
    }
}