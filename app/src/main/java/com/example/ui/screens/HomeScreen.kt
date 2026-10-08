package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.AuthSessionState
import com.example.data.PdfDocumentEntity
import com.example.data.StreakData
import com.example.ui.GlassPaperViewModel
import com.example.ui.LibraryCategory
import com.example.ui.components.GlassCircularIconButton
import com.example.ui.components.LiquidGlassDropdownMenu
import com.example.ui.components.LiquidGlassDropdownMenuItem
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.DesertClayCardElevated
import com.example.ui.theme.DesertClayCardSurface
import com.example.ui.theme.DesertClayCocoaBrown
import com.example.ui.theme.DesertClayMutedTaupe
import com.example.ui.theme.DesertClayPeachTerracotta
import com.example.ui.theme.DesertClaySoftApricot
import com.example.ui.theme.DesertClayWarmAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestDeepCanopy
import com.example.ui.theme.ForestFernGreen
import com.example.ui.theme.ForestParchmentCream
import com.example.ui.theme.ForestSageGreen
import com.example.ui.theme.ForestSunlightGold
import com.example.ui.theme.ForestTerracottaClay
import com.example.ui.theme.ForestWarmSand
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.MerriweatherSerifFamily
import com.example.ui.theme.NothingBrightRed
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.NothingDotMatrixFamily
import com.example.ui.theme.NothingGlyphWhite
import com.example.ui.theme.NothingObsidianBlack
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarmGold

@Composable
fun HomeScreen(
    viewModel: GlassPaperViewModel,
    documents: List<PdfDocumentEntity>,
    streakData: StreakData,
    authSession: AuthSessionState = AuthSessionState(),
    onOpenSelectPdf: () -> Unit,
    onOpenPdf: (PdfDocumentEntity) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenStreakDetails: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenGitHubRepo: () -> Unit = {},
    onReplayFlashIntro: () -> Unit = {},
    onRefreshPage: () -> Unit = {},
    onNavigateToLibrary: (LibraryCategory) -> Unit
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current
    var showTopMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 118.dp // Space for floating glass capsule nav
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top Greeting Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Iridescent Glass Orb Avatar Button (with "Galaxy Explorer" outer ring in Galactic Codex theme)
                    Box(
                        modifier = Modifier
                            .size(if (glass.isGalacticCodex) 56.dp else 46.dp)
                            .testTag("home_avatar_auth_button")
                            .clickable { onOpenAuth() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (glass.isGalacticCodex) {
                            // Outer Dark Frosted "Galaxy Explorer" Orbital Ring
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(14.dp, CircleShape, ambientColor = LiquidCyan, spotColor = PrismViolet)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF1E293B).copy(alpha = 0.85f),
                                                Color(0xFF090D18).copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.65f),
                                                LiquidCyan.copy(alpha = 0.45f),
                                                PrismViolet.copy(alpha = 0.45f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }

                        // Inner Glowing Emblem Orb with Paperflow Red/Black/White Icon or user initial
                        Box(
                            modifier = Modifier
                                .size(if (glass.isGalacticCodex) 40.dp else 46.dp)
                                .shadow(12.dp, CircleShape, ambientColor = Color(0xFFFF1A1A), spotColor = ElectricBlue)
                                .clip(CircleShape)
                                .background(Color(0xFF0A0A0A))
                                .border(
                                    width = 1.6.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.White, Color(0xFFFF1A1A), Color.White.copy(alpha = 0.75f))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayInitial = when {
                                authSession.isAuthenticated && authSession.userFullName.isNotBlank() ->
                                    authSession.userFullName.trim().take(1).uppercase()
                                else -> null
                            }
                            if (displayInitial != null) {
                                Text(
                                    text = displayInitial,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.paperflow_red_black_icon_1791443745243),
                                    contentDescription = "Paperflow Account & Sign In",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (glass.isNothingOs) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NothingCrimsonRed)
                                )
                                Text(
                                    text = "NOTHING OS // LIQUID GLASS 3.0",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.1.sp
                                    ),
                                    color = NothingCrimsonRed,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (authSession.isAuthenticated && authSession.userFullName.isNotBlank()) {
                                    "SYS.USER // ${authSession.userFullName.substringBefore(" ").uppercase()}"
                                } else {
                                    "PAPERFLOW // OS"
                                },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = NothingDotMatrixFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = 1.2.sp
                                ),
                                color = NothingGlyphWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "GLYPH MATRIX • 165HZ ULTRA-SMOOTH ENGINE",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = SpaceMonoFamily,
                                    fontSize = 10.5.sp
                                ),
                                color = glass.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (glass.isGalacticCodex) {
                            val librarianHandle = when {
                                authSession.isAuthenticated && authSession.userEmail.isNotBlank() ->
                                    authSession.userEmail.substringBefore("@")
                                authSession.isAuthenticated && authSession.userFullName.isNotBlank() ->
                                    authSession.userFullName.lowercase().replace(" ", "")
                                else -> "penpoyem"
                            }
                            Text(
                                text = "Journey Awaits,\nGalactic Scholar",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    lineHeight = 22.sp
                                ),
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Librarian $librarianHandle",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp
                                ),
                                color = Color(0xFFCBD5E1),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (glass.isDesertDuneClay) {
                            Text(
                                text = stringResource(R.string.greeting_morning),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp
                                ),
                                color = DesertClayMutedTaupe,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (authSession.isAuthenticated && authSession.userFullName.isNotBlank()) {
                                        "Welcome, ${authSession.userFullName.substringBefore(" ")}"
                                    } else {
                                        stringResource(R.string.greeting_welcome)
                                    },
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    ),
                                    color = DesertClayCocoaBrown,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = DesertClayPeachTerracotta,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = stringResource(R.string.subtitle_home),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = DesertClayMutedTaupe,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (glass.isEnchantedForestCodex) {
                            Text(
                                text = stringResource(R.string.greeting_morning),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp
                                ),
                                color = Color(0xFFD7E8D4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (authSession.isAuthenticated && authSession.userFullName.isNotBlank()) {
                                        "Welcome, ${authSession.userFullName.substringBefore(" ")}"
                                    } else {
                                        stringResource(R.string.greeting_welcome)
                                    },
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = MerriweatherSerifFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = ForestParchmentCream,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = ForestSunlightGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = stringResource(R.string.subtitle_home),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFFD7E8D4),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.greeting_morning),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.5.sp
                                ),
                                color = if (glass.isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E3A8A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (authSession.isAuthenticated && authSession.userFullName.isNotBlank()) {
                                        "Welcome, ${authSession.userFullName.substringBefore(" ")}"
                                    } else {
                                        stringResource(R.string.greeting_welcome)
                                    },
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = if (glass.isDark) Color.White else Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = SolarAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = stringResource(R.string.subtitle_home),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = glass.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Snapchat-style Top Bar Streak Button (🔥 + Count Pill)
                    SnapchatStyleStreakButton(
                        streakCount = streakData.currentStreak.coerceAtLeast(1),
                        onClick = onOpenStreakDetails,
                        modifier = Modifier.testTag("home_streak_badge")
                    )

                    GlassCircularIconButton(
                        icon = Icons.Filled.Search,
                        contentDescription = "Search documents",
                        onClick = { onNavigateToLibrary(LibraryCategory.ALL) },
                        size = 40.dp,
                        modifier = Modifier.testTag("home_search_button")
                    )
                    Box {
                        GlassCircularIconButton(
                            icon = Icons.Filled.MoreHoriz,
                            contentDescription = "More options",
                            onClick = { showTopMenu = true },
                            size = 40.dp,
                            modifier = Modifier.testTag("home_more_button")
                        )
                        LiquidGlassDropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            LiquidGlassDropdownMenuItem(
                                text = "Refresh Page",
                                onClick = {
                                    showTopMenu = false
                                    onRefreshPage()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "Import PDF",
                                onClick = {
                                    showTopMenu = false
                                    onOpenSelectPdf()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "Create Study Note",
                                onClick = {
                                    showTopMenu = false
                                    onOpenNotes()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "Saved Bookmarks",
                                onClick = {
                                    showTopMenu = false
                                    onOpenBookmarks()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "\uD83D\uDD25 Daily Reading Streak",
                                onClick = {
                                    showTopMenu = false
                                    onOpenStreakDetails()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = if (authSession.isAuthenticated) {
                                    "Account (${authSession.userEmail})"
                                } else {
                                    "Sign In / Create Account"
                                },
                                onClick = {
                                    showTopMenu = false
                                    onOpenAuth()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "GitHub Repository",
                                onClick = {
                                    showTopMenu = false
                                    onOpenGitHubRepo()
                                }
                            )
                            LiquidGlassDropdownMenuItem(
                                text = "Replay Flash Intro",
                                showDivider = false,
                                onClick = {
                                    showTopMenu = false
                                    onReplayFlashIntro()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Hero Card — "Open Your PDF" with animated light movement & 3D glass PDF badge
        item {
            HeroOpenPdfCard(onOpenSelectPdf = onOpenSelectPdf)
        }

        // 3. Quick Actions Section Header + 4 Colorful Glass Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.section_quick_actions),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = if (glass.isDark) Color.White else Color(0xFF0F172A)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToLibrary(LibraryCategory.ALL) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = glass.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Colorful Glass Cards (3-column Galactic Codex grid or 4-card horizontal row)
                if (glass.isGalacticCodex) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionGlassCard(
                            title = "View PDF",
                            subtitle = "Unlock the knowledge within.",
                            icon = Icons.Filled.Description,
                            primaryTint = LiquidCyan,
                            secondaryTint = CrystalTeal,
                            onClick = onOpenSelectPdf,
                            testTag = "quick_action_view_pdf",
                            modifier = Modifier.weight(1f),
                            isConstellationCard = false
                        )
                        QuickActionGlassCard(
                            title = "Add Notes",
                            subtitle = "Annotate and expand upon existing texts",
                            icon = Icons.Filled.Edit,
                            primaryTint = IridescentPink,
                            secondaryTint = PrismViolet,
                            onClick = onOpenNotes,
                            testTag = "quick_action_add_notes",
                            modifier = Modifier.weight(1f),
                            isConstellationCard = false
                        )
                        QuickActionGlassCard(
                            title = "Bookmarks",
                            subtitle = "Chronicle your path for easy recall",
                            icon = Icons.Filled.Star,
                            primaryTint = CrystalTeal,
                            secondaryTint = EmeraldGreen,
                            onClick = onOpenBookmarks,
                            testTag = "quick_action_bookmarks",
                            modifier = Modifier.weight(1f),
                            isConstellationCard = true
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_view_pdf),
                            subtitle = stringResource(R.string.action_view_pdf_sub),
                            icon = Icons.Filled.Description,
                            primaryTint = when {
                                glass.isDesertDuneClay -> DesertClayPeachTerracotta
                                glass.isEnchantedForestCodex -> ForestSageGreen
                                else -> ElectricBlue
                            },
                            secondaryTint = when {
                                glass.isDesertDuneClay -> DesertClaySoftApricot
                                glass.isEnchantedForestCodex -> ForestFernGreen
                                else -> LiquidCyan
                            },
                            onClick = onOpenSelectPdf,
                            testTag = "quick_action_view_pdf"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_add_notes),
                            subtitle = stringResource(R.string.action_add_notes_sub),
                            icon = Icons.Filled.Edit,
                            primaryTint = when {
                                glass.isDesertDuneClay -> DesertClayWarmAmber
                                glass.isEnchantedForestCodex -> ForestTerracottaClay
                                else -> IridescentPink
                            },
                            secondaryTint = when {
                                glass.isDesertDuneClay -> DesertClayPeachTerracotta
                                glass.isEnchantedForestCodex -> ForestSunlightGold
                                else -> PrismPurple
                            },
                            onClick = onOpenNotes,
                            testTag = "quick_action_add_notes"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_bookmarks),
                            subtitle = stringResource(R.string.action_bookmarks_sub),
                            icon = Icons.Filled.Bookmark,
                            primaryTint = when {
                                glass.isDesertDuneClay -> Color(0xFFD98A6C)
                                glass.isEnchantedForestCodex -> ForestSunlightGold
                                else -> CrystalTeal
                            },
                            secondaryTint = when {
                                glass.isDesertDuneClay -> DesertClaySoftApricot
                                glass.isEnchantedForestCodex -> ForestSageGreen
                                else -> EmeraldGreen
                            },
                            onClick = onOpenBookmarks,
                            testTag = "quick_action_bookmarks"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_recent),
                            subtitle = stringResource(R.string.action_recent_sub),
                            icon = Icons.Filled.History,
                            primaryTint = when {
                                glass.isDesertDuneClay -> DesertClayCocoaBrown
                                glass.isEnchantedForestCodex -> ForestFernGreen
                                else -> SolarAmber
                            },
                            secondaryTint = when {
                                glass.isDesertDuneClay -> DesertClayPeachTerracotta
                                glass.isEnchantedForestCodex -> ForestSageGreen
                                else -> IridescentPink
                            },
                            onClick = {
                                val mostRecent = documents.firstOrNull()
                                if (mostRecent != null) {
                                    onOpenPdf(mostRecent)
                                } else {
                                    onNavigateToLibrary(LibraryCategory.RECENT)
                                }
                            },
                            testTag = "quick_action_recent"
                        )
                    }
                }
            }
        }

        // 4. Recent Files Section inside a Large Translucent Glass Container
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.section_recent_files),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = if (glass.isDark) Color.White else Color(0xFF0F172A)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("home_view_all_recent")
                            .clickable { onNavigateToLibrary(LibraryCategory.RECENT) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_view_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = glass.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 30.dp,
                    tintColor = ElectricBlue,
                    shadowElevation = 14.dp
                ) {
                    if (documents.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PictureAsPdf,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.empty_recent_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.empty_library_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            documents.take(4).forEach { doc ->
                                RecentPdfGlassRow(
                                    doc = doc,
                                    onClick = { onOpenPdf(doc) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(doc) },
                                    onShare = { viewModel.shareDocument(context, doc) },
                                    onDuplicate = { viewModel.duplicateDocument(doc) },
                                    onDelete = { viewModel.deleteDocument(doc) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroOpenPdfCard(
    onOpenSelectPdf: () -> Unit
) {
    val glass = LocalGlassColors.current
    val lightSweep = 0.32f

    val heroShape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = heroShape,
                ambientColor = if (glass.isGalacticCodex) WarmGold.copy(alpha = 0.45f) else ElectricBlue.copy(alpha = 0.45f),
                spotColor = if (glass.isGalacticCodex) CrystalTeal.copy(alpha = 0.45f) else PrismPurple.copy(alpha = 0.45f)
            )
            .clip(heroShape)
            .clickable(onClick = onOpenSelectPdf)
            .testTag("hero_open_pdf_card")
    ) {
        // Generated Liquid Glass or Galactic Codex Hero Background Art
        Image(
            painter = painterResource(
                id = if (glass.isGalacticCodex) {
                    R.drawable.img_hero_galactic_codex_1791427754468
                } else {
                    R.drawable.img_hero_liquid_glass_1791391701805
                }
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(if (glass.isGalacticCodex) 0.92f else 0.88f)
        )

        // Rich Iridescent Translucent Glass Overlay with animated light movement
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = when {
                            glass.isDesertDuneClay -> listOf(
                                DesertClayCardElevated.copy(alpha = 0.94f),
                                DesertClaySoftApricot.copy(alpha = 0.88f),
                                DesertClayPeachTerracotta.copy(alpha = 0.72f),
                                Color(0xFFE7B696).copy(alpha = 0.90f)
                            )
                            glass.isEnchantedForestCodex -> listOf(
                                ForestDeepCanopy.copy(alpha = 0.82f),
                                Color(0xFF133A2A).copy(alpha = 0.76f),
                                ForestFernGreen.copy(alpha = 0.48f),
                                Color(0xFF283618).copy(alpha = 0.78f)
                            )
                            glass.isNothingOs -> listOf(
                                Color(0xFF0A0C10).copy(alpha = 0.80f),
                                Color(0xFF161922).copy(alpha = 0.72f),
                                NothingCrimsonRed.copy(alpha = 0.36f),
                                Color(0xFF0B0E14).copy(alpha = 0.84f)
                            )
                            glass.isGalacticCodex -> listOf(
                                Color(0xFF451A03).copy(alpha = 0.36f),
                                Color(0xFF0F292E).copy(alpha = 0.44f),
                                Color(0xFF064E3B).copy(alpha = 0.38f),
                                Color(0xFF78350F).copy(alpha = 0.34f)
                            )
                            else -> listOf(
                                Color(0xFF1E3A8A).copy(alpha = 0.42f),
                                Color(0xFF4F46E5).copy(alpha = 0.36f),
                                Color(0xFF9333EA).copy(alpha = 0.38f),
                                Color(0xFFEC4899).copy(alpha = 0.32f)
                            )
                        }
                    )
                )
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.32f), Color.Transparent),
                            center = Offset(size.width * lightSweep, size.height * 0.25f),
                            radius = size.width * 0.55f
                        )
                    )
                    if (glass.isNothingOs) {
                        val step = 16.dp.toPx()
                        val cols = (size.width / step).toInt()
                        val rows = (size.height / step).toInt()
                        for (r in 1..rows) {
                            for (c in 1..cols) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.12f),
                                    radius = 1.dp.toPx(),
                                    center = Offset(c * step, r * step)
                                )
                            }
                        }
                    }
                }
                .border(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(
                        colors = when {
                            glass.isDesertDuneClay -> listOf(
                                Color.White,
                                DesertClayPeachTerracotta.copy(alpha = 0.75f),
                                Color.White.copy(alpha = 0.90f)
                            )
                            glass.isEnchantedForestCodex -> listOf(
                                ForestSunlightGold.copy(alpha = 0.85f),
                                ForestParchmentCream.copy(alpha = 0.75f),
                                ForestSageGreen.copy(alpha = 0.80f)
                            )
                            glass.isNothingOs -> listOf(
                                Color.White.copy(alpha = 0.92f),
                                NothingCrimsonRed.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.55f),
                                NothingCrimsonRed.copy(alpha = 0.85f)
                            )
                            glass.isGalacticCodex -> listOf(
                                WarmGold.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.85f),
                                CrystalTeal.copy(alpha = 0.75f),
                                WarmGold.copy(alpha = 0.85f)
                            )
                            else -> listOf(
                                Color.White.copy(alpha = 0.95f),
                                LiquidCyan.copy(alpha = 0.7f),
                                IridescentPink.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.9f)
                            )
                        }
                    ),
                    shape = heroShape
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Translucent Glass PDF Document Icon Cube
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .rotate(-6f)
                    .shadow(
                        16.dp,
                        RoundedCornerShape(24.dp),
                        ambientColor = when {
                            glass.isDesertDuneClay -> DesertClayPeachTerracotta
                            glass.isEnchantedForestCodex -> ForestSageGreen
                            glass.isNothingOs -> NothingCrimsonRed
                            else -> LiquidCyan
                        },
                        spotColor = when {
                            glass.isDesertDuneClay -> DesertClayWarmAmber
                            glass.isEnchantedForestCodex -> ForestSunlightGold
                            glass.isNothingOs -> Color.White
                            glass.isGalacticCodex -> WarmGold
                            else -> IridescentPink
                        }
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = when {
                                glass.isDesertDuneClay -> listOf(
                                    DesertClayPeachTerracotta,
                                    DesertClaySoftApricot,
                                    DesertClayWarmAmber
                                )
                                glass.isEnchantedForestCodex -> listOf(
                                    ForestSageGreen,
                                    ForestFernGreen,
                                    Color(0xFF1B4332)
                                )
                                glass.isNothingOs -> listOf(
                                    Color(0xFF1C2029).copy(alpha = 0.90f),
                                    NothingCrimsonRed.copy(alpha = 0.82f),
                                    Color(0xFF0E1117).copy(alpha = 0.92f)
                                )
                                glass.isGalacticCodex -> listOf(
                                    Color(0xFF0E3A47).copy(alpha = 0.82f),
                                    LiquidCyan.copy(alpha = 0.55f),
                                    CrystalTeal.copy(alpha = 0.65f),
                                    WarmGold.copy(alpha = 0.45f)
                                )
                                else -> listOf(
                                    LiquidCyan.copy(alpha = 0.85f),
                                    ElectricBlue.copy(alpha = 0.85f),
                                    PrismPurple.copy(alpha = 0.9f),
                                    IridescentPink.copy(alpha = 0.85f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 1.8.dp,
                        brush = Brush.linearGradient(
                            colors = when {
                                glass.isDesertDuneClay -> listOf(Color.White, DesertClaySoftApricot, Color.White)
                                glass.isEnchantedForestCodex -> listOf(ForestParchmentCream, ForestSunlightGold, ForestSageGreen)
                                glass.isNothingOs -> listOf(Color.White, NothingCrimsonRed, Color.White)
                                glass.isGalacticCodex -> listOf(Color.White, LiquidCyan, WarmGold)
                                else -> listOf(Color.White, LiquidCyan, Color.White)
                            }
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = if (glass.isGalacticCodex || glass.isNothingOs) 0.18f else 0.28f))
                        .border(1.1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "PDF Document",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            val heroPrimaryTextColor = if (glass.isDesertDuneClay) DesertClayCocoaBrown else Color.White
            val heroSecondaryTextColor = if (glass.isDesertDuneClay) DesertClayCocoaBrown.copy(alpha = 0.84f) else Color.White.copy(alpha = 0.92f)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (glass.isNothingOs) "NOTHING // GLYPH READER" else stringResource(R.string.hero_badge),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = when {
                        glass.isNothingOs -> NothingCrimsonRed
                        glass.isDesertDuneClay -> DesertClayCocoaBrown.copy(alpha = 0.75f)
                        glass.isEnchantedForestCodex -> ForestSunlightGold
                        else -> Color.White.copy(alpha = 0.88f)
                    }
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        glass.isNothingOs -> "OPEN DOCUMENT [PDF]"
                        glass.isGalacticCodex -> "Discover Your Codex"
                        else -> stringResource(R.string.hero_title)
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = if (glass.isEnchantedForestCodex) MerriweatherSerifFamily else MaterialTheme.typography.headlineLarge.fontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (glass.isGalacticCodex || glass.isNothingOs) 19.sp else 22.sp
                    ),
                    color = heroPrimaryTextColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        glass.isNothingOs -> "Smoked liquid glass • Dot-matrix precision rendering."
                        glass.isGalacticCodex -> "Explore, engage, or dissect your files."
                        else -> stringResource(R.string.hero_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = heroSecondaryTextColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Frosted Glass Pill Button "Open File ->"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = when {
                                    glass.isDesertDuneClay -> listOf(
                                        DesertClayPeachTerracotta,
                                        DesertClaySoftApricot
                                    )
                                    glass.isEnchantedForestCodex -> listOf(
                                        ForestParchmentCream,
                                        ForestWarmSand
                                    )
                                    glass.isNothingOs -> listOf(
                                        NothingCrimsonRed.copy(alpha = 0.88f),
                                        Color(0xFF1F2430).copy(alpha = 0.85f)
                                    )
                                    glass.isGalacticCodex -> listOf(
                                        Color(0xFF1E293B).copy(alpha = 0.68f),
                                        LiquidCyan.copy(alpha = 0.52f)
                                    )
                                    else -> listOf(
                                        Color.White.copy(alpha = 0.34f),
                                        LiquidCyan.copy(alpha = 0.35f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = 1.3.dp,
                            brush = Brush.horizontalGradient(
                                colors = when {
                                    glass.isDesertDuneClay -> listOf(Color.White, DesertClayPeachTerracotta)
                                    glass.isEnchantedForestCodex -> listOf( Color.White, ForestSunlightGold)
                                    glass.isNothingOs -> listOf(Color.White.copy(alpha = 0.95f), NothingCrimsonRed.copy(alpha = 0.90f))
                                    else -> listOf(Color.White.copy(alpha = 0.90f), LiquidCyan.copy(alpha = 0.85f))
                                }
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .testTag("hero_open_file_button")
                        .clickable(onClick = onOpenSelectPdf)
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val btnTextColor = if (glass.isEnchantedForestCodex) ForestDeepCanopy else Color.White
                    Text(
                        text = stringResource(R.string.btn_open_file),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = btnTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = btnTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = heroPrimaryTextColor.copy(alpha = 0.85f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun QuickActionGlassCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    primaryTint: Color,
    secondaryTint: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier.width(142.dp),
    isConstellationCard: Boolean = false
) {
    val glass = LocalGlassColors.current
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .height(if (glass.isGalacticCodex) 162.dp else 148.dp)
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                ambientColor = primaryTint.copy(alpha = 0.38f),
                spotColor = secondaryTint.copy(alpha = 0.38f)
            )
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = when {
                        glass.isDesertDuneClay -> listOf(
                            DesertClayCardElevated,
                            DesertClayCardSurface,
                            primaryTint.copy(alpha = 0.25f)
                        )
                        glass.isEnchantedForestCodex -> listOf(
                            Color(0xFF143C2C).copy(alpha = 0.90f),
                            primaryTint.copy(alpha = 0.26f),
                            ForestDeepCanopy.copy(alpha = 0.92f)
                        )
                        glass.isGalacticCodex -> listOf(
                            primaryTint.copy(alpha = 0.32f),
                            secondaryTint.copy(alpha = 0.22f),
                            Color(0xFF0A1420).copy(alpha = 0.88f)
                        )
                        glass.isDark -> listOf(
                            primaryTint.copy(alpha = 0.38f),
                            secondaryTint.copy(alpha = 0.25f),
                            Color(0xFF141D38).copy(alpha = 0.85f)
                        )
                        else -> listOf(
                            primaryTint.copy(alpha = 0.42f),
                            secondaryTint.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.55f)
                        )
                    }
                )
            )
            .drawBehind {
                if (isConstellationCard) {
                    // Subtle star constellation lines inside the Bookmarks card matching the reference photo
                    val p1 = Offset(size.width * 0.55f, size.height * 0.48f)
                    val p2 = Offset(size.width * 0.72f, size.height * 0.36f)
                    val p3 = Offset(size.width * 0.86f, size.height * 0.42f)
                    val p4 = Offset(size.width * 0.78f, size.height * 0.22f)
                    val lineColor = Color.White.copy(alpha = 0.22f)
                    drawLine(lineColor, p1, p2, strokeWidth = 1.dp.toPx())
                    drawLine(lineColor, p2, p3, strokeWidth = 1.dp.toPx())
                    drawLine(lineColor, p2, p4, strokeWidth = 1.dp.toPx())
                    listOf(p1, p2, p3, p4).forEach { pt ->
                        drawCircle(Color.White.copy(alpha = 0.55f), radius = 2.dp.toPx(), center = pt)
                    }
                }
            }
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (glass.isGalacticCodex) 0.78f else 0.95f),
                        primaryTint.copy(alpha = 0.55f),
                        Color.White.copy(alpha = if (glass.isGalacticCodex) 0.48f else 0.7f)
                    )
                ),
                shape = cardShape
            )
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Glowing 3D Glass Squircle Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, RoundedCornerShape(15.dp), ambientColor = primaryTint)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryTint, secondaryTint)
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        color = Color.White.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = if (glass.isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = if (glass.isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentPdfGlassRow(
    doc: PdfDocumentEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val glass = LocalGlassColors.current
    var menuExpanded by remember { mutableStateOf(false) }
    val accentColor = Color(doc.accentHex)
    val rowShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (glass.isDark) {
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            accentColor.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.72f),
                            accentColor.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.58f)
                        )
                    }
                )
            )
            .border(
                width = 1.1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.65f),
                        Color.White.copy(alpha = if (glass.isDark) 0.35f else 0.9f)
                    )
                ),
                shape = rowShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Vertical Color Accent Bar + Frosted Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp
                    ),
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "PDF  •  ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}  •  ${GlassPaperViewModel.formatRelativeTime(doc.lastOpenedTimestamp)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = glass.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Document options",
                        tint = glass.textSecondary
                    )
                }
                LiquidGlassDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    LiquidGlassDropdownMenuItem(
                        text = "Open PDF",
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
                    LiquidGlassDropdownMenuItem(
                        text = if (doc.isFavorite) "Remove Favorite" else "Add to Favorites",
                        onClick = {
                            menuExpanded = false
                            onFavoriteToggle()
                        }
                    )
                    LiquidGlassDropdownMenuItem(
                        text = "Duplicate",
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    LiquidGlassDropdownMenuItem(
                        text = "Share",
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    LiquidGlassDropdownMenuItem(
                        text = "Delete",
                        textColor = LiquidMagenta,
                        showDivider = false,
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapchatStyleStreakButton(
    streakCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .height(36.dp)
            .shadow(
                elevation = 8.dp,
                shape = pillShape,
                ambientColor = SolarAmber.copy(alpha = 0.45f),
                spotColor = Color(0xFFFF5722).copy(alpha = 0.45f)
            )
            .clip(pillShape)
            .background(
                brush = if (glass.isDark || glass.isGalacticCodex) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1F232B).copy(alpha = 0.94f),
                            Color(0xFF2B1D19).copy(alpha = 0.94f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.94f),
                            Color(0xFFFFF7ED).copy(alpha = 0.94f)
                        )
                    )
                }
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        SolarAmber.copy(alpha = 0.90f),
                        Color(0xFFFF6D00).copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.65f)
                    )
                ),
                shape = pillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDD25",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streakCount",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = (-0.2).sp
            ),
            color = if (glass.isDark || glass.isGalacticCodex) Color.White else Color(0xFF1E293B)
        )
    }
}

