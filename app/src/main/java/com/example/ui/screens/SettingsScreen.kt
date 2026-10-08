package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.R
import com.example.auth.AuthSessionState
import com.example.data.AiButtonSizeOption
import com.example.data.AppSettings
import com.example.data.AppThemeOption
import com.example.data.PageLayoutOption
import com.example.data.PdfDocumentEntity
import com.example.data.ReaderThemeOption
import com.example.data.TextSizeOption
import com.example.ui.GlassPaperViewModel
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.components.PaperflowAiPrismLogo
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.NothingBrightRed
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.NothingDotMatrixFamily
import com.example.ui.theme.NothingGlyphWhite
import com.example.ui.theme.NothingObsidianBlack
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SpaceMonoFamily
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

        // 1. PHOTO THEMES & VISUAL EDITIONS + THEME PAGE BUTTON
        item {
            SettingsSectionHeader("THEME & APP STYLE", NothingCrimsonRed)
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openThemePage() }
                    .testTag("settings_open_theme_page_card"),
                cornerRadius = 26.dp,
                tintColor = NothingCrimsonRed
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141518))
                            .border(1.5.dp, NothingCrimsonRed.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = "Theme",
                            tint = NothingCrimsonRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${settings.appStyle.label} • ${settings.themeMode.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = NothingCrimsonRed
                        )
                        Text(
                            text = "Switch between Nothing UI, Apple UI, Journey Awaits, Desert Clay & Enchanted Forest",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                    Button(
                        onClick = { viewModel.openThemePage() },
                        colors = ButtonDefaults.buttonColors(containerColor = NothingCrimsonRed),
                        modifier = Modifier.testTag("settings_theme_button")
                    ) {
                        Text("Theme", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1.5 FLOATING AI ASSISTANT CONTROLS — 3D Liquid Glass Orb Card (Photo 2 Design)
        item {
            PrismaticFloatingAiAssistantCard(
                settings = settings,
                onToggleEnabled = { viewModel.setAiAssistantEnabled(it) },
                onToggleHideWhileReading = { viewModel.setAiHideWhileReadingPdf(it) },
                onSelectSize = { viewModel.setAiButtonSize(it) },
                onChangeOpacity = { viewModel.setAiButtonOpacity(it) },
                onOpenAiChat = { viewModel.openAiChatPanel() },
                onResetPosition = { viewModel.resetAiButtonPosition() },
                onClearHistory = { viewModel.clearAiChatConversation() }
            )
        }

        // 2. APPEARANCE — Prismatic Liquid Glass Card (Photo 2 Design)
        item {
            PrismaticAppearanceGlassCard(
                settings = settings,
                onSelectReaderTheme = { viewModel.setReaderTheme(it) },
                onSelectTextSize = { viewModel.setTextSize(it) },
                onSelectPageLayout = { viewModel.setPageLayout(it) }
            )
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
private fun PrismaticFloatingAiAssistantCard(
    settings: AppSettings,
    onToggleEnabled: (Boolean) -> Unit,
    onToggleHideWhileReading: (Boolean) -> Unit,
    onSelectSize: (AiButtonSizeOption) -> Unit,
    onChangeOpacity: (Float) -> Unit,
    onOpenAiChat: () -> Unit,
    onResetPosition: () -> Unit,
    onClearHistory: () -> Unit
) {
    val cardShape = RoundedCornerShape(34.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = cardShape,
                ambientColor = ElectricBlue.copy(alpha = 0.40f),
                spotColor = PrismViolet.copy(alpha = 0.40f)
            )
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A2234).copy(alpha = 0.94f),
                        Color(0xFF141B29).copy(alpha = 0.95f),
                        Color(0xFF182030).copy(alpha = 0.95f),
                        Color(0xFF111724).copy(alpha = 0.96f)
                    )
                )
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                val cornerPx = 34.dp.toPx()

                // Ambient glow behind top-left 3D liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.35f),
                            Color(0xFF38BDF8).copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.24f, h * 0.14f),
                        radius = w * 0.44f
                    )
                )

                // Ambient glow behind bottom-right 3D liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF818CF8).copy(alpha = 0.32f),
                            Color(0xFF38BDF8).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.86f, h * 0.92f),
                        radius = w * 0.36f
                    )
                )

                // Top specular glass highlight rim
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.22f
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // Inner double-refraction chromatic glass rim
                val inset = 2.5.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFF60A5FA).copy(alpha = 0.70f),
                            Color(0xFFA78BFA).copy(alpha = 0.65f),
                            Color(0xFFF472B6).copy(alpha = 0.55f),
                            Color(0xFF38BDF8).copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.88f)
                        )
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(w - inset * 2, h - inset * 2),
                    cornerRadius = CornerRadius(cornerPx - inset, cornerPx - inset),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            .border(
                width = 2.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFF93C5FD).copy(alpha = 0.85f),
                        Color(0xFFC4B5FD).copy(alpha = 0.80f),
                        Color(0xFF38BDF8).copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.92f)
                    )
                ),
                shape = cardShape
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
            .testTag("settings_ai_assistant_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Hero Header: Large 3D Liquid-Wave Crystal Sphere + Bold Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidWaveCrystalSphereOrb(
                    size = 112.dp,
                    modifier = Modifier.clickable { onOpenAiChat() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Paperflow\nDraggable AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            lineHeight = 30.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Summarize PDFs, explain pages &\ngenerate study notes",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFD1D9E6)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "anywhere in the app",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.16f)
                    )
                }
            }

            // 2. Section Label: F L O A T I N G   A I   A S S I S T A N T
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFC084FC))) {
                        append("FLOATING ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF38BDF8))) {
                        append("AI ASSISTANT")
                    }
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    letterSpacing = 2.0.sp
                )
            )

            // 3. Switch 1: Enable Floating AI Button (with 3D glass sphere thumb)
            LiquidGlassOrbSwitchRow(
                title = "Enable Floating AI Button",
                subtitle = "Show the draggable liquid-glass AI orb above screens",
                checked = settings.aiAssistantEnabled,
                onCheckedChange = onToggleEnabled
            )

            // 4. Switch 2: Hide Button While Reading PDFs
            LiquidGlassOrbSwitchRow(
                title = "Hide Button While Reading PDFs",
                subtitle = "Automatically hide the floating AI orb inside the PDF Reader",
                checked = settings.aiHideWhileReadingPdf,
                onCheckedChange = onToggleHideWhileReading
            )

            // 5. Floating Button Size + 3-Pill Capsule Track (Compact | Standard | Large)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Floating Button Size",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.5.sp
                    ),
                    color = Color(0xFFE2E8F0)
                )

                LiquidGlassSizeCapsuleSelector(
                    options = listOf(
                        AiButtonSizeOption.COMPACT to "Compact",
                        AiButtonSizeOption.MEDIUM to "Standard",
                        AiButtonSizeOption.LARGE to "Large"
                    ),
                    selected = settings.aiButtonSize,
                    onSelect = onSelectSize
                )
            }

            // 6. Floating Button Opacity + Glowing Pill Badge + 3D Glass Orb Slider
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Floating Button Opacity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.5.sp
                        ),
                        color = Color(0xFFE2E8F0)
                    )

                    // Frosted Blue Pill Badge showing e.g. "98%"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A5F).copy(alpha = 0.85f),
                                        Color(0xFF0F2442).copy(alpha = 0.90f)
                                    )
                                )
                            )
                            .border(
                                width = 1.1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF7DD3FC).copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0.65f)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(settings.aiButtonOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                LiquidGlassOrbOpacitySlider(
                    value = settings.aiButtonOpacity,
                    onValueChange = onChangeOpacity,
                    valueRange = 0.35f..1.0f,
                    modifier = Modifier.testTag("settings_ai_opacity_slider")
                )
            }

            // 7. Action Buttons Row: [ ✨ Open AI Chat ]  [ Reset Position ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Glowing Azure-Blue Pill Button with Sparkle Icon
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color(0xFF0284C7),
                            spotColor = Color(0xFF38BDF8)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0EA5E9),
                                    Color(0xFF0284C7),
                                    Color(0xFF0369A1)
                                )
                            )
                        )
                        .border(
                            width = 1.4.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.92f),
                                    Color(0xFF7DD3FC).copy(alpha = 0.75f)
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(onClick = onOpenAiChat)
                        .testTag("settings_open_ai_chat_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFBAE6FD),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open AI Chat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Right: Frosted Translucent Glass Pill Button "Reset Position"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    Color(0xFF334155).copy(alpha = 0.42f)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.25f)
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(onClick = onResetPosition)
                        .testTag("settings_reset_ai_pos_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset Position",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.5.sp
                        ),
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            // 8. Bottom Row: Clear AI Chat History + Secondary 3D Liquid-Wave Crystal Orb on the Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onClearHistory)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "Clear AI Chat History",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Delete all saved Paperflow AI conversation messages",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp
                        ),
                        color = Color(0xFFCBD5E1)
                    )
                }

                LiquidWaveCrystalSphereOrb(
                    size = 64.dp,
                    modifier = Modifier.clickable(onClick = onClearHistory)
                )
            }
        }
    }
}

