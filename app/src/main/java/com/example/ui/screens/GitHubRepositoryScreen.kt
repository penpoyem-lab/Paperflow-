package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PaperflowGitHubRepoData
import com.example.data.RepoCommitEntry
import com.example.data.RepoFileNode
import com.example.data.RepoNodeType
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.WarmGold

private enum class GitHubTopTab(val label: String, val icon: ImageVector, val badgeCount: Int? = null) {
    CODE("Code", Icons.Filled.Code),
    ISSUES("Issues", Icons.Filled.BugReport, 2),
    PULL_REQUESTS("Pull requests", Icons.AutoMirrored.Filled.CallSplit, 1),
    ACTIONS("Actions", Icons.Filled.PlayArrow),
    SECURITY("Security", Icons.Filled.Security),
    INSIGHTS("Insights", Icons.Filled.Info)
}

private enum class DocPreviewTab {
    README,
    MIT_LICENSE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GitHubRepositoryScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val glass = LocalGlassColors.current
    val isDark = glass.isDark

    // Authentic GitHub Dark Dimmed / Dark Default palette + Clean Light Mode palette
    val ghCanvasBg = if (isDark) Color(0xFF0D1117) else Color(0xFFFFFFFF)
    val ghHeaderBg = if (isDark) Color(0xFF010409) else Color(0xFFF6F8FA)
    val ghSurfaceBg = if (isDark) Color(0xFF161B22) else Color(0xFFF6F8FA)
    val ghSurfaceElevated = if (isDark) Color(0xFF21262D) else Color(0xFFEFF2F5)
    val ghBorderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)
    val ghTextPrimary = if (isDark) Color(0xFFE6EDF3) else Color(0xFF1F2328)
    val ghTextSecondary = if (isDark) Color(0xFF8B949E) else Color(0xFF656D76)
    val ghLinkBlue = if (isDark) Color(0xFF58A6FF) else Color(0xFF0969DA)
    val ghGreenButton = Color(0xFF238636)
    val ghFolderBlue = if (isDark) Color(0xFF58A6FF) else Color(0xFF54AEFF)

    // Interactive Repository State
    var selectedTopTab by remember { mutableStateOf(GitHubTopTab.CODE) }
    var selectedBranch by remember { mutableStateOf(PaperflowGitHubRepoData.DEFAULT_BRANCH) }
    var showBranchMenu by remember { mutableStateOf(false) }
    var showAddFileMenu by remember { mutableStateOf(false) }
    var showCodeModal by remember { mutableStateOf(false) }
    var showCommitHistoryModal by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }

    var isStarred by remember { mutableStateOf(true) }
    var starCount by remember { mutableIntStateOf(48) }
    var isWatching by remember { mutableStateOf(true) }
    var forkCount by remember { mutableIntStateOf(9) }

    // File search & directory navigation state
    var isSearchingFiles by remember { mutableStateOf(false) }
    var fileSearchQuery by remember { mutableStateOf("") }
    val currentDirectoryStack = remember { mutableStateListOf<RepoFileNode>() }
    var selectedFileForViewer by remember { mutableStateOf<RepoFileNode?>(null) }
    var selectedDocTab by remember { mutableStateOf(DocPreviewTab.README) }

    // Custom user-created files in session
    val customRootFiles = remember { mutableStateListOf<RepoFileNode>() }

    BackHandler {
        when {
            selectedFileForViewer != null -> selectedFileForViewer = null
            currentDirectoryStack.isNotEmpty() -> currentDirectoryStack.removeAt(currentDirectoryStack.lastIndex)
            isSearchingFiles -> {
                isSearchingFiles = false
                fileSearchQuery = ""
            }
            selectedTopTab != GitHubTopTab.CODE -> selectedTopTab = GitHubTopTab.CODE
            else -> onBack()
        }
    }

    val activeDirectoryNodes: List<RepoFileNode> = remember(
        currentDirectoryStack.size,
        customRootFiles.size,
        fileSearchQuery,
        isSearchingFiles
    ) {
        if (isSearchingFiles && fileSearchQuery.isNotBlank()) {
            val all = PaperflowGitHubRepoData.flattenAllFiles() + customRootFiles
            all.filter {
                it.name.contains(fileSearchQuery.trim(), ignoreCase = true) ||
                    it.path.contains(fileSearchQuery.trim(), ignoreCase = true) ||
                    it.commitMessage.contains(fileSearchQuery.trim(), ignoreCase = true)
            }
        } else if (currentDirectoryStack.isEmpty()) {
            val combined = PaperflowGitHubRepoData.rootFiles + customRootFiles
            combined.sortedWith(
                compareBy<RepoFileNode> { it.type != RepoNodeType.DIRECTORY }
                    .thenBy { it.name.lowercase() }
            )
        } else {
            currentDirectoryStack.last().children.sortedWith(
                compareBy<RepoFileNode> { it.type != RepoNodeType.DIRECTORY }
                    .thenBy { it.name.lowercase() }
            )
        }
    }

    val latestCommit = PaperflowGitHubRepoData.commitHistory.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ghCanvasBg)
            .testTag("github_repository_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // 1. TOP GITHUB NAVIGATION BAR & REPOSITORY IDENTITY HEADER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ghHeaderBg)
                ) {
                    // Top Action Bar (Back to Paperflow, GitHub Octocat/Code Emblem, Search, External Browser)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                                    .testTag("github_repo_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Paperflow",
                                    tint = ghTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // GitHub-style Monospace Mark Badge
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF21262D) else Color(0xFF1F2328))
                                    .border(1.dp, ghBorderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Terminal,
                                    contentDescription = "GitHub Repository",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "GitHub Repository",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = ghTextSecondary
                                )
                                Text(
                                    text = "${PaperflowGitHubRepoData.OWNER_USERNAME} / ${PaperflowGitHubRepoData.REPO_NAME}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ghTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    copyToClipboard(
                                        context,
                                        "Repository URL",
                                        PaperflowGitHubRepoData.REPO_URL
                                    )
                                    onShowMessage("Copied ${PaperflowGitHubRepoData.REPO_URL}")
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Copy Repository URL",
                                    tint = ghTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            PaperflowGitHubRepoData.REPO_URL.toUri()
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        onShowMessage("URL: ${PaperflowGitHubRepoData.REPO_URL}")
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open in Browser",
                                    tint = ghTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // Owner / Repository Breadcrumb Title + Public Pill Badge + Star / Fork / Watch Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = ghTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = PaperflowGitHubRepoData.OWNER_USERNAME,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 17.sp
                                ),
                                color = ghLinkBlue,
                                modifier = Modifier.clickable {
                                    currentDirectoryStack.clear()
                                    selectedFileForViewer = null
                                }
                            )
                            Text(
                                text = " / ",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                                color = ghTextSecondary
                            )
                            Text(
                                text = PaperflowGitHubRepoData.REPO_NAME,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = ghLinkBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clickable {
                                        currentDirectoryStack.clear()
                                        selectedFileForViewer = null
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // "Public" pill badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = PaperflowGitHubRepoData.VISIBILITY_BADGE,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = ghTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pin / Watch / Fork / Star Action Pills (matching Reference Image 1 & 2)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Star button
                            GitHubHeaderActionPill(
                                icon = if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                iconTint = if (isStarred) Color(0xFFE3B341) else ghTextSecondary,
                                label = if (isStarred) "Starred" else "Star",
                                count = starCount.toString(),
                                bgColor = ghSurfaceElevated,
                                borderColor = ghBorderColor,
                                textColor = ghTextPrimary,
                                secondaryColor = ghTextSecondary,
                                onClick = {
                                    isStarred = !isStarred
                                    starCount += if (isStarred) 1 else -1
                                }
                            )

                            // Watch / Notifications button
                            GitHubHeaderActionPill(
                                icon = Icons.Filled.Visibility,
                                iconTint = ghTextSecondary,
                                label = if (isWatching) "Unwatch" else "Watch",
                                count = "12",
                                bgColor = ghSurfaceElevated,
                                borderColor = ghBorderColor,
                                textColor = ghTextPrimary,
                                secondaryColor = ghTextSecondary,
                                onClick = {
                                    isWatching = !isWatching
                                    onShowMessage(
                                        if (isWatching) "Watching repository notifications"
                                        else "Unwatched repository"
                                    )
                                }
                            )

                            // Fork button
                            GitHubHeaderActionPill(
                                icon = Icons.AutoMirrored.Filled.CallSplit,
                                iconTint = ghTextSecondary,
                                label = "Fork",
                                count = forkCount.toString(),
                                bgColor = ghSurfaceElevated,
                                borderColor = ghBorderColor,
                                textColor = ghTextPrimary,
                                secondaryColor = ghTextSecondary,
                                onClick = {
                                    forkCount += 1
                                    onShowMessage("Created fork metadata preview (#$forkCount)")
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Top GitHub Navigation Tabs (Code, Issues, Pull requests, Actions, Security, Insights)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GitHubTopTab.entries.forEach { tab ->
                            val isSelected = selectedTopTab == tab
                            Column(
                                modifier = Modifier
                                    .clickable { selectedTopTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) ghTextPrimary else ghTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.5.sp
                                        ),
                                        color = if (isSelected) ghTextPrimary else ghTextSecondary
                                    )
                                    if (tab.badgeCount != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(ghSurfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = tab.badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = ghTextPrimary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.5.dp)
                                        .width(if (isSelected) 64.dp else 0.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isSelected) Color(0xFFF78166) // Authentic GitHub orange-coral active tab underline
                                            else Color.Transparent
                                        )
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = ghBorderColor)
                }
            }

            // If a non-Code tab is selected, show an authentic interactive panel with a direct return to Code
            if (selectedTopTab != GitHubTopTab.CODE) {
                item {
                    NonCodeTabPreviewSection(
                        tab = selectedTopTab,
                        ghSurfaceBg = ghSurfaceBg,
                        ghBorderColor = ghBorderColor,
                        ghTextPrimary = ghTextPrimary,
                        ghTextSecondary = ghTextSecondary,
                        ghLinkBlue = ghLinkBlue,
                        onReturnToCode = { selectedTopTab = GitHubTopTab.CODE }
                    )
                }
                return@LazyColumn
            }

            // 2. REPOSITORY CONTROLS BAR (Branch Selector, Branches/Tags Count, Go to file, Add file, Green <> Code Button)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Branch Selector Dropdown + Branch / Tag Counters
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ghSurfaceElevated)
                                        .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                                        .clickable { showBranchMenu = true }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                        .testTag("github_branch_selector"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountTree,
                                        contentDescription = "Branch",
                                        tint = ghTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedBranch,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        color = ghTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = ghTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showBranchMenu,
                                    onDismissRequest = { showBranchMenu = false }
                                ) {
                                    Text(
                                        text = "Switch branches/tags",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ghTextSecondary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                    PaperflowGitHubRepoData.branches.forEach { branchName ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (branchName == selectedBranch) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(branchName)
                                                }
                                            },
                                            onClick = {
                                                selectedBranch = branchName
                                                showBranchMenu = false
                                                onShowMessage("Switched branch to $branchName")
                                            }
                                        )
                                    }
                                }
                            }

                            // Branches & Tags Metadata Counters
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showBranchMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountTree,
                                    contentDescription = null,
                                    tint = ghTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${PaperflowGitHubRepoData.branches.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ghTextPrimary
                                )
                                Text(
                                    text = " Branches",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ghTextSecondary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Filled.LocalOffer,
                                    contentDescription = null,
                                    tint = ghTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${PaperflowGitHubRepoData.tags.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ghTextPrimary
                                )
                                Text(
                                    text = " Tags",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ghTextSecondary
                                )
                            }
                        }
                    }

                    // Second Row of Controls on Mobile: Go to file search input, Add file, Green <> Code Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Go to file button / search toggle
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ghSurfaceBg)
                                .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                                .clickable { isSearchingFiles = !isSearchingFiles }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("github_go_to_file_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Go to file",
                                tint = ghTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (fileSearchQuery.isNotBlank()) fileSearchQuery else "Go to file",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = if (fileSearchQuery.isNotBlank()) ghTextPrimary else ghTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "t",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    color = ghTextSecondary
                                )
                            }
                        }

                        // "Add file" dropdown button
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ghSurfaceElevated)
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                                    .clickable { showAddFileMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add file",
                                    tint = ghTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add file",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = ghTextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = ghTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showAddFileMenu,
                                onDismissRequest = { showAddFileMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Create new file") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Add, contentDescription = null)
                                    },
                                    onClick = {
                                        showAddFileMenu = false
                                        showCreateFileDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Copy repository clone command") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                                    },
                                    onClick = {
                                        showAddFileMenu = false
                                        copyToClipboard(
                                            context,
                                            "Git Clone",
                                            "git clone ${PaperflowGitHubRepoData.REPO_URL}.git"
                                        )
                                        onShowMessage("Copied git clone command")
                                    }
                                )
                            }
                        }

                        // Authentic Green "<> Code" Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ghGreenButton)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { showCodeModal = true }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("github_code_clone_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Code",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Expandable Search Input Box when "Go to file" is active
                    AnimatedVisibility(
                        visible = isSearchingFiles,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = fileSearchQuery,
                            onValueChange = { fileSearchQuery = it },
                            placeholder = {
                                Text(
                                    "Filter files by name, path, or commit message...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ghTextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = ghLinkBlue
                                )
                            },
                            trailingIcon = {
                                if (fileSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { fileSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear filter",
                                            tint = ghTextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ghLinkBlue,
                                unfocusedBorderColor = ghBorderColor,
                                focusedTextColor = ghTextPrimary,
                                unfocusedTextColor = ghTextPrimary,
                                focusedContainerColor = ghSurfaceBg,
                                unfocusedContainerColor = ghSurfaceBg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    // Directory Breadcrumb Bar (when inside a subfolder or viewing a file)
                    if (currentDirectoryStack.isNotEmpty() || selectedFileForViewer != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .clip(RoundedCornerShape(6.dp))
                                .background(ghSurfaceBg)
                                .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = PaperflowGitHubRepoData.REPO_NAME,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ghLinkBlue,
                                modifier = Modifier.clickable {
                                    currentDirectoryStack.clear()
                                    selectedFileForViewer = null
                                }
                            )
                            currentDirectoryStack.forEachIndexed { idx, dirNode ->
                                Text(
                                    text = " / ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ghTextSecondary
                                )
                                Text(
                                    text = dirNode.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (idx == currentDirectoryStack.lastIndex && selectedFileForViewer == null)
                                            FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (idx == currentDirectoryStack.lastIndex && selectedFileForViewer == null)
                                        ghTextPrimary else ghLinkBlue,
                                    modifier = Modifier.clickable {
                                        selectedFileForViewer = null
                                        while (currentDirectoryStack.size > idx + 1) {
                                            currentDirectoryStack.removeAt(currentDirectoryStack.lastIndex)
                                        }
                                    }
                                )
                            }
                            selectedFileForViewer?.let { file ->
                                Text(
                                    text = " / ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ghTextSecondary
                                )
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ghTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. FILE CODE VIEWER OR COMPLETE GITHUB REPOSITORY FILE EXPLORER TABLE
            if (selectedFileForViewer != null) {
                val file = selectedFileForViewer!!
                item {
                    GitHubFileContentViewerCard(
                        file = file,
                        ghSurfaceBg = ghSurfaceBg,
                        ghSurfaceElevated = ghSurfaceElevated,
                        ghBorderColor = ghBorderColor,
                        ghTextPrimary = ghTextPrimary,
                        ghTextSecondary = ghTextSecondary,
                        ghLinkBlue = ghLinkBlue,
                        onCloseFile = { selectedFileForViewer = null },
                        onCopyContent = {
                            copyToClipboard(context, file.name, file.content)
                            onShowMessage("Copied ${file.name} to clipboard")
                        }
                    )
                }
            } else {
                // COMPLETE REPOSITORY FILE TABLE (LATEST COMMIT HEADER ROW + FILE ROWS)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                            .background(ghCanvasBg)
                    ) {
                        // A. Latest Commit Banner Row (matching Reference Image 1 & 2)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ghSurfaceBg)
                                .clickable { showCommitHistoryModal = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Author Avatar Circle ("P")
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(ElectricBlue, PrismViolet)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = latestCommit.author,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = ghTextPrimary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = latestCommit.message,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = ghTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right: Green Checkmark + Short Commit Hash + Relative Timestamp + Commits Count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Checks passed",
                                    tint = Color(0xFF3FB950),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = latestCommit.hash,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp
                                    ),
                                    color = ghTextSecondary
                                )
                                Text(
                                    text = "· ${latestCommit.relativeDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = ghTextSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = "Commit history",
                                        tint = ghTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${PaperflowGitHubRepoData.commitHistory.size} Commits",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = ghTextPrimary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

                        // Optional ".." parent directory row when inside a subdirectory
                        if (currentDirectoryStack.isNotEmpty() && !isSearchingFiles) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentDirectoryStack.removeAt(currentDirectoryStack.lastIndex)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FolderOpen,
                                    contentDescription = "Parent directory",
                                    tint = ghFolderBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "..",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = ghLinkBlue
                                )
                            }
                            HorizontalDivider(thickness = 1.dp, color = ghBorderColor)
                        }

                        // B. File & Directory Rows (Name | Commit Message | Relative Timestamp)
                        activeDirectoryNodes.forEachIndexed { index, node ->
                            val isDirectory = node.type == RepoNodeType.DIRECTORY
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isDirectory) {
                                            isSearchingFiles = false
                                            fileSearchQuery = ""
                                            currentDirectoryStack.add(node)
                                        } else {
                                            selectedFileForViewer = node
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("repo_node_${node.name}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Column: Folder / File Icon + Name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.15f)
                                ) {
                                    Icon(
                                        imageVector = if (isDirectory) Icons.Filled.Folder else Icons.Filled.Description,
                                        contentDescription = if (isDirectory) "Directory" else "File",
                                        tint = if (isDirectory) ghFolderBlue else ghTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isSearchingFiles) node.path else node.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 13.5.sp
                                        ),
                                        color = ghTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Center Column: Commit Message
                                Text(
                                    text = node.commitMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = ghTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1.35f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Right Column: Relative Updated Time
                                Text(
                                    text = node.lastUpdatedRelative,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = ghTextSecondary
                                )
                            }

                            if (index < activeDirectoryNodes.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = ghBorderColor)
                            }
                        }
                    }
                }
            }

            // 4. README & MIT LICENSE TABBED DOCUMENTATION VIEWER (Matching Reference Image 2)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                        .background(ghCanvasBg)
                ) {
                    // Sticky-style Tab Header: [ 📖 README ]  [ ⚖️ MIT license ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ghSurfaceBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // README Tab
                            val isReadmeActive = selectedDocTab == DocPreviewTab.README
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isReadmeActive) ghSurfaceElevated else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isReadmeActive) Color(0xFFF78166) else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedDocTab = DocPreviewTab.README }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("github_doc_tab_readme"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = if (isReadmeActive) ghTextPrimary else ghTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "README",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isReadmeActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isReadmeActive) ghTextPrimary else ghTextSecondary
                                )
                            }

                            // MIT license Tab
                            val isLicenseActive = selectedDocTab == DocPreviewTab.MIT_LICENSE
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLicenseActive) ghSurfaceElevated else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isLicenseActive) Color(0xFFF78166) else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedDocTab = DocPreviewTab.MIT_LICENSE }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("github_doc_tab_license"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Gavel,
                                    contentDescription = null,
                                    tint = if (isLicenseActive) ghTextPrimary else ghTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MIT license",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isLicenseActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isLicenseActive) ghTextPrimary else ghTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val textToCopy = if (selectedDocTab == DocPreviewTab.README) {
                                    PaperflowGitHubRepoData.readmeMarkdownContent
                                } else {
                                    PaperflowGitHubRepoData.licenseContent
                                }
                                copyToClipboard(context, selectedDocTab.name, textToCopy)
                                onShowMessage("Copied ${selectedDocTab.name} content")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy documentation",
                                tint = ghTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

                    if (selectedDocTab == DocPreviewTab.README) {
                        PaperflowReadmeRichRender(
                            ghSurfaceBg = ghSurfaceBg,
                            ghSurfaceElevated = ghSurfaceElevated,
                            ghBorderColor = ghBorderColor,
                            ghTextPrimary = ghTextPrimary,
                            ghTextSecondary = ghTextSecondary,
                            ghLinkBlue = ghLinkBlue,
                            onCopyCloneCommand = { cmd ->
                                copyToClipboard(context, "Command", cmd)
                                onShowMessage("Copied command to clipboard")
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ghSurfaceBg)
                                    .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "MIT License",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ghTextPrimary
                                    )
                                    Text(
                                        text = "Permissive open-source license • Commercial use, modification, distribution, and private use permitted.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ghTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF3FB950),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = PaperflowGitHubRepoData.licenseContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                color = ghTextPrimary
                            )
                        }
                    }
                }
            }

            // 5. GITHUB REPOSITORY SIDEBAR / METADATA PANEL (About, Topics, Releases, Packages, Languages)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
                        .background(ghSurfaceBg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = ghTextPrimary
                    )

                    Text(
                        text = PaperflowGitHubRepoData.ABOUT_DESCRIPTION,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = ghTextPrimary
                    )

                    // Website link
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            tint = ghTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = PaperflowGitHubRepoData.REPO_URL.removePrefix("https://"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ghLinkBlue,
                            modifier = Modifier.clickable {
                                copyToClipboard(context, "Repo Link", PaperflowGitHubRepoData.REPO_URL)
                                onShowMessage("Copied repository link")
                            }
                        )
                    }

                    // Topic Pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaperflowGitHubRepoData.topics.forEach { topic ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ghLinkBlue.copy(alpha = 0.14f))
                                    .border(1.dp, ghLinkBlue.copy(alpha = 0.32f), RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = ghLinkBlue
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

                    // Releases section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Releases",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ghTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Paperflow v2.6.0 — Galactic Codex & 165Hz Liquid Glass",
                                style = MaterialTheme.typography.bodySmall,
                                color = ghTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, Color(0xFF3FB950), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Latest",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = Color(0xFF3FB950)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

                    // Languages Breakdown Bar
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Languages",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ghTextPrimary
                        )

                        // Multi-segment progress bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(0.942f)
                                    .fillMaxSize()
                                    .background(Color(0xFFA97BFF)) // Kotlin purple
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.038f)
                                    .fillMaxSize()
                                    .background(Color(0xFF0060AC)) // XML blue
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.020f)
                                    .fillMaxSize()
                                    .background(Color(0xFF3FB950)) // Gradle green
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LanguageLegendDot("Kotlin", "94.2%", Color(0xFFA97BFF), ghTextPrimary, ghTextSecondary)
                            LanguageLegendDot("XML", "3.8%", Color(0xFF0060AC), ghTextPrimary, ghTextSecondary)
                            LanguageLegendDot("Gradle", "2.0%", Color(0xFF3FB950), ghTextPrimary, ghTextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Green "<> Code" Clone & Download Dialog
    if (showCodeModal) {
        AlertDialog(
            onDismissRequest = { showCodeModal = false },
            containerColor = ghSurfaceBg,
            titleContentColor = ghTextPrimary,
            textContentColor = ghTextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = null,
                        tint = ghLinkBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clone or Download Repository", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "HTTPS Clone URL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ghTextPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ghCanvasBg)
                            .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${PaperflowGitHubRepoData.REPO_URL}.git",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp
                            ),
                            color = ghTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                copyToClipboard(
                                    context,
                                    "HTTPS Clone URL",
                                    "${PaperflowGitHubRepoData.REPO_URL}.git"
                                )
                                onShowMessage("Copied HTTPS clone URL")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy URL",
                                tint = ghLinkBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "GitHub CLI",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ghTextPrimary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ghCanvasBg)
                            .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ghCli = "gh repo clone ${PaperflowGitHubRepoData.OWNER_USERNAME}/${PaperflowGitHubRepoData.REPO_NAME}"
                        Text(
                            text = ghCli,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp
                            ),
                            color = ghTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                copyToClipboard(context, "GitHub CLI", ghCli)
                                onShowMessage("Copied GitHub CLI command")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy CLI",
                                tint = ghLinkBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCodeModal = false
                        onShowMessage("Prepared ${PaperflowGitHubRepoData.REPO_NAME}-main.zip archive info")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ghGreenButton)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download ZIP", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCodeModal = false }) {
                    Text("Close", color = ghTextSecondary)
                }
            }
        )
    }

    // Commit History Modal
    if (showCommitHistoryModal) {
        AlertDialog(
            onDismissRequest = { showCommitHistoryModal = false },
            containerColor = ghSurfaceBg,
            titleContentColor = ghTextPrimary,
            textContentColor = ghTextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = ghLinkBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Commit History ($selectedBranch)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaperflowGitHubRepoData.commitHistory.forEach { commit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(ghCanvasBg)
                                .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = commit.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ghTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${commit.author} committed ${commit.relativeDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ghTextSecondary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .border(1.dp, Color(0xFF3FB950), RoundedCornerShape(50))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "Verified",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(0xFF3FB950)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = commit.hash,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = ghLinkBlue
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommitHistoryModal = false }) {
                    Text("Done", color = ghLinkBlue)
                }
            }
        )
    }

    // Create New File in Repository Dialog
    if (showCreateFileDialog) {
        var newFileName by remember { mutableStateOf("") }
        var newFileCommitMsg by remember { mutableStateOf("feat: add custom module file") }
        var newFileContent by remember { mutableStateOf("// Created in Paperflow Repository Explorer\n") }

        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            containerColor = ghSurfaceBg,
            titleContentColor = ghTextPrimary,
            title = { Text("Create new file in $selectedBranch", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File name (e.g., docs/ARCHITECTURE.md)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFileCommitMsg,
                        onValueChange = { newFileCommitMsg = it },
                        label = { Text("Commit message") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFileContent,
                        onValueChange = { newFileContent = it },
                        label = { Text("File contents") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = newFileName.trim().ifBlank { "NOTES.md" }
                        val created = RepoFileNode(
                            name = cleanName,
                            path = cleanName,
                            type = RepoNodeType.FILE,
                            commitMessage = newFileCommitMsg.trim().ifBlank { "Add $cleanName" },
                            lastUpdatedRelative = "just now",
                            languageOrExt = cleanName.substringAfterLast('.', "Text"),
                            sizeLabel = "${newFileContent.length} B",
                            content = newFileContent
                        )
                        customRootFiles.add(created)
                        showCreateFileDialog = false
                        onShowMessage("Committed $cleanName to $selectedBranch")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ghGreenButton)
                ) {
                    Text("Commit new file", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) {
                    Text("Cancel", color = ghTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun GitHubHeaderActionPill(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    count: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = textColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(borderColor.copy(alpha = 0.45f))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = secondaryColor
            )
        }
    }
}

@Composable
private fun GitHubFileContentViewerCard(
    file: RepoFileNode,
    ghSurfaceBg: Color,
    ghSurfaceElevated: Color,
    ghBorderColor: Color,
    ghTextPrimary: Color,
    ghTextSecondary: Color,
    ghLinkBlue: Color,
    onCloseFile: () -> Unit,
    onCopyContent: () -> Unit
) {
    val lines = remember(file.content) { file.content.lines() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
    ) {
        // File Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ghSurfaceBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = ghTextPrimary
                )
                Text(
                    text = "${lines.size} lines • ${file.sizeLabel.ifBlank { "1.2 KB" }} • ${file.languageOrExt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ghTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onCopyContent,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy file contents",
                        tint = ghTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(
                    onClick = onCloseFile,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close file viewer",
                        tint = ghTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

        // Line-numbered source code view
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            lines.forEachIndexed { idx, lineText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${idx + 1}".padStart(3, ' '),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = ghTextSecondary.copy(alpha = 0.65f),
                        modifier = Modifier.width(34.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = lineText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = ghTextPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaperflowReadmeRichRender(
    ghSurfaceBg: Color,
    ghSurfaceElevated: Color,
    ghBorderColor: Color,
    ghTextPrimary: Color,
    ghTextSecondary: Color,
    ghLinkBlue: Color,
    onCopyCloneCommand: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Centered Hero Branding Banner inside README (inspired by Reference Image 1 & 2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Paperflow App Icon Emblem
            Image(
                painter = painterResource(id = R.drawable.paperflow_red_black_icon_1791443745243),
                contentDescription = "Paperflow App Icon",
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, ghBorderColor, RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Paperflow — PDF Reader, Toolkit & Notes",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = ghTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "High-Refresh Liquid Glass PDF Reader • 20 PDF Tools • Universal AI Translator • Study Notes",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = ghTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // GitHub Shield-style Status Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReadmeShieldBadge("Platform", "Android 7.0+", Color(0xFF238636))
                ReadmeShieldBadge("Kotlin", "2.2.10", Color(0xFF7F52FF))
                ReadmeShieldBadge("Jetpack Compose", "Material 3", Color(0xFF4285F4))
                ReadmeShieldBadge("PDF Toolkit", "20 Tools", Color(0xFF00B4D8))
                ReadmeShieldBadge("Display", "165Hz Fluid UI", Color(0xFFF59E0B))
                ReadmeShieldBadge("License", "MIT", Color(0xFF0969DA))
            }
        }

        HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

        // App Interface & Visual Showcase Photos inside README.md
        Text(
            text = "📸 App Interface & Visual Showcase",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = ghTextPrimary
        )

        Text(
            text = "Explore Paperflow's multi-panel Apple VisionOS Liquid Glass interface, 6 bespoke photo theme editions, native PDF annotation studio, 20 built-in PDF tools, and draggable floating AI assistant.",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = ghTextSecondary
        )

        // Hero Multi-Panel App Interface Showcase Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ghSurfaceBg)
                .border(1.dp, ghBorderColor, RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_interface_showcase_1791433886976),
                contentDescription = "Paperflow App Interface Showcase — Home Workspace, PDF Reader & Annotation Studio, Offline PDF Toolkit & Floating AI Assistant",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Paperflow Multi-Panel Interface Overview",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ghTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Left: Home Workspace & Daily Streak • Center: High-Resolution PDF Reader & Annotation Studio • Right: 20 Built-In PDF Tools & Draggable Liquid-Glass AI Assistant",
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                    color = ghTextSecondary
                )
            }
        }

        // Horizontal Scrollable Photo Gallery of Visual Editions & Interface Modules
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReadmePhotoGalleryCard(
                drawableRes = R.drawable.img_hero_liquid_glass_1791391701805,
                title = "💎 VisionOS Liquid Glass",
                caption = "Frosted translucent glass layers, dynamic cyan/violet refraction & safe-area navigation capsule.",
                ghSurfaceBg = ghSurfaceBg,
                ghBorderColor = ghBorderColor,
                ghTextPrimary = ghTextPrimary,
                ghTextSecondary = ghTextSecondary
            )

            ReadmePhotoGalleryCard(
                drawableRes = R.drawable.img_hero_galactic_codex_1791427754468,
                title = "🌌 Galactic Scholar Edition",
                caption = "Deep-space obsidian & warm gold illumination photo theme configurable in Settings.",
                ghSurfaceBg = ghSurfaceBg,
                ghBorderColor = ghBorderColor,
                ghTextPrimary = ghTextPrimary,
                ghTextSecondary = ghTextSecondary
            )

            ReadmePhotoGalleryCard(
                drawableRes = R.drawable.img_hero_desert_dune_clay_1791461302278,
                title = "🏜️ Desert Dune Edition",
                caption = "Sun-baked terracotta clay, warm sandstone & amber bronze accents.",
                ghSurfaceBg = ghSurfaceBg,
                ghBorderColor = ghBorderColor,
                ghTextPrimary = ghTextPrimary,
                ghTextSecondary = ghTextSecondary
            )

            ReadmePhotoGalleryCard(
                drawableRes = R.drawable.img_hero_enchanted_forest_codex_1791461314095,
                title = "🌲 Enchanted Forest Edition",
                caption = "Deep woodland pine, emerald moss & sunlit botanical gold illumination.",
                ghSurfaceBg = ghSurfaceBg,
                ghBorderColor = ghBorderColor,
                ghTextPrimary = ghTextPrimary,
                ghTextSecondary = ghTextSecondary
            )

            ReadmePhotoGalleryCard(
                drawableRes = R.drawable.img_pdf_3d_badge_1791391714001,
                title = "🛠️ 20 Built-In PDF Tools",
                caption = "Hardware-accelerated PdfRenderer, 19 offline utilities & Universal 100+ Language AI PDF Translator.",
                ghSurfaceBg = ghSurfaceBg,
                ghBorderColor = ghBorderColor,
                ghTextPrimary = ghTextPrimary,
                ghTextSecondary = ghTextSecondary
            )
        }

        HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

        // Overview Section
        Text(
            text = "✨ Overview & Core Capabilities",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = ghTextPrimary
        )

        Text(
            text = "Paperflow is a local-first PDF Reader, Document Studio, Universal PDF Translator, and Study Notes workspace for Android. It combines high-resolution multi-page PDF rendering with an Apple VisionOS-inspired Liquid Glass interface, a Daily Reading Streak system, and 20 built-in PDF tools.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp
            ),
            color = ghTextPrimary
        )

        val capabilities = listOf(
            "📖 Native High-Resolution PDF Reader" to "Crisp multi-page rendering, pinch-to-zoom, Light/Sepia/Dark reader filters, 5-color Annotation Studio, and automatic reading progress.",
            "🌐 Universal PDF Translator (Any → Any Language)" to "Translate any PDF between 100+ world languages or any custom dialect with Auto-Detect, 1-tap swap (⇄), Bilingual mode, and Unicode PDF export.",
            "🛠️ 19 Offline PDF Manipulation Tools" to "Merge PDFs, Split page ranges, Rearrange/Rotate/Delete/Extract pages, Compress, Repair, Grayscale, Protect/Unlock, Watermark, and Sign with Digital Ink.",
            "✨ Draggable Floating Liquid-Glass AI Assistant" to "Freeform drag with 4-edge magnetic spring snap, keyboard-aware docking, document summarization, Q&A, and 1-tap save to Study Notes.",
            "🎨 6 Visual Themes & Play Store Pull-to-Refresh" to "Light, Dark, System, Galactic Scholar, Desert Dune, and Enchanted Forest themes with transparent morphing pull-to-refresh."
        )

        capabilities.forEach { (title, desc) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ghLinkBlue
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ghTextPrimary
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = ghTextSecondary
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = ghBorderColor)

        // Run Locally Section (matching Reference Image 1)
        Text(
            text = "Run Locally",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = ghTextPrimary
        )

        Text(
            text = "Prerequisites: Android Studio Ladybug (2024.2.1+), JDK 17, Gradle 9.3.1, and Android SDK 36.",
            style = MaterialTheme.typography.bodySmall,
            color = ghTextSecondary
        )

        val cloneCmd = "git clone https://github.com/penpoyem-create/Paperflow-pdf-reader-Notes.git\ncd Paperflow-pdf-reader-Notes\ngradle :app:assembleDebug --stacktrace --no-daemon"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ghSurfaceBg)
                .border(1.dp, ghBorderColor, RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(end = 32.dp)) {
                Text(
                    text = cloneCmd,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    color = ghTextPrimary
                )
            }
            IconButton(
                onClick = { onCopyCloneCommand(cloneCmd) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = "Copy commands",
                    tint = ghTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun ReadmePhotoGalleryCard(
    drawableRes: Int,
    title: String,
    caption: String,
    ghSurfaceBg: Color,
    ghBorderColor: Color,
    ghTextPrimary: Color,
    ghTextSecondary: Color
) {
    Column(
        modifier = Modifier
            .width(248.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ghSurfaceBg)
            .border(1.dp, ghBorderColor, RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = ghTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = ghTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReadmeShieldBadge(
    leftLabel: String,
    rightValue: String,
    rightColor: Color
) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF555555))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = leftLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White
            )
        }
        Box(
            modifier = Modifier
                .background(rightColor)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = rightValue,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LanguageLegendDot(
    name: String,
    percentage: String,
    dotColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textPrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelSmall,
            color = textSecondary
        )
    }
}

@Composable
private fun NonCodeTabPreviewSection(
    tab: GitHubTopTab,
    ghSurfaceBg: Color,
    ghBorderColor: Color,
    ghTextPrimary: Color,
    ghTextSecondary: Color,
    ghLinkBlue: Color,
    onReturnToCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ghBorderColor, RoundedCornerShape(8.dp))
            .background(ghSurfaceBg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = ghLinkBlue,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "${PaperflowGitHubRepoData.REPO_NAME} — ${tab.label}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ghTextPrimary
        )
        Text(
            text = when (tab) {
                GitHubTopTab.ISSUES -> "2 open feature enhancements tracked for Paperflow v2.7 (Cloud Vault Sync & Multi-window Split Reader)."
                GitHubTopTab.PULL_REQUESTS -> "1 open pull request: feat(theme): add Galactic Scholar Cosmic Codex photo edition (#26)."
                GitHubTopTab.ACTIONS -> "All Android CI/CD workflows passing (`assembleDebug`, `testDebugUnitTest`)."
                GitHubTopTab.SECURITY -> "Zero known vulnerabilities. 100% offline-first document processing policy active."
                GitHubTopTab.INSIGHTS -> "6 recent commits across 14 Kotlin & Gradle modules by penpoyem-create."
                GitHubTopTab.CODE -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = ghTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onReturnToCode,
            colors = ButtonDefaults.buttonColors(containerColor = ghLinkBlue)
        ) {
            Text("Back to Repository Code & Files", color = Color.White)
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    } catch (_: Exception) {
    }
}
