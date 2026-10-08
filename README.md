# Paperflow — PDF Reader & Notes

<div align="center">

<img src="app/src/main/res/drawable/paperflow_red_black_icon_1791443745243.jpg" alt="Paperflow App Icon" width="110" style="border-radius: 24px;" />

### **PDF Reader • PDF Toolkit • Linked Study Notes • Floating AI Assistant**

*A modern, 100% local-first Android PDF Reader, Document Organizer, and Study Notes workspace crafted with Kotlin, Jetpack Compose, and an Apple VisionOS-inspired Liquid Glass design system.*

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-238636?style=for-the-badge&logo=android&logoColor=white)](#local-build-instructions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](#architecture)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#app-interface--visual-tour)
[![License: MIT](https://img.shields.io/badge/License-MIT-0969DA?style=for-the-badge)](LICENSE)

</div>

---

## 📸 App Interface & Visual Showcase

### Multi-Panel App Interface Overview
<div align="center">
  <img src="app/src/main/res/drawable/img_app_interface_showcase_1791433886976.jpg" alt="Paperflow App Interface Showcase — Home Workspace, PDF Reader & Annotation Studio, Offline PDF Toolkit & Floating AI Assistant" width="100%" />
  <p><em>Left: Home Workspace & Daily Streak • Center: High-Resolution PDF Reader & Annotation Studio • Right: 19 Offline PDF Tools & Draggable Liquid-Glass AI Assistant</em></p>
</div>

---

### Visual Editions, Themes & Brand Assets

| VisionOS Liquid Glass Hero | Galactic Scholar (Cosmic Codex) Edition | 3D Crystalline PDF Engine Emblem |
| :---: | :---: | :---: |
| <img src="app/src/main/res/drawable/img_hero_liquid_glass_1791391701805.jpg" alt="VisionOS Liquid Glass Hero Banner" width="320" /> | <img src="app/src/main/res/drawable/img_hero_galactic_codex_1791427754468.jpg" alt="Galactic Scholar Cosmic Codex Photo Theme" width="320" /> | <img src="app/src/main/res/drawable/img_pdf_3d_badge_1791391714001.jpg" alt="3D Crystalline PDF Badge" width="210" /> |
| **Default Liquid Glass Workspace**<br/>Frosted translucent layers, specular highlights, and dynamic cyan/indigo refraction. | **Galactic Scholar Photo Theme**<br/>Deep-space midnight obsidian & warm gold illumination configurable in **Settings**. | **Native On-Device PDF Engine**<br/>Hardware-accelerated `PdfRenderer` & `PdfDocument` processing with zero cloud uploads. |

---

## 🖥️ Interactive Interface Breakdown

Paperflow is organized around a **Safe-Area Floating Liquid Glass Navigation Bar** and full-screen interactive workspaces:

### 1. 🏠 Home Workspace & Daily Reading Streak (`HomeScreen.kt`)
- **Hero Banner & Quick Actions**: Instant 1-tap access to **Open PDF** (Android Storage Access Framework), **Merge PDF**, **Split PDF**, **Compress**, and **New Study Note**.
- **Snapchat-Style Streak Pill (`🔥 + count`)**: Located in the top header bar; tapping opens the full **Daily Reading Streak** celebration modal and 7-day check-in calendar strip.
- **Continue Reading Carousel**: Displays your most recently opened PDF documents with live reading progress bars, page counters, and last-read timestamps.
- **Recent Study Notes**: Quick preview cards linked directly to specific PDF documents and page numbers.

### 2. 📖 Native PDF Reader & Annotation Studio (`PdfReaderScreen.kt`)
- **Crisp Multi-Page Rendering**: Powered by Android's native `android.graphics.pdf.PdfRenderer` with `Mutex`-synchronized bitmap rendering and `LruCache` page thumbnail caching.
- **Fluid Gestures**: Pinch-to-zoom (`1.0x` to `4.0x`), double-tap zoom toggle, vertical/horizontal page scrolling, fit-to-width reset, and interactive bottom thumbnail scrubber.
- **Reader Color Modes**: Switch seamlessly between **Light**, **Sepia (Warm Paper)**, and **Dark (AMOLED Night)** reading filters.
- **Persistent Annotation Studio**: Draw and save **Highlight**, **Underline**, **Strikethrough**, **Pen**, **Marker**, **Sticky Text Notes**, and **Eraser** strokes in **Yellow**, **Green**, **Blue**, **Pink**, and **Purple**.

### 3. 🛠️ 19 Offline PDF Tools (`ToolsScreen.kt` & `PdfEngine.kt`)
All 19 utilities execute 100% locally on your Android device and save generated files directly to local storage and your Paperflow Library:

| Category | Included Tools |
| :--- | :--- |
| **Edit & Arrange** | **Merge PDF**, **Split PDF**, **Rotate PDF** (90°/180°/270°), **Rearrange Pages**, **Delete Pages**, **Extract Pages**, **Page Numbers**, **Diagonal Watermark**, **Digital Ink Signature Pad** |
| **Optimize** | **Compress PDF**, **Repair PDF**, **Grayscale PDF** |
| **Security & Metadata** | **Protect PDF** (Password Lock), **Unlock PDF**, **Document Metadata Inspector** |
| **Convert & Extract** | **PDF to Image (PNG)**, **Image to PDF**, **Extract Embedded Images**, **PDF to Text** |

### 4. 📚 Unified PDF & Notes Library (`LibraryScreen.kt`)
- **Category Filters**: Filter by **ALL**, **PDFs**, **NOTES**, **RECENT**, and **FAVORITES**.
- **Search, Sort & View Modes**: Live search bar, sort by Name / Date / Size, and toggle between **Grid View** and **Detailed List View**.
- **Document Actions**: Favorite star toggle, Rename, Duplicate, Share via Android system sheet, Export, or Delete.

### 5. ✨ Draggable Floating Liquid-Glass AI Assistant (`FloatingAiChatbot.kt`)
- **Bespoke Animated Prism Star Logo**: Features counter-rotating cyan, electric-blue, and violet orbital rings with a breathing crystalline star core.
- **Smooth Drag & 4-Edge Magnetic Snap**: Drag the floating orb anywhere on screen with safe-area inset clamping and spring physics that snap cleanly to the nearest screen edge (**Left**, **Right**, **Top**, or **Bottom**).
- **Document-Aware Intelligence**: Summarizes your currently open PDF, explains complex sections, extracts key takeaways, translates passages, and saves AI responses directly as **Study Notes**.

### 6. 💻 Built-in GitHub Repository Explorer (`GitHubRepositoryScreen.kt`)
- Accessible from **Home** and **Settings**, featuring branch/tag switching (`main`, `feat/liquid-glass-v2.6`), commit history viewer, interactive file tree browser, and a tabbed **README / MIT License** preview with live App Interface gallery cards.

---

## 🏗️ Architecture & Tech Stack

```text
Paperflow-pdf-reader-Notes/
├── .github/workflows/
│   └── build-apk.yml                          # Automated Android CI/CD Debug APK & GitHub Release pipeline
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt                # 165Hz frame-paced window host, Flash Intro & Floating AI overlay
│   │   │   ├── ai/
│   │   │   │   └── PaperflowAiService.kt      # Gemini REST API & local PDF context engine
│   │   │   ├── auth/
│   │   │   │   └── PaperflowAuthManager.kt    # Local session & profile management
│   │   │   ├── data/
│   │   │   │   ├── Entities.kt                # Room entities (PDFs, Notes, Bookmarks, Annotations, AI Chat)
│   │   │   │   ├── GlassPaperDatabase.kt      # Room SQLite database (paperflow.db)
│   │   │   │   ├── GlassPaperRepository.kt    # Single source of truth repository
│   │   │   │   ├── PaperflowGitHubRepoData.kt # Repository explorer metadata & file tree
│   │   │   │   └── SettingsDataStore.kt       # Jetpack DataStore preferences (Theme, Streak, AI Orb position)
│   │   │   ├── pdf/
│   │   │   │   └── PdfEngine.kt               # Thread-safe native PdfRenderer & 19 PDF manipulation tools
│   │   │   └── ui/
│   │   │       ├── GlassPaperViewModel.kt     # Reactive MVVM StateFlows
│   │   │       ├── components/                # Liquid Glass cards, organic loader, Flash Intro & AI Chatbot
│   │   │       ├── screens/                   # Home, Library, Tools, PdfReader, Notes, Streak, GitHub, Settings
│   │   │       └── theme/                     # VisionOS Liquid Glass & Galactic Scholar color/typography system
│   │   ├── res/
│   │   │   ├── drawable/                      # App interface showcase banners, Galactic Codex art & icons
│   │   │   ├── font/                          # Bundled Plus Jakarta Sans & Inter TTF fonts
│   │   │   └── values/                        # Strings & adaptive themes
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml                  # Centralized dependency version catalog
├── LICENSE                                    # MIT License
├── README.md                                  # Project documentation & visual interface guide
└── settings.gradle.kts
```

---

## 🚀 Local Build Instructions

### Prerequisites
- **JDK**: Java 17+
- **Gradle**: 9.3.1
- **Android Gradle Plugin (AGP)**: 9.1.1
- **Android SDK**: `compileSdk` 36 (`minSdk` 24, `targetSdk` 35)

### Build Debug APK
From the repository root, run:

```bash
gradle :app:assembleDebug --stacktrace --no-daemon
```

The generated Debug APK will be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ GitHub Actions Debug APK & Release Workflow

This repository includes an automated GitHub Actions workflow at `.github/workflows/build-apk.yml`:

1. **Triggers**: Runs automatically on `push` to `main` and supports manual `workflow_dispatch`.
2. **Environment**: Configures Java 17 and Gradle 9.3.1 on `ubuntu-latest`, generating a temporary `debug.keystore` only if one is not already present on the runner.
3. **Artifact Upload**: Builds `app/build/outputs/apk/debug/app-debug.apk` and uploads it as the `app-debug-apk` workflow artifact.
4. **GitHub Release Publishing**: Publishes a uniquely tagged GitHub Release (`debug-apk-build-<RUN_NUMBER>-<RUN_ATTEMPT>`) containing the directly downloadable `<repo>-debug-build-<RUN_NUMBER>.apk` asset.

---

## 🔒 Privacy

**Your documents stay on your device.**  
Paperflow is local-first. All PDF rendering, page modifications, annotations, bookmarks, and study notes are stored on-device and never leave your phone unless you explicitly invoke the Android system Share sheet.

---

## 📄 License

Licensed under the [MIT License](LICENSE).