/**
 * Custom 3D Liquid-Wave Crystal Sphere matching Photo (2):
 * Renders a glossy glass orb containing an internal cobalt/violet liquid wave,
 * floating micro-bubbles, internal caustics, and a 4-point star lens flare on top-right.
 */
@Composable
private fun LiquidWaveCrystalSphereOrb(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.45f

        // 1. Outer ambient halo glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF818CF8).copy(alpha = 0.45f),
                    Color(0xFF38BDF8).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.52f
            ),
            radius = w * 0.52f,
            center = center
        )

        // 2. Base crystal glass sphere body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3B4C7A),
                    Color(0xFF1E2648),
                    Color(0xFF141833),
                    Color(0xFF2E3B6E)
                ),
                center = Offset(w * 0.38f, h * 0.32f),
                radius = radius * 1.25f
            ),
            radius = radius,
            center = center
        )

        // 3. Internal swirling cobalt & electric-violet liquid wave inside the lower hemisphere
        val wavePath = Path().apply {
            moveTo(center.x - radius * 0.96f, center.y - radius * 0.04f)
            cubicTo(
                center.x - radius * 0.38f,
                center.y - radius * 0.34f,
                center.x + radius * 0.25f,
                center.y + radius * 0.36f,
                center.x + radius * 0.96f,
                center.y - radius * 0.06f
            )
            lineTo(center.x + radius * 0.85f, center.y + radius * 0.65f)
            cubicTo(
                center.x + radius * 0.35f,
                center.y + radius * 0.98f,
                center.x - radius * 0.35f,
                center.y + radius * 0.98f,
                center.x - radius * 0.85f,
                center.y + radius * 0.65f
            )
            close()
        }
        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF6366F1).copy(alpha = 0.88f),
                    Color(0xFF312E81).copy(alpha = 0.95f),
                    Color(0xFF4F46E5).copy(alpha = 0.90f),
                    Color(0xFF93C5FD).copy(alpha = 0.65f)
                ),
                startY = center.y - radius * 0.3f,
                endY = center.y + radius
            )
        )

        // 4. Liquid wave crest highlight ribbon
        val crestPath = Path().apply {
            moveTo(center.x - radius * 0.92f, center.y - radius * 0.02f)
            cubicTo(
                center.x - radius * 0.36f,
                center.y - radius * 0.32f,
                center.x + radius * 0.25f,
                center.y + radius * 0.35f,
                center.x + radius * 0.92f,
                center.y - radius * 0.05f
            )
        }
        drawPath(
            path = crestPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    Color(0xFFA5B4FC),
                    Color(0xFF60A5FA),
                    Color.White.copy(alpha = 0.80f)
                )
            ),
            style = Stroke(width = (w * 0.024f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )

        // 5. Internal water droplets / micro-bubbles in upper hemisphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.9f), Color(0xFF60A5FA).copy(alpha = 0.35f)),
                center = Offset(center.x + radius * 0.34f, center.y - radius * 0.36f),
                radius = radius * 0.13f
            ),
            radius = radius * 0.11f,
            center = Offset(center.x + radius * 0.36f, center.y - radius * 0.34f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = radius * 0.045f,
            center = Offset(center.x + radius * 0.14f, center.y - radius * 0.50f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.50f),
            radius = radius * 0.035f,
            center = Offset(center.x - radius * 0.22f, center.y - radius * 0.38f)
        )

        // 6. Top-left curved specular reflection arc
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.88f),
                    Color.White.copy(alpha = 0.10f)
                )
            ),
            startAngle = 195f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.84f, center.y - radius * 0.84f),
            size = Size(radius * 1.68f, radius * 1.68f),
            style = Stroke(width = (w * 0.028f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )

        // 7. Outer crystal sphere rim
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.92f),
                    Color(0xFF93C5FD).copy(alpha = 0.75f),
                    Color(0xFFC4B5FD).copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.95f),
                    Color(0xFF60A5FA).copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.92f)
                ),
                center = center
            ),
            radius = radius,
            center = center,
            style = Stroke(width = (w * 0.025f).coerceAtLeast(2f))
        )

        // 8. 4-Point Star Lens Flare on Top-Right Rim (matching Photo 2)
        val flareCenter = Offset(center.x + radius * 0.64f, center.y - radius * 0.64f)
        val flareRadius = radius * 0.34f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFBAE6FD).copy(alpha = 0.65f),
                    Color.Transparent
                ),
                center = flareCenter,
                radius = flareRadius
            ),
            radius = flareRadius,
            center = flareCenter
        )
        drawLine(
            color = Color.White,
            start = Offset(flareCenter.x - flareRadius * 1.1f, flareCenter.y),
            end = Offset(flareCenter.x + flareRadius * 1.1f, flareCenter.y),
            strokeWidth = (w * 0.016f).coerceAtLeast(1.5f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(flareCenter.x, flareCenter.y - flareRadius * 1.1f),
            end = Offset(flareCenter.x, flareCenter.y + flareRadius * 1.1f),
            strokeWidth = (w * 0.016f).coerceAtLeast(1.5f),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun LiquidGlassOrbSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                ),
                color = Color(0xFFCBD5E1)
            )
        }

        // Custom 3D Glass Orb Toggle Switch matching Photo (2)
        val thumbOffset by animateDpAsState(
            targetValue = if (checked) 28.dp else 2.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "glass_switch_thumb"
        )

        Box(
            modifier = Modifier
                .width(62.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = if (checked) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0284C7),
                                Color(0xFF38BDF8)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF334155).copy(alpha = 0.65f),
                                Color(0xFF1E293B).copy(alpha = 0.75f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (checked) {
                            listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color(0xFF38BDF8).copy(alpha = 0.65f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            // 3D Glass Sphere Thumb
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(30.dp)
                    .then(
                        if (checked) {
                            Modifier.shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                ambientColor = Color.White,
                                spotColor = Color(0xFF38BDF8)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(CircleShape)
                    .background(
                        brush = if (checked) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFBAE6FD),
                                    Color(0xFF3B82F6),
                                    Color(0xFF1D4ED8)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF94A3B8),
                                    Color(0xFF64748B)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (checked) 1.5.dp else 1.dp,
                        color = if (checked) Color.White else Color.White.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun LiquidGlassSizeCapsuleSelector(
    options: List<Pair<AiButtonSizeOption, String>>,
    selected: AiButtonSizeOption,
    onSelect: (AiButtonSizeOption) -> Unit
) {
    val outerShape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.55f),
                        Color(0xFF1E293B).copy(alpha = 0.45f)
                    )
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color(0xFF93C5FD).copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.55f)
                    )
                ),
                shape = outerShape
            )
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            val pillShape = RoundedCornerShape(50)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 12.dp,
                                shape = pillShape,
                                ambientColor = Color(0xFF38BDF8),
                                spotColor = Color(0xFF60A5FA)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(pillShape)
                    .background(
                        brush = if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                    Color(0xFF2563EB).copy(alpha = 0.68f),
                                    Color(0xFF38BDF8).copy(alpha = 0.55f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.14f),
                                    Color.White.copy(alpha = 0.06f)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.6.dp else 1.dp,
                        brush = if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFF7DD3FC),
                                    Color.White
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            )
                        },
                        shape = pillShape
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassOrbOpacitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val thumbSize = 38.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val newFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    val newFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newFraction * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0F172A).copy(alpha = 0.70f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            // Glowing Neon-Blue Active Track Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.06f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1D4ED8),
                                Color(0xFF0284C7),
                                Color(0xFF00E5FF)
                            )
                        )
                    )
            )
        }

        // 3D Glossy Blue Crystal Sphere Thumb
        val maxThumbOffsetPx = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
        val currentThumbOffsetPx = (maxThumbOffsetPx * fraction).roundToInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(currentThumbOffsetPx, 0) }
                .size(thumbSize)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF38BDF8),
                    spotColor = Color.White
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.width / 2f
                val c = Offset(r, r)

                // Sphere base gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF1D4ED8),
                            Color(0xFF0F172A),
                            Color(0xFF60A5FA)
                        ),
                        center = Offset(r * 0.7f, r * 0.65f),
                        radius = r * 1.3f
                    ),
                    radius = r * 0.92f,
                    center = c
                )

                // Top specular arc reflection
                drawArc(
                    color = Color.White.copy(alpha = 0.90f),
                    startAngle = 200f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(r * 0.22f, r * 0.22f),
                    size = Size(r * 1.56f, r * 1.56f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Outer crystal rim
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF7DD3FC),
                            Color.White.copy(alpha = 0.85f)
                        )
                    ),
                    radius = r * 0.92f,
                    center = c,
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun PrismaticAppearanceGlassCard(
    settings: AppSettings,
    onSelectReaderTheme: (ReaderThemeOption) -> Unit,
    onSelectTextSize: (TextSizeOption) -> Unit,
    onSelectPageLayout: (PageLayoutOption) -> Unit
) {
    val cardShape = RoundedCornerShape(30.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = cardShape,
                ambientColor = LiquidCyan.copy(alpha = 0.35f),
                spotColor = IridescentPink.copy(alpha = 0.35f)
            )
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6B747E).copy(alpha = 0.68f),
                        Color(0xFF4F5962).copy(alpha = 0.72f),
                        Color(0xFF3E4A4B).copy(alpha = 0.76f),
                        Color(0xFF565E68).copy(alpha = 0.70f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 900f)
                )
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                val cornerPx = 30.dp.toPx()

                // Warm sunlight & prismatic refraction glow washes inside the frosted glass slab
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD89B).copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.26f, h * 0.42f),
                        radius = w * 0.48f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.22f),
                            PrismViolet.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.12f, h * 0.18f),
                        radius = w * 0.45f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IridescentPink.copy(alpha = 0.18f),
                            LiquidCyan.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f, h * 0.85f),
                        radius = w * 0.46f
                    )
                )

                // Top specular glass highlight rim
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.35f
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // Inner double-refraction chromatic border
                val inset = 2.5.dp.toPx()
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.90f),
                            IridescentPink.copy(alpha = 0.78f),
                            SolarAmber.copy(alpha = 0.65f),
                            LiquidCyan.copy(alpha = 0.88f),
                            PrismViolet.copy(alpha = 0.80f),
                            Color.White.copy(alpha = 0.92f),
                            LiquidCyan.copy(alpha = 0.85f)
                        )
                    ),
                    topLeft = Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(w - inset * 2, h - inset * 2),
                    cornerRadius = CornerRadius(cornerPx - inset, cornerPx - inset),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
            .border(
                width = 2.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        LiquidCyan.copy(alpha = 0.88f),
                        IridescentPink.copy(alpha = 0.82f),
                        Color(0xFFFFE082).copy(alpha = 0.78f),
                        LiquidCyan.copy(alpha = 0.90f),
                        Color.White.copy(alpha = 0.95f)
                    )
                ),
                shape = cardShape
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
            .testTag("settings_appearance_prismatic_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Subtle italic serif "A P P E A R A N C E" header inside the top-left of the glass slab
            Text(
                text = "APPEARANCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 3.2.sp
                ),
                color = Color.White.copy(alpha = 0.72f)
            )

            // Row 1: Reader Theme  |  [ Light | Sepia | Dark ] (Glowing Violet Pill)
            PrismaticAppearanceRow(
                label = "Reader Theme",
                options = listOf(
                    ReaderThemeOption.LIGHT to "Light",
                    ReaderThemeOption.SEPIA to "Sepia",
                    ReaderThemeOption.DARK to "Dark"
                ),
                selected = settings.readerTheme,
                pillStartColor = Color(0xFFA78BFA),
                pillEndColor = Color(0xFF7C3AED),
                pillGlowColor = Color(0xFFC4B5FD),
                onSelect = onSelectReaderTheme
            )

            // Row 2: Text Size  |  [ Small | Medium | Large ] (Glowing Cyan-Teal Pill)
            PrismaticAppearanceRow(
                label = "Text Size",
                options = listOf(
                    TextSizeOption.SMALL to "Small",
                    TextSizeOption.MEDIUM to "Medium",
                    TextSizeOption.LARGE to "Large"
                ),
                selected = settings.textSize,
                pillStartColor = Color(0xFF00E5CC),
                pillEndColor = Color(0xFF00B4A6),
                pillGlowColor = Color(0xFF64FFDA),
                onSelect = onSelectTextSize
            )

            // Row 3: Page Layout  |  [ Single Page | Continuous | Two Page ] (Glowing Rose-Pink Pill)
            PrismaticAppearanceRow(
                label = "Page Layout",
                options = listOf(
                    PageLayoutOption.SINGLE_PAGE to "Single Page",
                    PageLayoutOption.CONTINUOUS to "Continuous",
                    PageLayoutOption.TWO_PAGE to "Two Page"
                ),
                selected = settings.pageLayout,
                pillStartColor = Color(0xFFF472B6),
                pillEndColor = Color(0xFFDB2777),
                pillGlowColor = Color(0xFFF9A8D4),
                onSelect = onSelectPageLayout
            )
        }
    }
}

