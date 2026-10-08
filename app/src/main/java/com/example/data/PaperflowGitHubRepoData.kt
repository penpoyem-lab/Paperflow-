package com.example.data

enum class RepoNodeType {
    DIRECTORY,
    FILE
}

data class RepoFileNode(
    val name: String,
    val path: String,
    val type: RepoNodeType,
    val commitMessage: String,
    val lastUpdatedRelative: String,
    val languageOrExt: String = "",
    val sizeLabel: String = "",
    val content: String = "",
    val children: List<RepoFileNode> = emptyList()
)

data class RepoCommitEntry(
    val hash: String,
    val author: String,
    val message: String,
    val relativeDate: String,
    val verified: Boolean = true,
    val additions: Int = 142,
    val deletions: Int = 18
)

object PaperflowGitHubRepoData {

    const val OWNER_USERNAME = "penpoyem-create"
    const val REPO_NAME = "Paperflow-pdf-reader-Notes"
    const val VISIBILITY_BADGE = "Public"
    const val REPO_URL = "https://github.com/penpoyem-create/Paperflow-pdf-reader-Notes"
    const val DEFAULT_BRANCH = "main"

    val branches: List<String> = listOf(
        "main",
        "feat/liquid-glass-v2.6",
        "feat/galactic-scholar-theme",
        "release/v2.6.0-android"
    )

    val tags: List<String> = listOf(
        "v2.6.0",
        "v2.5.2",
        "v2.4.0",
        "v2.0.0-glass"
    )

    const val ABOUT_DESCRIPTION =
        "Paperflow — Modern PDF Reader, PDF Organizer, Study Notes & Local Document Utilities for Android with an Apple VisionOS-inspired Liquid Glass interface."

    val topics: List<String> = listOf(
        "android",
        "kotlin",
        "jetpack-compose",
        "pdf-reader",
        "pdf-tools",
        "notes-app",
        "liquid-glass",
        "material3",
        "offline-first"
    )

    val languageBreakdown: List<Pair<String, Float>> = listOf(
        "Kotlin" to 94.2f,
        "XML" to 3.8f,
        "Gradle Kotlin DSL" to 2.0f
    )

    val commitHistory: List<RepoCommitEntry> = listOf(
        RepoCommitEntry(
            hash = "b41ca07",
            author = "penpoyem-create",
            message = "feat: add Galactic Scholar photo theme, VisionOS flash intro & GitHub repository explorer",
            relativeDate = "now",
            verified = true,
            additions = 864,
            deletions = 42
        ),
        RepoCommitEntry(
            hash = "9e72d14",
            author = "penpoyem-create",
            message = "perf(ui): enable 165Hz frame-paced Liquid Glass navigation & Play Store blob loader",
            relativeDate = "2 hours ago",
            verified = true,
            additions = 512,
            deletions = 76
        ),
        RepoCommitEntry(
            hash = "6a09f8c",
            author = "penpoyem-create",
            message = "feat(auth): implement Paperflow Liquid Glass authentication & session security",
            relativeDate = "yesterday",
            verified = true,
            additions = 740,
            deletions = 19
        ),
        RepoCommitEntry(
            hash = "3d81e22",
            author = "penpoyem-create",
            message = "feat(streak): add local Daily Reading Streak system with animated celebration modal",
            relativeDate = "2 days ago",
            verified = true,
            additions = 618,
            deletions = 14
        ),
        RepoCommitEntry(
            hash = "1f54a90",
            author = "penpoyem-create",
            message = "feat(pdf): add Merge, Split, Rearrange, Compress, Watermark, Sign & OCR PDF engine",
            relativeDate = "4 days ago",
            verified = true,
            additions = 1420,
            deletions = 88
        ),
        RepoCommitEntry(
            hash = "8c20b11",
            author = "penpoyem-create",
            message = "chore: initial Paperflow Android Studio Gradle Kotlin DSL & Room architecture",
            relativeDate = "last week",
            verified = true,
            additions = 2190,
            deletions = 0
        )
    )

