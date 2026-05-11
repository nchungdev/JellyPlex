# JellyPlex: Technical Design Document

## 1. Project Overview

JellyPlex is a modern, cross-platform media client for Jellyfin, designed to provide a "Plex-like" user experience. It
targets Android (Mobile & TV), iOS, macOS, and Web using a single codebase.

## 2. Technical Stack

- **Language:** Kotlin
- **Framework:** Compose Multiplatform (CMP)
- **UI Libraries:**
    - Mobile/Desktop/Web: `androidx.compose.material3`
    - Android TV: `androidx.tv.material3` (Compose for TV)
- **Networking:** Ktor Client (Multiplatform)
- **Image Loading:** Coil3 (Multiplatform)
- **Local Database:** Room (Multiplatform)
- **Concurrency:** Kotlin Coroutines & Flow
- **Dependency Injection:** Koin (Multiplatform)
- **Playback Engine:**
    - Android: Media3 (ExoPlayer)
    - iOS/macOS/Web: CMP-native player or VLC/MPV bindings.

## 3. Architecture

- **Pattern:** Clean Architecture + MVI (Model-View-Intent).
- **Modules:**
    - `shared-core`: API definitions, Models, UseCases.
    - `shared-ui`: Shared Compose components, Styles, Icons.
    - `android-app`: Android-specific entry point and TV optimizations.
    - `ios-app`: iOS entry point.
    - `desktop-app`: macOS/Desktop entry point.
    - `web-app`: Wasm/JS entry point.

## 4. Core Features (Phase 1)

### 4.1. Authentication

- **Server Discovery:** Manual URL input + Discovery (SSDP/mDNS).
- **Login Methods:**
    - Username / Password.
    - **QuickConnect:** Generate PIN on TV -> Authorize on Mobile (Plex style).
- **Session Management:** Secure token storage using Multiplatform Settings (Encrypted).

### 4.2. Library & Browsing

- **Plex-style UI:**
    - Sidebar navigation for libraries (Movies, TV Shows, Music).
    - Hero sections with large backdrops in Detail views.
    - Poster grids with scale/zoom effects on focus (for TV).
    - "Continue Watching" and "Recently Added" horizontal lists on Home.
- **Metadata:** Rich display of cast, crew, ratings, and technical specs (4K, HDR, Atmos).

### 4.3. Search

- Global search across all libraries.
- Voice Search integration for Android TV.
- Instant results as you type.

### 4.4. Playback

- Custom Video Player UI (Plex-inspired).
- Subtitle & Audio track selection.
- Playback speed control.
- **Direct Play / Transcoding** status indicator.

## 5. Special Features (Phase 2 - "The Plex Magic")

- **Skip Intro / Skip Credits:** Integration with Jellyfin Intro Skipper plugin.
- **SyncPlay:** Real-time synchronized playback with other users.
- **Offline Downloads:** Download media for offline viewing (Mobile).
- **Trailers & Extras:** Display trailers and bonus content.
- **Smart Collections:** Grouping by franchises/genres with custom banners.
- **Picture-in-Picture (PiP):** Multitasking support.
- **Live TV & DVR:** Grid guide and recording management.

## 6. Focus Management (Android TV)

- Strategic use of `Modifier.focusable()` and `FocusRequester`.
- Custom `Indication` for visual focus feedback.
- Keypad/D-pad navigation optimization (Circular focus, focus restoration).

## 7. Implementation Roadmap

1. **Foundation:** Setup KMP project structure and Ktor client.
2. **Auth:** Implement Login & QuickConnect logic.
3. **Home UI:** Create the main layout and library browsing logic.
4. **Detail UI:** Rich media details and backdrop management.
5. **Player:** Media3 integration and basic playback controls.
6. **Polish:** Focus effects for TV and Plex-style animations.
