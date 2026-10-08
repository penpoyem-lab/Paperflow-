package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.AuthSessionState
import com.example.data.AppSettings
import com.example.data.AppThemeOption
import com.example.data.PageLayoutOption
import com.example.data.PdfDocumentEntity
import com.example.data.ReaderThemeOption
import com.example.data.TextSizeOption
import com.example.ui.GlassPaperViewModel
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.WarmGold

@Composable
fun SettingsScreen(
    viewModel: GlassPaperViewModel,
    settings: AppSettings,
    documents: List<PdfDocumentEntity>,
    authSession: AuthSessionState = AuthSessionState()
) {
    val glass = LocalGlassColors.current
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearAllDataDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    val totalLibraryBytes = remember(documents) {
        documents.sumOf { it.fileSizeBytes }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 118.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Customize your Liquid Glass reading & study environment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary
                )
            }
        }

        // 0. PAPERFLOW ACCOUNT & AUTHENTICATION
        item {
            SettingsSectionHeader("PAPERFLOW ACCOUNT", LiquidCyan)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = LiquidCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (authSession.isAuthenticated) {
                        SettingsInfoRow(
                            icon = Icons.Filled.VerifiedUser,
                            title = authSession.userFullName.ifBlank { "Paperflow Member" },
                            subtitle = "${authSession.userEmail} • Authenticated Session",
                            accent = EmeraldGreen,
                            onClick = { viewModel.openAuthentication() }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openAuthentication() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_switch_account_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Text("Account Security", color = Color.White)
                            }
                            TextButton(
                                onClick = { viewModel.signOut() },
                                modifier = Modifier.testTag("settings_sign_out_button")
                            ) {
                                Text("Sign Out", color = LiquidMagenta, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        SettingsInfoRow(
                            icon = Icons.Filled.AccountCircle,
                            title = "Sign in to Paperflow",
                            subtitle = "Access your encrypted Paperflow vault, profile & account security",
                            accent = LiquidCyan,
                            onClick = { viewModel.openAuthentication() }
                        )
                        Button(
                            onClick = { viewModel.openAuthentication() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_open_auth_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text(
                                text = "Sign In or Create Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 1. PHOTO THEMES & VISUAL EDITIONS (NEW SECTION)
        item {
            SettingsSectionHeader("PHOTO THEMES & VISUAL EDITIONS", WarmGold)
            Spacer(modifier = Modifier.height(8.dp))
            GalacticCodexPhotoThemeCard(
                selectedTheme = settings.appTheme,
                onSelectTheme = { viewModel.setAppTheme(it) }
            )
        }

        // 2. APPEARANCE
        item {
            SettingsSectionHeader("APPEARANCE", ElectricBlue)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = ElectricBlue
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    GlassSegmentedControl(
                        options = listOf(
                            AppThemeOption.LIGHT to "Light",
                            AppThemeOption.DARK to "Dark",
                            AppThemeOption.SYSTEM to "System",
                            AppThemeOption.GALACTIC_CODEX to "Galactic"
                        ),
                        selected = settings.appTheme,
                        accentColor = if (settings.appTheme == AppThemeOption.GALACTIC_CODEX) WarmGold else ElectricBlue,
                        onSelect = { viewModel.setAppTheme(it) }
                    )

                    Text(
                        text = "Reader Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    GlassSegmentedControl(
                        options = listOf(
                            ReaderThemeOption.LIGHT to "Light",
                            ReaderThemeOption.SEPIA to "Sepia",
                            ReaderThemeOption.DARK to "Dark"
                        ),
                        selected = settings.readerTheme,
                        accentColor = PrismViolet,
                        onSelect = { viewModel.setReaderTheme(it) }
                    )

                    Text(
                        text = "Text Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    GlassSegmentedControl(
                        options = listOf(
                            TextSizeOption.SMALL to "Small",
                            TextSizeOption.MEDIUM to "Medium",
                            TextSizeOption.LARGE to "Large"
                        ),
                        selected = settings.textSize,
                        accentColor = CrystalTeal,
                        onSelect = { viewModel.setTextSize(it) }
                    )

                    Text(
                        text = "Page Layout",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    GlassSegmentedControl(
                        options = listOf(
                            PageLayoutOption.SINGLE_PAGE to "Single Page",
                            PageLayoutOption.CONTINUOUS to "Continuous",
                            PageLayoutOption.TWO_PAGE to "Two Page"
                        ),
                        selected = settings.pageLayout,
                        accentColor = IridescentPink,
                        onSelect = { viewModel.setPageLayout(it) }
                    )
                }
            }
        }

        // 2. READER OPTIONS
        item {
            SettingsSectionHeader("READER OPTIONS", PrismPurple)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = PrismPurple
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsSwitchRow(
                        title = "Auto Rotate",
                        subtitle = "Adapt page view when rotating your phone",
                        checked = settings.autoRotate,
                        onCheckedChange = { viewModel.setAutoRotate(it) }
                    )
                    SettingsSwitchRow(
                        title = "Keep Screen Awake",
                        subtitle = "Prevent screen dimming while reading a PDF",
                        checked = settings.keepScreenAwake,
                        onCheckedChange = { viewModel.setKeepScreenAwake(it) }
                    )
                    SettingsSwitchRow(
                        title = "Remember Reading Position",
                        subtitle = "Automatically resume from your last read page",
                        checked = settings.rememberReadingPosition,
                        onCheckedChange = { viewModel.setRememberReadingPosition(it) }
                    )
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Subtle tactile response on glass interactions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.setHapticFeedback(it) }
                    )
                }
            }
        }

        // 3. STORAGE
        item {
            SettingsSectionHeader("STORAGE", CrystalTeal)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = CrystalTeal
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingsInfoRow(
                        icon = Icons.Filled.Folder,
                        title = "Default Download Location",
                        subtitle = settings.defaultDownloadLocation,
                        accent = CrystalTeal,
                        onClick = {
                            val next = if (settings.defaultDownloadLocation.contains("Documents")) {
                                "Downloads/Paperflow"
                            } else {
                                "Documents/Paperflow"
                            }
                            viewModel.setDefaultDownloadLocation(next)
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Storage,
                        title = "Library Storage",
                        subtitle = "${documents.size} documents • ${GlassPaperViewModel.formatFileSize(totalLibraryBytes)} used locally",
                        accent = EmeraldGreen,
                        onClick = {
                            infoDialogContent = "Library Storage" to "Your Paperflow library currently tracks ${documents.size} PDF documents (${GlassPaperViewModel.formatFileSize(totalLibraryBytes)}) in local storage."
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.History,
                        title = "Clear Recent History",
                        subtitle = "Reset recent file timestamps without deleting PDFs",
                        accent = SolarAmber,
                        onClick = { showClearHistoryDialog = true }
                    )
                }
            }
        }

        // 4. PRIVACY
        item {
            SettingsSectionHeader("PRIVACY", EmeraldGreen)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = EmeraldGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% Local Processing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Your documents stay on your device. No unnecessary cloud upload or external tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5. ABOUT
        item {
            SettingsSectionHeader("ABOUT", SolarAmber)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = SolarAmber
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SettingsInfoRow(
                        icon = Icons.Filled.Info,
                        title = "App Version",
                        subtitle = "Paperflow v2.6.0 (2026 Liquid Glass Edition)",
                        accent = ElectricBlue,
                        onClick = {
                            infoDialogContent = "About Paperflow" to "Paperflow — PDF Reader, Notes & Document Tools v2.6.0.\nDesigned with an original VisionOS-inspired Liquid Glass aesthetic for distraction-free reading, annotation, and local PDF management."
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Info,
                        title = "About Paperflow",
                        subtitle = "PDF Reader • Notes • Documents • Tools",
                        accent = IridescentPink,
                        onClick = {
                            infoDialogContent = "About Paperflow" to "Paperflow combines a high-resolution native PDF reader, persistent page annotations, rich study notes, and offline document utilities inside a translucent Liquid Glass UI."
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "Zero telemetry • Strictly offline-first architecture",
                        accent = CrystalTeal,
                        onClick = {
                            infoDialogContent = "Privacy Policy" to "Your documents stay on your device.\n\nPaperflow processes all PDF rendering, annotations, notes, and document utilities strictly on your Android device. No files or reading habits are ever uploaded to external servers."
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Code,
                        title = "Paperflow GitHub Repository",
                        subtitle = "penpoyem-create/Paperflow-pdf-reader-Notes • Browse source tree, commits & README",
                        accent = LiquidCyan,
                        onClick = {
                            viewModel.openGitHubRepository()
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.Description,
                        title = "Open Source Licenses",
                        subtitle = "Android Jetpack Compose, Room, DataStore & PdfRenderer",
                        accent = PrismViolet,
                        onClick = {
                            infoDialogContent = "Open Source Licenses" to "Built with Android Jetpack Compose, Material 3, Room Persistence Library, Jetpack DataStore, Coil, and Android Native PdfRenderer under the Apache 2.0 License."
                        }
                    )
                    SettingsInfoRow(
                        icon = Icons.Filled.BugReport,
                        title = "Report a Problem",
                        subtitle = "Diagnostic log & feedback guide",
                        accent = IridescentPink,
                        onClick = {
                            infoDialogContent = "Report a Problem" to "If a PDF fails to render, verify that the document is not encrypted with an owner password and that the file header is intact."
                        }
                    )
                }
            }
        }

        // 6. DANGER ZONE
        item {
            SettingsSectionHeader("DANGER ZONE", LiquidMagenta)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = LiquidMagenta
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearAllDataDialog = true }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LiquidMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                tint = LiquidMagenta,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Clear App Data",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LiquidMagenta
                            )
                            Text(
                                text = "Remove all imported document references, annotations, bookmarks, and notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiquidMagenta
                    )
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Recent History?") },
            text = { Text("This will reset recent reading timestamps. Your PDF documents and notes will remain safe in your Library.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearRecentHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearAllDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDataDialog = false },
            title = { Text("Clear App Data?") },
            text = { Text("This will permanently clear all library references, bookmarks, annotations, and study notes from Paperflow.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidMagenta),
                    onClick = {
                        viewModel.clearAllAppData()
                        showClearAllDataDialog = false
                    }
                ) {
                    Text("Clear App Data", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    infoDialogContent?.let { (dialogTitle, dialogBody) ->
        AlertDialog(
            onDismissRequest = { infoDialogContent = null },
            title = { Text(dialogTitle) },
            text = { Text(dialogBody) },
            confirmButton = {
                Button(onClick = { infoDialogContent = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun GalacticCodexPhotoThemeCard(
    selectedTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit
) {
    val glass = LocalGlassColors.current
    val isGalacticSelected = selectedTheme == AppThemeOption.GALACTIC_CODEX

    LiquidGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_photo_theme_section"),
        cornerRadius = 28.dp,
        tintColor = WarmGold
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Change Theme Section",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Select the Galactic Scholar cosmic photo theme or classic Liquid Glass editions",
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = "Theme Gallery",
                    tint = WarmGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Interactive Photo Theme Showcase Card — "Galactic Scholar • Cosmic Codex"
            val previewShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(previewShape)
                    .border(
                        width = if (isGalacticSelected) 2.2.dp else 1.3.dp,
                        brush = Brush.linearGradient(
                            colors = if (isGalacticSelected) {
                                listOf(WarmGold, Color.White, LiquidCyan, WarmGold)
                            } else {
                                listOf(Color.White.copy(alpha = 0.65f), WarmGold.copy(alpha = 0.45f))
                            }
                        ),
                        shape = previewShape
                    )
                    .testTag("settings_galactic_codex_theme_card")
                    .clickable { onSelectTheme(AppThemeOption.GALACTIC_CODEX) }
            ) {
                // Cosmic Nebula & Swirling Gold-Teal Liquid Glass Photo Art
                Image(
                    painter = painterResource(id = R.drawable.img_hero_galactic_codex_1791427754468),
                    contentDescription = "Galactic Scholar Photo Theme Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                )

                // Dark Cosmic Scrim for crisp legibility
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF050811).copy(alpha = 0.68f),
                                    Color(0xFF0B1522).copy(alpha = 0.56f),
                                    Color(0xFF1A1208).copy(alpha = 0.82f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mini Preview of the Galactic Scholar Header ("Galaxy Explorer" P orb + "Journey Awaits, Galactic Scholar")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                                    .border(
                                        width = 1.2.dp,
                                        brush = Brush.linearGradient(listOf(Color.White, LiquidCyan, PrismViolet)),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(Color.White, LiquidCyan, PrismViolet)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Journey Awaits, Galactic Scholar",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = "Read • Organize • Learn",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Diamond,
                                        contentDescription = null,
                                        tint = LiquidCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = WarmGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (isGalacticSelected) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(WarmGold)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Active Theme",
                                    tint = Color(0xFF1E1306),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF1E1306)
                                )
                            }
                        }
                    }

                    // Mini "Discover Your Codex" Glass Preview Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0E3A47).copy(alpha = 0.65f),
                                        Color(0xFF451A03).copy(alpha = 0.60f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(WarmGold.copy(alpha = 0.85f), LiquidCyan.copy(alpha = 0.75f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GALACTIC CODEX PHOTO THEME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.1.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                ),
                                color = WarmGold
                            )
                            Text(
                                text = "Discover Your Codex • Starfield & Golden Glass Bar",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isGalacticSelected) {
                                        Color.White.copy(alpha = 0.22f)
                                    } else {
                                        WarmGold
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isGalacticSelected) "Applied" else "Apply Theme",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isGalacticSelected) Color.White else Color(0xFF1E1306)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = color
        )
    }
}

@Composable
private fun <T> GlassSegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    accentColor: Color,
    onSelect: (T) -> Unit
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (glass.isDark) 0.08f else 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) accentColor else Color.Transparent
                    )
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else glass.textPrimary
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricBlue
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accent.copy(alpha = 0.2f))
                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (glass.isDark) Color.White else accent,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = glass.textSecondary
        )
    }
}
