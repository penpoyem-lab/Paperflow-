package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val Context.dataStore by preferencesDataStore(name = "glasspaper_settings")

enum class AppThemeOption { LIGHT, DARK, SYSTEM, GALACTIC_CODEX }
enum class ReaderThemeOption { LIGHT, SEPIA, DARK }
enum class TextSizeOption { SMALL, MEDIUM, LARGE }
enum class PageLayoutOption { SINGLE_PAGE, CONTINUOUS, TWO_PAGE }

data class AppSettings(
    val appTheme: AppThemeOption = AppThemeOption.LIGHT,
    val readerTheme: ReaderThemeOption = ReaderThemeOption.LIGHT,
    val textSize: TextSizeOption = TextSizeOption.MEDIUM,
    val pageLayout: PageLayoutOption = PageLayoutOption.CONTINUOUS,
    val autoRotate: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val rememberReadingPosition: Boolean = true,
    val hapticFeedback: Boolean = true,
    val defaultDownloadLocation: String = "Documents/Paperflow",
    val isGridViewInLibrary: Boolean = true
)

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckInDate: String = "", // ISO yyyy-MM-dd
    val checkInHistoryDates: Set<String> = emptySet(),
    val unlockedMilestones: Set<Int> = emptySet()
)

data class StreakCheckInEvent(
    val previousStreak: Int,
    val newStreak: Int,
    val longestStreak: Int,
    val isMilestone: Boolean,
    val milestoneTitle: String?,
    val subtitleMessage: String,
    val checkInDate: String
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val PAGE_LAYOUT = stringPreferencesKey("page_layout")
        val AUTO_ROTATE = booleanPreferencesKey("auto_rotate")
        val KEEP_AWAKE = booleanPreferencesKey("keep_screen_awake")
        val REMEMBER_POS = booleanPreferencesKey("remember_reading_position")
        val HAPTIC = booleanPreferencesKey("haptic_feedback")
        val DOWNLOAD_LOC = stringPreferencesKey("default_download_location")
        val LIBRARY_GRID = booleanPreferencesKey("library_grid")

        // Daily Streak Persistence Keys
        val CURRENT_STREAK = intPreferencesKey("streak_current")
        val LONGEST_STREAK = intPreferencesKey("streak_longest")
        val LAST_CHECK_IN_DATE = stringPreferencesKey("streak_last_date")
        val CHECK_IN_HISTORY = stringPreferencesKey("streak_history_dates")
        val UNLOCKED_MILESTONES = stringPreferencesKey("streak_unlocked_milestones")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            appTheme = prefs[Keys.APP_THEME]?.let { runCatching { AppThemeOption.valueOf(it) }.getOrNull() }
                ?: AppThemeOption.LIGHT,
            readerTheme = prefs[Keys.READER_THEME]?.let { runCatching { ReaderThemeOption.valueOf(it) }.getOrNull() }
                ?: ReaderThemeOption.LIGHT,
            textSize = prefs[Keys.TEXT_SIZE]?.let { runCatching { TextSizeOption.valueOf(it) }.getOrNull() }
                ?: TextSizeOption.MEDIUM,
            pageLayout = prefs[Keys.PAGE_LAYOUT]?.let { runCatching { PageLayoutOption.valueOf(it) }.getOrNull() }
                ?: PageLayoutOption.CONTINUOUS,
            autoRotate = prefs[Keys.AUTO_ROTATE] ?: true,
            keepScreenAwake = prefs[Keys.KEEP_AWAKE] ?: true,
            rememberReadingPosition = prefs[Keys.REMEMBER_POS] ?: true,
            hapticFeedback = prefs[Keys.HAPTIC] ?: true,
            defaultDownloadLocation = prefs[Keys.DOWNLOAD_LOC] ?: "Documents/Paperflow",
            isGridViewInLibrary = prefs[Keys.LIBRARY_GRID] ?: false
        )
    }

    val streakFlow: Flow<StreakData> = context.dataStore.data.map { prefs ->
        val current = prefs[Keys.CURRENT_STREAK] ?: 0
        val longest = prefs[Keys.LONGEST_STREAK] ?: 0
        val lastDate = prefs[Keys.LAST_CHECK_IN_DATE] ?: ""
        val historyRaw = prefs[Keys.CHECK_IN_HISTORY] ?: ""
        val milestonesRaw = prefs[Keys.UNLOCKED_MILESTONES] ?: ""

        val historySet = historyRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val milestonesSet = milestonesRaw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

        StreakData(
            currentStreak = current,
            longestStreak = longest,
            lastCheckInDate = lastDate,
            checkInHistoryDates = historySet,
            unlockedMilestones = milestonesSet
        )
    }

    /**
     * Evaluates the device's local calendar date and performs a real daily check-in if not already checked in today.
     * Returns a [StreakCheckInEvent] ONLY when a new daily check-in is registered today; returns null if already checked in today.
     */
    suspend fun registerDailyCheckInIfNeeded(): StreakCheckInEvent? {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        var checkInEvent: StreakCheckInEvent? = null

        context.dataStore.edit { prefs ->
            val prevStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            val prevLongest = prefs[Keys.LONGEST_STREAK] ?: 0
            val lastDateStr = prefs[Keys.LAST_CHECK_IN_DATE] ?: ""

            if (lastDateStr == todayStr && prevStreak > 0) {
                // Already checked in on this calendar day — count only once
                checkInEvent = null
                return@edit
            }

            val parsedLastDate = if (lastDateStr.isNotBlank()) {
                runCatching { LocalDate.parse(lastDateStr, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
            } else null

            val newStreak = when {
                parsedLastDate == null -> 1 // First-ever app open
                ChronoUnit.DAYS.between(parsedLastDate, today) == 1L -> prevStreak + 1 // Consecutive calendar day
                ChronoUnit.DAYS.between(parsedLastDate, today) <= 0L -> prevStreak.coerceAtLeast(1)
                else -> 1 // Missed 1 or more full calendar days -> reset active streak to 1
            }

            val animatedFromStreak = when {
                parsedLastDate == null -> 0
                ChronoUnit.DAYS.between(parsedLastDate, today) == 1L -> prevStreak
                else -> 0
            }

            val newLongest = maxOf(prevLongest, newStreak)

            val existingHistory = (prefs[Keys.CHECK_IN_HISTORY] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableSet()
            existingHistory.add(todayStr)
            // Keep most recent 120 dates to stay lightweight
            val trimmedHistory = existingHistory.sorted().takeLast(120)

            val existingMilestones = (prefs[Keys.UNLOCKED_MILESTONES] ?: "")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableSet()

            val milestoneTargets = setOf(3, 7, 14, 30, 50, 100, 365)
            val isMilestone = milestoneTargets.contains(newStreak)
            if (isMilestone) {
                existingMilestones.add(newStreak)
            }

            prefs[Keys.CURRENT_STREAK] = newStreak
            prefs[Keys.LONGEST_STREAK] = newLongest
            prefs[Keys.LAST_CHECK_IN_DATE] = todayStr
            prefs[Keys.CHECK_IN_HISTORY] = trimmedHistory.joinToString(",")
            prefs[Keys.UNLOCKED_MILESTONES] = existingMilestones.sorted().joinToString(",")

            val milestoneBanner = getMilestoneTitle(newStreak)
            val subtitle = getStreakSubtitle(newStreak)

            checkInEvent = StreakCheckInEvent(
                previousStreak = animatedFromStreak,
                newStreak = newStreak,
                longestStreak = newLongest,
                isMilestone = isMilestone,
                milestoneTitle = milestoneBanner,
                subtitleMessage = subtitle,
                checkInDate = todayStr
            )
        }

        return checkInEvent
    }

    suspend fun setAppTheme(option: AppThemeOption) {
        context.dataStore.edit { it[Keys.APP_THEME] = option.name }
    }

    suspend fun setReaderTheme(option: ReaderThemeOption) {
        context.dataStore.edit { it[Keys.READER_THEME] = option.name }
    }

    suspend fun setTextSize(option: TextSizeOption) {
        context.dataStore.edit { it[Keys.TEXT_SIZE] = option.name }
    }

    suspend fun setPageLayout(option: PageLayoutOption) {
        context.dataStore.edit { it[Keys.PAGE_LAYOUT] = option.name }
    }

    suspend fun setAutoRotate(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ROTATE] = enabled }
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_AWAKE] = enabled }
    }

    suspend fun setRememberReadingPosition(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMEMBER_POS] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC] = enabled }
    }

    suspend fun setDefaultDownloadLocation(location: String) {
        context.dataStore.edit { it[Keys.DOWNLOAD_LOC] = location }
    }

    suspend fun setLibraryGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.LIBRARY_GRID] = isGrid }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        val MILESTONE_DAYS = listOf(3, 7, 14, 30, 50, 100, 365)

        fun getMilestoneTitle(days: Int): String? = when (days) {
            3 -> "3 DAYS — GREAT START!"
            7 -> "7 DAYS — ONE WEEK STRONG!"
            14 -> "14 DAYS — TWO WEEKS UNSTOPPABLE!"
            30 -> "30 DAYS — INCREDIBLE!"
            50 -> "50 DAYS — MASTER READER!"
            100 -> "100 DAYS — LEGENDARY!"
            365 -> "365 DAYS — FULL YEAR CROWN!"
            else -> null
        }

        fun getStreakSubtitle(days: Int): String = when {
            days >= 100 -> "Legendary consistency! Your focus is unmatched."
            days >= 30 -> "A full month of daily reading & learning!"
            days >= 7 -> "You're on fire! Keep the momentum flowing."
            days >= 3 -> "Great habit building! Every day counts."
            days == 2 -> "Back for Day 2! Keep your reading flow alive."
            else -> "Your daily reading & learning journey begins today."
        }
    }
}
