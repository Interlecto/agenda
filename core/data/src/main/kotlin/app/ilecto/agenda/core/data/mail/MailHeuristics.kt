package app.ilecto.agenda.core.data.mail

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Locale
import java.util.regex.Pattern

/**
 * Phase 3: a deliberately narrow, local-only heuristic — no NLP/LLM. A message's subject +
 * snippet must contain *both* a confirmation-style keyword and an explicitly-formatted date to
 * become a candidate; this won't catch every phrasing (relative dates like "next Tuesday",
 * non-Latin scripts, unusual date formats) by design — see Phase 3's plan notes for why that's
 * an accepted scope cut, not an oversight. Pure/stateless: no network or database access.
 */
object MailHeuristics {

    data class Match(val start: Instant, val end: Instant, val isAllDay: Boolean)

    /** Returns null if the combined subject+snippet doesn't look like an actionable event. */
    fun match(subject: String, snippet: String): Match? {
        val text = "$subject $snippet"
        if (!containsKeyword(text)) return null
        val date = extractDate(text) ?: return null
        val time = extractTime(text)
        return if (time == null) {
            val start = date.atStartOfDay(ZoneOffset.UTC).toInstant()
            Match(start = start, end = start.plusSeconds(SECONDS_PER_DAY), isAllDay = true)
        } else {
            val start = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)
            Match(start = start, end = start.plusSeconds(SECONDS_PER_HOUR), isAllDay = false)
        }
    }

    private fun containsKeyword(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return KEYWORDS.any { lower.contains(it) }
    }

    private fun extractDate(text: String): LocalDate? {
        ISO_DATE.matcher(text).let { m ->
            if (m.find()) {
                return runCatching { LocalDate.of(m.group(1)!!.toInt(), m.group(2)!!.toInt(), m.group(3)!!.toInt()) }
                    .getOrNull()
            }
        }
        MONTH_DAY_YEAR.matcher(text).let { m ->
            if (m.find()) {
                val month = MONTH_ABBREVIATIONS[m.group(1)!!.take(3).lowercase(Locale.ROOT)]
                if (month != null) {
                    return runCatching { LocalDate.of(m.group(3)!!.toInt(), month, m.group(2)!!.toInt()) }.getOrNull()
                }
            }
        }
        // Assumed DD/MM/YYYY (matches this app's Spanish-speaking audience as much as English) —
        // a genuine ambiguity against US-style MM/DD/YYYY with no reliable way to disambiguate
        // from the text alone; an accepted limitation, not a bug.
        SLASH_DATE.matcher(text).let { m ->
            if (m.find()) {
                return runCatching { LocalDate.of(m.group(3)!!.toInt(), m.group(2)!!.toInt(), m.group(1)!!.toInt()) }
                    .getOrNull()
            }
        }
        return null
    }

    private fun extractTime(text: String): LocalTime? {
        TIME_12H.matcher(text).let { m ->
            if (m.find()) {
                var hour = m.group(1)!!.toInt() % 12
                if (m.group(3)!!.lowercase(Locale.ROOT) == "pm") hour += 12
                return runCatching { LocalTime.of(hour, m.group(2)!!.toInt()) }.getOrNull()
            }
        }
        TIME_24H.matcher(text).let { m ->
            if (m.find()) {
                return runCatching { LocalTime.of(m.group(1)!!.toInt(), m.group(2)!!.toInt()) }.getOrNull()
            }
        }
        return null
    }

    private const val SECONDS_PER_HOUR = 3_600L
    private const val SECONDS_PER_DAY = 86_400L

    private val KEYWORDS = listOf(
        // English
        "confirmed", "confirmation", "booking", "reservation", "itinerary", "boarding pass",
        "e-ticket", "eticket", "appointment", "invitation", "deadline", "due date", "reminder:",
        "check-in", "your flight", "your trip", "your order",
        // Spanish
        "confirmación", "confirmado", "reserva", "itinerario", "cita", "invitación",
        "fecha límite", "recordatorio", "tu vuelo", "tu viaje", "tu pedido",
    )

    private val MONTH_ABBREVIATIONS = mapOf(
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
        "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
    )

    private val MONTH_DAY_YEAR = Pattern.compile(
        "\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\.?\\s+(\\d{1,2}),?\\s+(\\d{4})\\b",
        Pattern.CASE_INSENSITIVE,
    )
    private val ISO_DATE = Pattern.compile("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b")
    private val SLASH_DATE = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b")
    private val TIME_12H = Pattern.compile("\\b(1[0-2]|0?[1-9]):([0-5]\\d)\\s?([AaPp][Mm])\\b")
    private val TIME_24H = Pattern.compile("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b")
}
