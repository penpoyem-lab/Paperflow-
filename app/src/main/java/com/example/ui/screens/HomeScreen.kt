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
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.ui.MainTab
import com.example.ui.components.GlassCircularIconButton
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
                    modifier = Modifier.weight(1f)
                ) {
                    // Iridescent Glass Orb Avatar Button (with "Galaxy Explorer" outer ring in Galactic Codex theme)
                    Box(
                        modifier = Modifier
                            .size(if (glass.isGalacticCodex) 74.dp else 56.dp)
                            .testTag("home_avatar_auth_button")
                            .clickable { onOpenAuth() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (glass.isGalacticCodex) {
                            // Outer Dark Frosted "Galaxy Explorer" Orbital Ring
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shadow(18.dp, CircleShape, ambientColor = LiquidCyan, spotColor = PrismViolet)
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
                                        width = 1.4.dp,
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
                            Text(
                                text = "Galaxy Explorer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = Color.White.copy(alpha = 0.78f),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 3.dp)
                            )
                        }

                        // Inner Glowing Pearl-Cyan-Violet Orb with "P" or user initial
                        Box(
                            modifier = Modifier
                                .size(if (glass.isGalacticCodex) 48.dp else 56.dp)
                                .shadow(14.dp, CircleShape, ambientColor = LiquidCyan, spotColor = PrismPurple)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.94f),
                                            LiquidCyan.copy(alpha = 0.82f),
                                            ElectricBlue.copy(alpha = 0.85f),
                                            PrismPurple.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.8.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.White, LiquidCyan, IridescentPink)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayInitial = when {
                                authSession.isAuthenticated && authSession.userFullName.isNotBlank() ->
                                    authSession.userFullName.trim().take(1).uppercase()
                                glass.isGalacticCodex -> "P"
                                else -> null
                            }
                            if (displayInitial != null) {
                                Text(
                                    text = displayInitial,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Paperflow Account & Sign In",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        if (glass.isGalacticCodex) {
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
                                    fontSize = 22.sp,
                                    lineHeight = 26.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Librarian $librarianHandle",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            Text(
                                text = stringResource(R.string.greeting_morning),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (glass.isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E3A8A)
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
                                        fontSize = 23.sp
                                    ),
                                    color = if (glass.isDark) Color.White else Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = SolarAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.subtitle_home),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = if (glass.isGalacticCodex) 13.sp else 12.sp
                                ),
                                color = glass.textSecondary
                            )

                            if (glass.isGalacticCodex) {
                                // Glowing Blue-Violet Diamond Badge + Golden Flame Orb Badge matching the reference photo
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    LiquidCyan.copy(alpha = 0.35f),
                                                    PrismViolet.copy(alpha = 0.45f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = LiquidCyan.copy(alpha = 0.75f),
                                            shape = RoundedCornerShape(9.dp)
                                        )
                                        .clickable(onClick = onOpenStreakDetails),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Diamond,
                                        contentDescription = "Scholar Gem",
                                        tint = LiquidCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    SolarAmber.copy(alpha = 0.38f),
                                                    Color(0xFF78350F).copy(alpha = 0.55f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = WarmGold.copy(alpha = 0.80f),
                                            shape = RoundedCornerShape(9.dp)
                                        )
                                        .testTag("home_streak_badge")
                                        .clickable(onClick = onOpenStreakDetails),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Daily Streak",
                                        tint = WarmGold,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            } else {
                                val activeDays = streakData.currentStreak.coerceAtLeast(1)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    SolarAmber.copy(alpha = if (glass.isDark) 0.28f else 0.25f),
                                                    IridescentPink.copy(alpha = if (glass.isDark) 0.25f else 0.20f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = SolarAmber.copy(alpha = 0.75f),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .testTag("home_streak_badge")
                                        .clickable(onClick = onOpenStreakDetails)
                                        .padding(horizontal = 9.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Daily Streak",
                                        tint = SolarAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$activeDays day streak",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (glass.isDark) Color.White else Color(0xFF9A3412)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCircularIconButton(
                        icon = Icons.Filled.Search,
                        contentDescription = "Search documents",
                        onClick = { onNavigateToLibrary(LibraryCategory.ALL) },
                        modifier = Modifier.testTag("home_search_button")
                    )
                    Box {
                        GlassCircularIconButton(
                            icon = Icons.Filled.MoreHoriz,
                            contentDescription = "More options",
                            onClick = { showTopMenu = true },
                            modifier = Modifier.testTag("home_more_button")
                        )
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import PDF") },
                                onClick = {
                                    showTopMenu = false
                                    onOpenSelectPdf()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Create Study Note") },
                                onClick = {
                                    showTopMenu = false
                                    onOpenNotes()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Daily Reading Streak") },
                                onClick = {
                                    showTopMenu = false
                                    onOpenStreakDetails()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (authSession.isAuthenticated) {
                                            "Account (${authSession.userEmail})"
                                        } else {
                                            "Sign In / Create Account"
                                        }
                                    )
                                },
                                onClick = {
                                    showTopMenu = false
                                    onOpenAuth()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Saved Bookmarks") },
                                onClick = {
                                    showTopMenu = false
                                    onOpenBookmarks()
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
                            primaryTint = ElectricBlue,
                            secondaryTint = LiquidCyan,
                            onClick = onOpenSelectPdf,
                            testTag = "quick_action_view_pdf"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_add_notes),
                            subtitle = stringResource(R.string.action_add_notes_sub),
                            icon = Icons.Filled.Edit,
                            primaryTint = IridescentPink,
                            secondaryTint = PrismPurple,
                            onClick = onOpenNotes,
                            testTag = "quick_action_add_notes"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_bookmarks),
                            subtitle = stringResource(R.string.action_bookmarks_sub),
                            icon = Icons.Filled.Bookmark,
                            primaryTint = CrystalTeal,
                            secondaryTint = EmeraldGreen,
                            onClick = onOpenBookmarks,
                            testTag = "quick_action_bookmarks"
                        )
                        QuickActionGlassCard(
                            title = stringResource(R.string.action_recent),
                            subtitle = stringResource(R.string.action_recent_sub),
                            icon = Icons.Filled.History,
                            primaryTint = SolarAmber,
                            secondaryTint = IridescentPink,
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
    val infiniteTransition = rememberInfiniteTransition(label = "hero_shimmer")
    val lightSweep by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "light_sweep"
    )

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
                        colors = if (glass.isGalacticCodex) {
                            listOf(
                                Color(0xFF451A03).copy(alpha = 0.36f),
                                Color(0xFF0F292E).copy(alpha = 0.44f),
                                Color(0xFF064E3B).copy(alpha = 0.38f),
                                Color(0xFF78350F).copy(alpha = 0.34f)
                            )
                        } else {
                            listOf(
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
                }
                .border(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(
                        colors = if (glass.isGalacticCodex) {
                            listOf(
                                WarmGold.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.85f),
                                CrystalTeal.copy(alpha = 0.75f),
                                WarmGold.copy(alpha = 0.85f)
                            )
                        } else {
                            listOf(
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
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Translucent Glass PDF Document Icon Cube
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .rotate(-6f)
                    .shadow(
                        16.dp,
                        RoundedCornerShape(26.dp),
                        ambientColor = LiquidCyan,
                        spotColor = if (glass.isGalacticCodex) WarmGold else IridescentPink
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (glass.isGalacticCodex) {
                                listOf(
                                    Color(0xFF0E3A47).copy(alpha = 0.82f),
                                    LiquidCyan.copy(alpha = 0.55f),
                                    CrystalTeal.copy(alpha = 0.65f),
                                    WarmGold.copy(alpha = 0.45f)
                                )
                            } else {
                                listOf(
                                    LiquidCyan.copy(alpha = 0.85f),
                                    ElectricBlue.copy(alpha = 0.85f),
                                    PrismPurple.copy(alpha = 0.9f),
                                    IridescentPink.copy(alpha = 0.85f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = if (glass.isGalacticCodex) {
                                listOf(Color.White, LiquidCyan, WarmGold)
                            } else {
                                listOf(Color.White, LiquidCyan, Color.White)
                            }
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = if (glass.isGalacticCodex) 0.18f else 0.28f))
                        .border(1.2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "PDF Document",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.hero_badge),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.88f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (glass.isGalacticCodex) "Discover Your Codex" else stringResource(R.string.hero_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (glass.isGalacticCodex) 22.sp else 25.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (glass.isGalacticCodex) {
                        "Explore, engage, or dissect your files."
                    } else {
                        stringResource(R.string.hero_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Frosted Glass Pill Button "Open File ->"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (glass.isGalacticCodex) {
                                    listOf(
                                        Color(0xFF1E293B).copy(alpha = 0.68f),
                                        LiquidCyan.copy(alpha = 0.52f)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.34f),
                                        LiquidCyan.copy(alpha = 0.35f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = 1.3.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.90f), LiquidCyan.copy(alpha = 0.85f))
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .testTag("hero_open_file_button")
                        .clickable(onClick = onOpenSelectPdf)
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.btn_open_file),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
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
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open PDF") },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (doc.isFavorite) "Remove Favorite" else "Add to Favorites") },
                        onClick = {
                            menuExpanded = false
                            onFavoriteToggle()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = LiquidMagenta) },
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
