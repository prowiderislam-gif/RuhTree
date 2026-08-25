package com.example.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            LocalDate.parse(dateStr.trim(), dateFormatter)
        } catch (e: Exception) {
            null
        }
    }

    fun parseTime(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            LocalTime.parse(timeStr.trim(), timeFormatter)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate?): String {
        if (date == null) return ""
        return try {
            date.format(dateFormatter)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDisplayDate(dateStr: String?): String {
        val date = parseDate(dateStr) ?: return dateStr ?: ""
        return try {
            date.format(displayDateFormatter)
        } catch (e: Exception) {
            dateStr ?: ""
        }
    }

    fun formatDisplayTime(timeStr: String?): String {
        val time = parseTime(timeStr) ?: return timeStr ?: ""
        return try {
            time.format(displayTimeFormatter)
        } catch (e: Exception) {
            timeStr ?: ""
        }
    }

    /**
     * Calculates current age or age at death.
     */
    fun calculateAge(
        dateOfBirthStr: String?,
        isDeceased: Boolean = false,
        dateOfDeathStr: String? = null,
        referenceDate: LocalDate = LocalDate.now()
    ): AgeResult? {
        val dob = parseDate(dateOfBirthStr) ?: return null
        val endDate = if (isDeceased) {
            parseDate(dateOfDeathStr) ?: referenceDate
        } else {
            referenceDate
        }

        if (dob.isAfter(endDate)) {
            return AgeResult(years = 0, months = 0, days = 0, isDeceased = isDeceased)
        }

        val period = Period.between(dob, endDate)
        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            isDeceased = isDeceased
        )
    }

    /**
     * Formats calculated age into human readable text.
     */
    fun formatAgeString(
        dateOfBirthStr: String?,
        isDeceased: Boolean = false,
        dateOfDeathStr: String? = null
    ): String {
        val ageResult = calculateAge(dateOfBirthStr, isDeceased, dateOfDeathStr) ?: return "Age Unknown"
        val years = ageResult.years
        val months = ageResult.months
        val days = ageResult.days

        val ageText = when {
            years > 0 && months > 0 -> "$years yrs, $months mos"
            years > 0 -> "$years years old"
            months > 0 -> "$months months, $days days"
            days > 0 -> "$days days old"
            else -> "Newborn"
        }

        return if (isDeceased) {
            "Lived $ageText (Deceased)"
        } else {
            ageText
        }
    }

    /**
     * Formats calculated age into short format like "33 yrs, 4 mos" or "10 yrs"
     */
    fun formatShortAgeString(
        dateOfBirthStr: String?,
        isDeceased: Boolean = false,
        dateOfDeathStr: String? = null
    ): String? {
        val ageResult = calculateAge(dateOfBirthStr, isDeceased, dateOfDeathStr) ?: return null
        val years = ageResult.years
        val months = ageResult.months
        val days = ageResult.days

        val text = when {
            years > 0 && months > 0 -> "$years yrs, $months mos"
            years > 0 -> "$years yrs"
            months > 0 && days > 0 -> "$months mos, $days d"
            months > 0 -> "$months mos"
            days > 0 -> "$days d"
            else -> "Newborn"
        }

        return if (isDeceased) "$text (Dec)" else text
    }

    /**
     * Calculates age difference between two members.
     * Returns: (olderMemberIndex 1 or 2, formatted difference string)
     */
    fun calculateAgeDifference(
        dobStr1: String?,
        dobStr2: String?
    ): AgeDifferenceResult {
        val dob1 = parseDate(dobStr1)
        val dob2 = parseDate(dobStr2)

        if (dob1 == null || dob2 == null) {
            return AgeDifferenceResult(
                hasExactDates = false,
                firstIsOlder = true,
                differenceText = "Birth date not available for exact comparison",
                years = 0,
                months = 0,
                days = 0
            )
        }

        if (dob1.isEqual(dob2)) {
            return AgeDifferenceResult(
                hasExactDates = true,
                firstIsOlder = true,
                differenceText = "Same age (Born on the same day)",
                years = 0,
                months = 0,
                days = 0
            )
        }

        val firstIsOlder = dob1.isBefore(dob2)
        val olderDob = if (firstIsOlder) dob1 else dob2
        val youngerDob = if (firstIsOlder) dob2 else dob1

        val period = Period.between(olderDob, youngerDob)
        val parts = mutableListOf<String>()
        if (period.years > 0) parts.add("${period.years} year${if (period.years > 1) "s" else ""}")
        if (period.months > 0) parts.add("${period.months} month${if (period.months > 1) "s" else ""}")
        if (period.days > 0 && period.years == 0) parts.add("${period.days} day${if (period.days > 1) "s" else ""}")

        val diffText = if (parts.isEmpty()) "Less than a day" else parts.joinToString(", ")

        return AgeDifferenceResult(
            hasExactDates = true,
            firstIsOlder = firstIsOlder,
            differenceText = "$diffText difference",
            years = period.years,
            months = period.months,
            days = period.days
        )
    }
}

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val isDeceased: Boolean
)

data class AgeDifferenceResult(
    val hasExactDates: Boolean,
    val firstIsOlder: Boolean,
    val differenceText: String,
    val years: Int,
    val months: Int,
    val days: Int
)