@Composable
private fun <T> PrismaticAppearanceRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    pillStartColor: Color,
    pillEndColor: Color,
    pillGlowColor: Color,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.5.sp,
                letterSpacing = 0.4.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 10.dp)
        )

        PrismaticCapsuleSegmentedControl(
            options = options,
            selected = selected,
            pillStartColor = pillStartColor,
            pillEndColor = pillEndColor,
            pillGlowColor = pillGlowColor,
            onSelect = onSelect,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun <T> PrismaticCapsuleSegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    pillStartColor: Color,
    pillEndColor: Color,
    pillGlowColor: Color,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(outerShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF1E293B).copy(alpha = 0.24f)
                    )
                )
            )
            .border(
                width = 1.1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.62f),
                        Color.White.copy(alpha = 0.28f)
                    )
                ),
                shape = outerShape
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            val pillShape = RoundedCornerShape(50)

            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 12.dp,
                                shape = pillShape,
                                ambientColor = pillGlowColor,
                                spotColor = pillStartColor
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(pillShape)
                    .background(
                        brush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    pillStartColor,
                                    pillEndColor
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.3.dp else 0.8.dp,
                        brush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    pillGlowColor.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        },
                        shape = pillShape
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 13.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        letterSpacing = 0.4.sp
                    ),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.82f),
                    maxLines = 1
                )
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