    val readmeMarkdownContent: String = """
# Paperflow — PDF Reader, Organizer & Study Notes

<div align="center">
  <img src="app/src/main/res/drawable/paperflow_red_black_icon_1791443745243.jpg" alt="Paperflow App Icon" width="100" />
  <h3>PDF Reader • PDF Toolkit • Linked Study Notes • Floating AI Assistant</h3>
  <p>A modern, 100% local-first Android PDF Reader, PDF Toolkit, and Study Notes workspace crafted with Kotlin, Jetpack Compose, and an Apple VisionOS-inspired Liquid Glass design system.</p>
</div>

---

## 📸 App Interface & Visual Showcase

<div align="center">
  <img src="app/src/main/res/drawable/img_app_interface_showcase_1791433886976.jpg" alt="Paperflow App Interface Showcase" width="100%" />
  <p><em>Left: Home Workspace & Daily Streak • Center: High-Resolution PDF Reader & Annotation Studio • Right: 19 Offline PDF Tools & Draggable Liquid-Glass AI Assistant</em></p>
</div>

| VisionOS Liquid Glass Hero | Galactic Scholar (Cosmic Codex) Edition | 3D Crystalline PDF Engine Emblem |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/img_hero_liquid_glass_1791391701805.jpg" width="280" /> | <img src="app/src/main/res/drawable/img_hero_galactic_codex_1791427754468.jpg" width="280" /> | <img src="app/src/main/res/drawable/img_pdf_3d_badge_1791391714001.jpg" width="180" /> |

---

## 🖥️ Interactive App Interface Breakdown

1. **🏠 Home Workspace & Daily Reading Streak (`HomeScreen.kt`)**:
   - Quick-action cards for **Open PDF**, **Merge**, **Split**, **Compress**, and **New Study Note**, plus the **🔥 Daily Streak** pill and **Continue Reading** carousel.
2. **📖 Native High-Resolution PDF Reader (`PdfReaderScreen.kt`)**:
   - Crisp multi-page rendering powered by Android's `PdfRenderer`, pinch-to-zoom, page thumbnails, Light / Sepia / Dark reader modes, and 5-color **Annotation Studio**.
3. **🛠️ Complete Offline PDF Toolkit (`ToolsScreen.kt` & `PdfEngine.kt`)**:
   - **19 Active Utilities**: Merge, Split, Rotate, Rearrange, Delete/Extract Pages, Page Numbers, Watermark, Digital Ink Signature, Compress, Repair, Grayscale, Protect/Unlock PDF, Metadata Inspector, PDF to Image/Text, and Image to PDF.
4. **✨ Draggable Floating Liquid-Glass AI Assistant (`FloatingAiChatbot.kt`)**:
   - Smooth freeform drag with 4-edge magnetic spring snap, animated Prism Star logo, and PDF-aware summarization, Q&A, translation, and 1-tap save to Study Notes.
5. **🎨 Visual Editions & Photo Themes (`SettingsScreen.kt`)**:
   - Switch between **Light**, **Dark**, **System**, and the **Galactic Scholar (Cosmic Codex)** photo edition.

---

## 🏗️ Project Structure

```text
Paperflow-pdf-reader-Notes/
├── .github/workflows/       # Android CI/CD & release verification pipelines
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── ai/          # PaperflowAiService & PDF context engine
│   │   │   ├── auth/        # PaperflowAuthManager & session persistence
│   │   │   ├── data/        # Room Database, Entities, DAO & SettingsDataStore
│   │   │   ├── pdf/         # Native PdfEngine (Merge, Split, Compress, Sign, Watermark)
│   │   │   └── ui/          # Jetpack Compose screens, Liquid Glass components & Theme
│   │   ├── res/             # App interface showcase photos, adaptive icons & fonts
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts     # App module dependencies & Compose configuration
├── gradle/                  # Version catalog (libs.versions.toml) & wrapper config
├── .gitignore
├── LICENSE                  # MIT License
├── README.md                # Project documentation & app interface gallery
├── build.gradle.kts         # Root Gradle configuration
└── settings.gradle.kts      # Project settings (rootProject.name = "Paperflow")
```

---

## 🚀 Run Locally

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17+**
- **Gradle 9.3.1 & AGP 9.1.1**
- **Android SDK 36** (`minSdk` 24)

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/penpoyem-create/Paperflow-pdf-reader-Notes.git
   cd Paperflow-pdf-reader-Notes
   ```
2. **Build the debug APK:**
   ```bash
   gradle :app:assembleDebug --stacktrace --no-daemon
   ```
3. **Run unit & Robolectric tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🔒 Privacy & Local-First Architecture

Paperflow processes **100% of your PDFs, annotations, signatures, and study notes locally on your Android device**. No documents are uploaded to external cloud servers.
""".trimIndent()

    val licenseContent: String = """
MIT License

Copyright (c) 2026 penpoyem-create (Paperflow — PDF Reader & Notes)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
""".trimIndent()

