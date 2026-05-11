# JellyPlex: Project Progress Tracking

## Status Overview

- **Project Name:** JellyPlex (Jellyfin Client with Plex-style UI)
- **Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Ktor, Media3
- **Latest Version:** 1.0.0-alpha
- **Last Updated:** May 9, 2026

---

## 🟢 Phase 1: Foundation & Authentication (100% Completed)

- [x] **Project Scaffolding:** CMP structure for Android, iOS, Web, and Desktop.
- [x] **API Integration:** Ktor client setup with JSON serialization and logging.
- [x] **Traditional Login:** Manual URL, Username, and Password authentication.
- [x] **QuickConnect:** PIN-based authentication for TV and effortless mobile login.
- [x] **Server Discovery:**
    - **Android/Desktop:** Real-time UDP Broadcast (Port 7359) implementation.
    - **Web:** HTTP-based local scanning fallback.
    - **Manual:** Direct IP/URL input support.
- [x] **Session Management:** Persistent storage for authentication tokens using `multiplatform-settings`.

## 🟢 Phase 2: Browsing & UI - "The Plex Experience" (100% Completed)

- [x] **Sidebar Navigation:** Dynamic sidebar with D-pad/Focus support for TV.
- [x] **Home Layout:** "Continue Watching" and "Recently Added" horizontal rows.
- [x] **Media Components:** Plex-style posters with focus-scaling and glowing borders.
- [x] **Detail Screen:** Immersive backdrop, rich metadata display, and prominent Play button.
- [x] **Global Search:** Adaptive grid results with instant search UI.

## 🟢 Phase 3: Playback & Advanced Features (100% Completed)

- [x] **Video Player Engine:**
    - **Android:** Integrated Media3 (ExoPlayer) for native performance.
    - **Web:** Actual/Expect wrapper with placeholder UI.
- [x] **Custom Player Controls:** Auto-hiding overlay, yellow progress bar, and media info.
- [x] **Skip Intro:** UI and logic integration for skipping media segments.
- [x] **SyncPlay:** Status indicators for synchronized playback sessions.

---

## 🛠 Engineering Standards Applied

- **Clean Architecture:** Separation of Concerns (Data, Domain, UI).
- **MVI Pattern:** State-driven UI for predictable focus management on TV.
- **S.O.L.I.D:** Interface-based repositories and platform-specific implementations.
- **UI Logic Separation:** `expect/actual` platform detection to provide tailored experiences for Mobile vs TV.
- **Code Quality:** Automated Ktlint checks enforced across all platforms.

## 🚀 Next Steps (Future Phases)

- [ ] Real Media3 testing on physical Android TV hardware.
- [ ] Implement actual file system downloading for Offline Manager.
- [ ] PiP (Picture-in-Picture) support for Android.
