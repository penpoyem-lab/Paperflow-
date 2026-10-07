# Paperflow — PDF Reader & Notes

**PDF Reader • Notes • Documents • Tools**

Paperflow is a modern, local-first Android application built with Kotlin, Jetpack Compose, and Material 3 featuring a **VisionOS-inspired Liquid Glass & Glassmorphism UI**. All PDF rendering, page annotations, study notes, and PDF document tools operate strictly on-device with zero cloud uploads.

---

## Features

- **Liquid Glass Interface**: Translucent frosted surfaces, iridescent light refraction, custom typography (`Plus Jakarta Sans` & `Inter`), and dedicated Light and Dark themes.
- **Floating Glass Navigation Bar**: Safe-area-aware capsule navigation bar with **Home**, **Tools**, **Library**, and **Settings** destinations.
- **PDF Library & Native SAF Picker**:
  - Import and open PDFs from Android device storage, Downloads, Documents, or cloud providers via Android's Storage Access Framework (`OpenDocument`).
  - Stores document URI references and metadata in Room (`paperflow.db`) without bloating the database with binary payloads.
  - Supports **ALL**, **PDFs**, **NOTES**, **RECENT**, and **FAVORITES** categories, live search, sorting, Grid/List view switching, rename, duplicate, share, and delete.
- **Real PDF Reader (`android.graphics.pdf.PdfRenderer`)**:
  - High-resolution vertical page scrolling, pinch-to-zoom, double-tap zoom, fit-width reset, and page thumbnail strip.
  - Auto-hiding floating glass top and bottom toolbars with Light, Sepia, and Dark reader color filters.
  - In-document search with page match navigation and clear notice when a document contains scanned pages.
- **PDF Annotation Studio**:
  - Persistent page annotations in **Yellow**, **Green**, **Blue**, **Pink**, and **Purple**: Highlight, Underline, Strikethrough, Pen, Marker, Sticky Text Notes, and Eraser.
- **Study Notes & Bookmarks**:
  - Rich study note editor with formatting tools (Bold, Italic, Underline, Bullet List, Numbered List, Highlight, Checklist) and direct PDF page linking.
  - Instant page bookmarking and dedicated Bookmarks sheet.
- **Offline PDF Tools**:
  - **Edit**: Merge PDF, Split PDF, Rotate PDF (90°/180°/270°), Rearrange PDF, Delete Pages, Extract Pages, Page Numbers, Diagonal Watermark, and Ink Signature Pad.
  - **Optimize**: Compress PDF, Repair PDF, and Grayscale conversion.
  - **Security**: Document Metadata inspector (with transparent *Coming Soon* states for hardware AES-256 Protect/Unlock).
  - **Convert**: PDF to Image (PNG export), Image to PDF, Extract Images, and PDF to Text.

---

## Architecture

- **UI Layer (`com.example.ui`)**: Jetpack Compose screens (`HomeScreen`, `LibraryScreen`, `ToolsScreen`, `PdfReaderScreen`, `NotesAndPickerScreen`, `SettingsScreen`) and reusable Liquid Glass components (`GlassComponents.kt`).
- **State Management**: `GlassPaperViewModel` exposing reactive `StateFlow` streams collected via `collectAsStateWithLifecycle()`.
- **Data Layer (`com.example.data`)**:
  - **Room Database (`paperflow.db`)**: Stores `PdfDocumentEntity` (URI/file references), `NoteEntity`, `BookmarkEntity`, and `AnnotationEntity`.
  - **Jetpack DataStore**: Persists theme, reader layout, text size, and storage preferences.
- **PDF Engine (`com.example.pdf.PdfEngine`)**: Thread-safe `Mutex`-protected wrapper around Android's native `PdfRenderer` and `PdfDocument` APIs with `LruCache` thumbnail caching.

---

## Local Build Instructions

### Prerequisites
- **JDK**: Java 17+
- **Gradle**: 9.3.1
- **Android Gradle Plugin (AGP)**: 9.1.1
- **Android SDK**: `compileSdk` 36 (`minSdk` 24)

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

## GitHub Actions Debug APK & Release Workflow

This repository includes an automated GitHub Actions workflow at `.github/workflows/build-apk.yml`:

1. **Triggers**: Runs automatically on `push` to `main` and supports manual `workflow_dispatch`.
2. **Environment**: Configures Java 17 and Gradle 9.3.1 on `ubuntu-latest`, generating a temporary `debug.keystore` only if one is not already present on the runner.
3. **Artifact Upload**: Builds `app/build/outputs/apk/debug/app-debug.apk` and uploads it as the `app-debug-apk` workflow artifact.
4. **GitHub Release Publishing**: Publishes a uniquely tagged GitHub Release (`debug-apk-build-<RUN_NUMBER>-<RUN_ATTEMPT>`) containing the directly downloadable `<repo>-debug-build-<RUN_NUMBER>.apk` asset. If release creation fails, the `app-debug-apk` Actions artifact remains preserved and downloadable.

---

## Privacy

**Your documents stay on your device.**
Paperflow is 100% local-first. Documents, annotations, bookmarks, and notes never leave your device unless you explicitly invoke the Android system Share sheet.

---

## License

Licensed under the [MIT License](LICENSE).