    val rootFiles: List<RepoFileNode> = listOf(
        RepoFileNode(
            name = ".github",
            path = ".github",
            type = RepoNodeType.DIRECTORY,
            commitMessage = "ci: add Android Gradle build & lint verification workflow",
            lastUpdatedRelative = "3 days ago",
            children = listOf(
                RepoFileNode(
                    name = "workflows",
                    path = ".github/workflows",
                    type = RepoNodeType.DIRECTORY,
                    commitMessage = "ci: configure JDK 17 & Android SDK caching",
                    lastUpdatedRelative = "3 days ago",
                    children = listOf(
                        RepoFileNode(
                            name = "build-apk.yml",
                            path = ".github/workflows/build-apk.yml",
                            type = RepoNodeType.FILE,
                            commitMessage = "ci: build and publish Debug APK artifact & GitHub Release",
                            lastUpdatedRelative = "now",
                            languageOrExt = "YAML",
                            sizeLabel = "7.85 KB",
                            content = """
name: Build and Publish Debug APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build-debug-apk:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Set up Gradle 9.3.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'
      - name: Build Debug APK
        run: gradle :app:assembleDebug --stacktrace --no-daemon
""".trimIndent()
                        ),
                        RepoFileNode(
                            name = "release-apk.yml",
                            path = ".github/workflows/release-apk.yml",
                            type = RepoNodeType.FILE,
                            commitMessage = "ci(release): automate signed APK artifact upload on tag",
                            lastUpdatedRelative = "5 days ago",
                            languageOrExt = "YAML",
                            sizeLabel = "1.18 KB",
                            content = """
name: Paperflow Release APK

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Assemble Release
        run: ./gradlew :app:assembleRelease
""".trimIndent()
                        )
                    )
                ),
                RepoFileNode(
                    name = "FUNDING.yml",
                    path = ".github/FUNDING.yml",
                    type = RepoNodeType.FILE,
                    commitMessage = "docs: add sponsor links for Paperflow open-source development",
                    lastUpdatedRelative = "last week",
                    languageOrExt = "YAML",
                    sizeLabel = "184 B",
                    content = "github: [penpoyem-create]\n"
                )
            )
        ),
        RepoFileNode(
            name = "app",
            path = "app",
            type = RepoNodeType.DIRECTORY,
            commitMessage = "feat: add Galactic Scholar photo theme, VisionOS flash intro & GitHub repository explorer",
            lastUpdatedRelative = "now",
            children = listOf(
                RepoFileNode(
                    name = "src/main/java/com/example",
                    path = "app/src/main/java/com/example",
                    type = RepoNodeType.DIRECTORY,
                    commitMessage = "feat: integrate GitHubRepositoryScreen & theme editions",
                    lastUpdatedRelative = "now",
                    children = listOf(
                        RepoFileNode(
                            name = "MainActivity.kt",
                            path = "app/src/main/java/com/example/MainActivity.kt",
                            type = RepoNodeType.FILE,
                            commitMessage = "feat: wire 165Hz window pipeline, Flash Intro & overlays",
                            lastUpdatedRelative = "now",
                            languageOrExt = "Kotlin",
                            sizeLabel = "28.6 KB",
                            content = """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.GlassPaperViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Launches Paperflow Liquid Glass & Galactic Codex UI
        }
    }
}
""".trimIndent()
                        ),
                        RepoFileNode(
                            name = "pdf/PdfEngine.kt",
                            path = "app/src/main/java/com/example/pdf/PdfEngine.kt",
                            type = RepoNodeType.FILE,
                            commitMessage = "feat(pdf): implement native Merge, Split, Rearrange, Compress & Sign",
                            lastUpdatedRelative = "4 days ago",
                            languageOrExt = "Kotlin",
                            sizeLabel = "34.2 KB",
                            content = """
package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer

/**
 * 100% local Android PdfRenderer & PdfDocument processing engine.
 * Supports Merge, Split, Page Reordering, Rotation, Watermarking, Digital Ink Signing & Compression.
 */
object PdfEngine {
    // High-resolution bitmap rendering & document mutation utilities
}
""".trimIndent()
                        ),
                        RepoFileNode(
                            name = "ui/GlassPaperViewModel.kt",
                            path = "app/src/main/java/com/example/ui/GlassPaperViewModel.kt",
                            type = RepoNodeType.FILE,
                            commitMessage = "feat(vm): state management for PDFs, notes, streaks & GitHub repo view",
                            lastUpdatedRelative = "now",
                            languageOrExt = "Kotlin",
                            sizeLabel = "33.9 KB",
                            content = """
package com.example.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class GlassPaperViewModel : ViewModel() {
    // Reactive StateFlows for documents, notes, bookmarks, streakData, and settings
}
""".trimIndent()
                        ),
                        RepoFileNode(
                            name = "ui/screens/HomeScreen.kt",
                            path = "app/src/main/java/com/example/ui/screens/HomeScreen.kt",
                            type = RepoNodeType.FILE,
                            commitMessage = "feat(home): add Galactic Scholar codex header & quick actions",
                            lastUpdatedRelative = "now",
                            languageOrExt = "Kotlin",
                            sizeLabel = "59.4 KB",
                            content = """
package com.example.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun HomeScreen() {
    // Paperflow Home Workspace with Continue Reading, Quick Actions & Daily Streak Card
}
""".trimIndent()
                        ),
                        RepoFileNode(
                            name = "ui/screens/GitHubRepositoryScreen.kt",
                            path = "app/src/main/java/com/example/ui/screens/GitHubRepositoryScreen.kt",
                            type = RepoNodeType.FILE,
                            commitMessage = "feat(repo): build complete GitHub repository explorer page",
                            lastUpdatedRelative = "now",
                            languageOrExt = "Kotlin",
                            sizeLabel = "42.8 KB",
                            content = """
package com.example.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun GitHubRepositoryScreen() {
    // Full mobile-responsive GitHub repository browser with file tree, commits & README/LICENSE tabs
}
""".trimIndent()
                        )
                    )
                ),
                RepoFileNode(
                    name = "src/main/AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    type = RepoNodeType.FILE,
                    commitMessage = "chore(manifest): configure FileProvider & adaptive launcher icon",
                    lastUpdatedRelative = "last week",
                    languageOrExt = "XML",
                    sizeLabel = "1.85 KB",
                    content = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""".trimIndent()
                ),
                RepoFileNode(
                    name = "build.gradle.kts",
                    path = "app/build.gradle.kts",
                    type = RepoNodeType.FILE,
                    commitMessage = "build(deps): configure Jetpack Compose, Room KSP & DataStore",
                    lastUpdatedRelative = "5 days ago",
                    languageOrExt = "Kotlin DSL",
                    sizeLabel = "2.94 KB",
                    content = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aistudio.glasspaper.vzxkqp"
        minSdk = 24
        targetSdk = 35
        versionCode = 26
        versionName = "2.6.0"
    }
}
""".trimIndent()
                )
            )
        ),
        RepoFileNode(
            name = "fastlane/metadata/android/en-US",
            path = "fastlane/metadata/android/en-US",
            type = RepoNodeType.DIRECTORY,
            commitMessage = "docs(store): update changelog and descriptions for v2.6.0",
            lastUpdatedRelative = "2 days ago",
            children = listOf(
                RepoFileNode(
                    name = "title.txt",
                    path = "fastlane/metadata/android/en-US/title.txt",
                    type = RepoNodeType.FILE,
                    commitMessage = "docs: set store title to Paperflow PDF Reader & Notes",
                    lastUpdatedRelative = "2 days ago",
                    languageOrExt = "Text",
                    sizeLabel = "29 B",
                    content = "Paperflow — PDF Reader & Notes"
                ),
                RepoFileNode(
                    name = "short_description.txt",
                    path = "fastlane/metadata/android/en-US/short_description.txt",
                    type = RepoNodeType.FILE,
                    commitMessage = "docs: update short description for v2.6.0",
                    lastUpdatedRelative = "2 days ago",
                    languageOrExt = "Text",
                    sizeLabel = "78 B",
                    content = "Offline-first Liquid Glass PDF Reader, PDF Organizer, Annotator & Study Notes."
                ),
                RepoFileNode(
                    name = "changelogs/26.txt",
                    path = "fastlane/metadata/android/en-US/changelogs/26.txt",
                    type = RepoNodeType.FILE,
                    commitMessage = "docs(changelog): add release notes for build 26 (v2.6.0)",
                    lastUpdatedRelative = "now",
                    languageOrExt = "Text",
                    sizeLabel = "340 B",
                    content = """
• Added Galactic Scholar (Cosmic Codex) photo theme edition in Settings
• Added 3.0s Apple VisionOS-inspired Liquid Glass Flash Intro
• Added integrated GitHub Repository Explorer page
• Improved 165Hz display smoothness and PDF page rearrangement
""".trimIndent()
                )
            )
        ),
        RepoFileNode(
            name = "gradle",
            path = "gradle",
            type = RepoNodeType.DIRECTORY,
            commitMessage = "chore(gradle): update libs.versions.toml catalog",
            lastUpdatedRelative = "last week",
            children = listOf(
                RepoFileNode(
                    name = "libs.versions.toml",
                    path = "gradle/libs.versions.toml",
                    type = RepoNodeType.FILE,
                    commitMessage = "chore(deps): align AGP 9.1.1, Kotlin 2.2.10, Room 2.7.0 & DataStore 1.1.7",
                    lastUpdatedRelative = "last week",
                    languageOrExt = "TOML",
                    sizeLabel = "6.98 KB",
                    content = """
[versions]
agp = "9.1.1"
kotlin = "2.2.10"
composeBom = "2024.09.00"
roomRuntime = "2.7.0"
datastorePreferences = "1.1.7"
""".trimIndent()
                ),
                RepoFileNode(
                    name = "wrapper/gradle-wrapper.properties",
                    path = "gradle/wrapper/gradle-wrapper.properties",
                    type = RepoNodeType.FILE,
                    commitMessage = "chore: configure Gradle 8.10.2 wrapper distribution",
                    lastUpdatedRelative = "last week",
                    languageOrExt = "Properties",
                    sizeLabel = "242 B",
                    content = """
distributionBase=GRADLE_USER_HOME
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
""".trimIndent()
                )
            )
        ),
        RepoFileNode(
            name = ".gitignore",
            path = ".gitignore",
            type = RepoNodeType.FILE,
            commitMessage = "chore: ignore build outputs, .gradle cache, and local keystore files",
            lastUpdatedRelative = "last week",
            languageOrExt = "GitIgnore",
            sizeLabel = "412 B",
            content = """
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
""".trimIndent()
        ),
        RepoFileNode(
            name = "LICENSE",
            path = "LICENSE",
            type = RepoNodeType.FILE,
            commitMessage = "docs: add MIT open-source license",
            lastUpdatedRelative = "last week",
            languageOrExt = "License",
            sizeLabel = "1.07 KB",
            content = licenseContent
        ),
        RepoFileNode(
            name = "README.md",
            path = "README.md",
            type = RepoNodeType.FILE,
            commitMessage = "docs(readme): document Paperflow architecture, PDF toolkit & Galactic theme",
            lastUpdatedRelative = "2 hours ago",
            languageOrExt = "Markdown",
            sizeLabel = "4.85 KB",
            content = readmeMarkdownContent
        ),
        RepoFileNode(
            name = "build.gradle.kts",
            path = "build.gradle.kts",
            type = RepoNodeType.FILE,
            commitMessage = "build: configure root Android application and KSP plugins",
            lastUpdatedRelative = "last week",
            languageOrExt = "Kotlin DSL",
            sizeLabel = "318 B",
            content = """
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
""".trimIndent()
        ),
        RepoFileNode(
            name = "gradle.properties",
            path = "gradle.properties",
            type = RepoNodeType.FILE,
            commitMessage = "perf(gradle): enable non-transitive R classes, parallel execution & configuration cache",
            lastUpdatedRelative = "5 days ago",
            languageOrExt = "Properties",
            sizeLabel = "624 B",
            content = """
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
""".trimIndent()
        ),
        RepoFileNode(
            name = "metadata.json",
            path = "metadata.json",
            type = RepoNodeType.FILE,
            commitMessage = "chore(meta): sync Paperflow app title and description",
            lastUpdatedRelative = "yesterday",
            languageOrExt = "JSON",
            sizeLabel = "295 B",
            content = """
{
  "name": "Paperflow",
  "description": "Modern Liquid Glass PDF Reader, PDF Toolkit & Study Notes for Android"
}
""".trimIndent()
        ),
        RepoFileNode(
            name = "settings.gradle.kts",
            path = "settings.gradle.kts",
            type = RepoNodeType.FILE,
            commitMessage = "chore: set rootProject.name to Paperflow",
            lastUpdatedRelative = "last week",
            languageOrExt = "Kotlin DSL",
            sizeLabel = "512 B",
            content = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Paperflow"
include(":app")
""".trimIndent()
        )
    )

    fun flattenAllFiles(nodes: List<RepoFileNode> = rootFiles): List<RepoFileNode> {
        val result = mutableListOf<RepoFileNode>()
        for (node in nodes) {
            result.add(node)
            if (node.children.isNotEmpty()) {
                result.addAll(flattenAllFiles(node.children))
            }
        }
        return result
    }
}
