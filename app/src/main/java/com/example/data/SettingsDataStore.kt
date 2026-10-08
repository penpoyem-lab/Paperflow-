package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

private val Context.dataStore by preferencesDataStore(name = "glasspaper_settings")

enum class AppThemeOption { LIGHT, DARK, SYSTEM, GALACTIC_CODEX, NOTHING_OS_GLASS }
enum class AppStyleOption(val label: String) {
    NOTHING_UI("Nothing UI"),
    APPLE_UI("Apple UI"),
    JOURNEY_AWAITS_UI("Journey Awaits UI")
}
enum class ThemeModeOption(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
enum class ReaderThemeOption { LIGHT, SEPIA, DARK }
enum class TextSizeOption { SMALL, MEDIUM, LARGE }
enum class PageLayoutOption { SINGLE_PAGE, CONTINUOUS, TWO_PAGE }
enum class AiButtonSizeOption(val label: String, val sizeDp: Int) {
    COMPACT("Compact", 48),
    MEDIUM("Standard", 56),
    LARGE("Large", 64)
}

data class AppSettings(
    val appTheme: AppThemeOption = AppThemeOption.NOTHING_OS_GLASS,
    val appStyle: AppStyleOption = AppStyleOption.NOTHING_UI,
    val themeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    val readerTheme: ReaderThemeOption = ReaderThemeOption.LIGHT,
    val textSize: TextSizeOption = TextSizeOption.MEDIUM,
    val pageLayout: PageLayoutOption = PageLayoutOption.CONTINUOUS,
    val autoRotate: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val rememberReadingPosition: Boolean = true,
    val hapticFeedback: Boolean = true,
    val defaultDownloadLocation: String = "Documents/Paperflow",
    val isGridViewInLibrary: Boolean = true,
    val aiAssistantEnabled: Boolean = true,
    val aiHideWhileReadingPdf: Boolean = false,
    val aiButtonSize: AiButtonSizeOption = AiButtonSizeOption.MEDIUM,
    val aiButtonOpacity: Float = 0.94f,
    val aiButtonNormalizedX: Float = 1.0f, // 0f = left edge, 1f = right edge, or intermediate on top/bottom
    val aiButtonNormalizedY: Float = 0.72f // 0f = top edge, 1f = bottom edge
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
        val APP_STYLE = stringPreferencesKey("app_style")
        val THEME_MODE = stringPreferencesKey("theme_mode")
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

        // Floating AI Assistant Persistence Keys
        val AI_ENABLED = booleanPreferencesKey("ai_assistant_enabled")
        val AI_HIDE_IN_PDF = booleanPreferencesKey("ai_hide_while_reading_pdf")
        val AI_BUTTON_SIZE = stringPreferencesKey("ai_button_size")
        val AI_BUTTON_OPACITY = floatPreferencesKey("ai_button_opacity")
        val AI_POS_X = floatPreferencesKey("ai_button_norm_x")
        val AI_POS_Y = floatPreferencesKey("ai_button_norm_y")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val rawTheme = prefs[Keys.APP_THEME]?.let { runCatching { AppThemeOption.valueOf(it) }.getOrNull() }
            ?: AppThemeOption.NOTHING_OS_GLASS
        val resolvedStyle = prefs[Keys.APP_STYLE]?.let { runCatching { AppStyleOption.valueOf(it) }.getOrNull() }
            ?: when (rawTheme) {
                AppThemeOption.NOTHING_OS_GLASS -> AppStyleOption.NOTHING_UI
                AppThemeOption.GALACTIC_CODEX -> AppStyleOption.JOURNEY_AWAITS_UI
                else -> AppStyleOption.NOTHING_UI
            }
        val resolvedThemeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeModeOption.valueOf(it) }.getOrNull() }
            ?: when (rawTheme) {
                AppThemeOption.LIGHT -> ThemeModeOption.LIGHT
                AppThemeOption.DARK -> ThemeModeOption.DARK
                else -> ThemeModeOption.SYSTEM
            }

        AppSettings(
            appTheme = rawTheme,
            appStyle = resolvedStyle,
            themeMode = resolvedThemeMode,
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
            isGridViewInLibrary = prefs[Keys.LIBRARY_GRID] ?: false,
            aiAssistantEnabled = prefs[Keys.AI_ENABLED] ?: true,
            aiHideWhileReadingPdf = prefs[Keys.AI_HIDE_IN_PDF] ?: false,
            aiButtonSize = prefs[Keys.AI_BUTTON_SIZE]?.let { runCatching { AiButtonSizeOption.valueOf(it) }.getOrNull() }
                ?: AiButtonSizeOption.MEDIUM,
            aiButtonOpacity = (prefs[Keys.AI_BUTTON_OPACITY] ?: 0.94f).coerceIn(0.35f, 1.0f),
            aiButtonNormalizedX = (prefs[Keys.AI_POS_X] ?: 1.0f).coerceIn(0f, 1f),
            aiButtonNormalizedY = (prefs[Keys.AI_POS_Y] ?: 0.72f).coerceIn(0f, 1f)
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
        val todayStr = getTodayIsoDate()
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

            val daysBetween = if (lastDateStr.isNotBlank()) {
                daysBetweenIsoDates(lastDateStr, todayStr)
            } else null

            val newStreak = when {
                daysBetween == null -> 1 // First-ever app open
                daysBetween == 1L -> prevStreak + 1 // Consecutive calendar day
                daysBetween <= 0L -> prevStreak.coerceAtLeast(1)
                else -> 1 // Missed 1 or more full calendar days -> reset active streak to 1
            }

            val animatedFromStreak = when {
                daysBetween == null -> 0
                daysBetween == 1L -> prevStreak
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
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = option.name
            when (option) {
                AppThemeOption.NOTHING_OS_GLASS -> {
                    prefs[Keys.APP_STYLE] = AppStyleOption.NOTHING_UI.name
                }
                AppThemeOption.GALACTIC_CODEX -> {
                    prefs[Keys.APP_STYLE] = AppStyleOption.JOURNEY_AWAITS_UI.name
                }
                AppThemeOption.LIGHT -> {
                    prefs[Keys.APP_STYLE] = AppStyleOption.APPLE_UI.name
                    prefs[Keys.THEME_MODE] = ThemeModeOption.LIGHT.name
                }
                AppThemeOption.DARK -> {
                    prefs[Keys.APP_STYLE] = AppStyleOption.APPLE_UI.name
                    prefs[Keys.THEME_MODE] = ThemeModeOption.DARK.name
                }
                AppThemeOption.SYSTEM -> {
                    prefs[Keys.THEME_MODE] = ThemeModeOption.SYSTEM.name
                }
            }
        }
    }

    suspend fun setAppStyle(style: AppStyleOption) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_STYLE] = style.name
            val currentMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeModeOption.valueOf(it) }.getOrNull() }
                ?: ThemeModeOption.SYSTEM
            prefs[Keys.APP_THEME] = when (style) {
                AppStyleOption.NOTHING_UI -> AppThemeOption.NOTHING_OS_GLASS.name
                AppStyleOption.JOURNEY_AWAITS_UI -> AppThemeOption.GALACTIC_CODEX.name
                AppStyleOption.APPLE_UI -> when (currentMode) {
                    ThemeModeOption.LIGHT -> AppThemeOption.LIGHT.name
                    ThemeModeOption.DARK -> AppThemeOption.DARK.name
                    ThemeModeOption.SYSTEM -> AppThemeOption.SYSTEM.name
                }
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeModeOption) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
            val currentStyle = prefs[Keys.APP_STYLE]?.let { runCatching { AppStyleOption.valueOf(it) }.getOrNull() }
                ?: AppStyleOption.NOTHING_UI
            if (currentStyle == AppStyleOption.APPLE_UI) {
                prefs[Keys.APP_THEME] = when (mode) {
                    ThemeModeOption.LIGHT -> AppThemeOption.LIGHT.name
                    ThemeModeOption.DARK -> AppThemeOption.DARK.name
                    ThemeModeOption.SYSTEM -> AppThemeOption.SYSTEM.name
                }
            }
        }
    }

    suspend fun resetThemeToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_STYLE] = AppStyleOption.NOTHING_UI.name
            prefs[Keys.THEME_MODE] = ThemeModeOption.SYSTEM.name
            prefs[Keys.APP_THEME] = AppThemeOption.NOTHING_OS_GLASS.name
        }
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

    suspend fun setAiAssistantEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AI_ENABLED] = enabled }
    }

    suspend fun setAiHideWhileReadingPdf(hide: Boolean) {
        context.dataStore.edit { it[Keys.AI_HIDE_IN_PDF] = hide }
    }

    suspend fun setAiButtonSize(size: AiButtonSizeOption) {
        context.dataStore.edit { it[Keys.AI_BUTTON_SIZE] = size.name }
    }

    suspend fun setAiButtonOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.AI_BUTTON_OPACITY] = opacity.coerceIn(0.35f, 1.0f) }
    }

    suspend fun setAiButtonPosition(normalizedX: Float, normalizedY: Float) {
        context.dataStore.edit {
            it[Keys.AI_POS_X] = normalizedX.coerceIn(0f, 1f)
            it[Keys.AI_POS_Y] = normalizedY.coerceIn(0f, 1f)
        }
    }

    suspend fun resetAiButtonPosition() {
        context.dataStore.edit {
            it[Keys.AI_POS_X] = 1.0f
            it[Keys.AI_POS_Y] = 0.72f
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        val MILESTONE_DAYS = listOf(3, 7, 14, 30, 50, 100, 365)

        fun getTodayIsoDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Calendar.getInstance().time)
        }

        fun daysBetweenIsoDates(startIso: String, endIso: String): Long? {
            return runCatching {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val startDate = sdf.parse(startIso) ?: return null
                val endDate = sdf.parse(endIso) ?: return null
                val startCal = Calendar.getInstance().apply {
                    time = startDate
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    time = endDate
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val diffMillis = endCal.timeInMillis - startCal.timeInMillis
                (diffMillis / (24.0 * 60.0 * 60.0 * 1000.0)).roundToLong()
            }.getOrNull()
        }

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
