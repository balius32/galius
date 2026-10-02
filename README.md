# Gallius

Gallius is a private media vault for Android. Import photos and videos from the device gallery into an app-owned vault, browse them in a dark media catalog UI, organize with categories and tags, and play videos in a dedicated player.

## What it does

- Import photos and videos into a private vault (moves media out of the public gallery flow)
- Browse the library on Home (photos + videos, optional videos-only filter)
- Search and filter by category / tag
- Assign colored tags and manage taxonomy (categories + tags)
- Full-screen media viewer with zoom for photos and details in a bottom sheet
- Dedicated video player with play/pause, seek gestures, and scrubbing

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Feature-based Clean Architecture + MVI |
| Navigation | AndroidX Navigation 3 |
| DI | Koin |
| Local DB | Room (+ KSP) |
| Images | Coil 3 |
| Video | Media3 ExoPlayer |
| Serialization | Kotlinx Serialization |
| Build | Android Gradle Plugin, Version Catalog |

## Project structure

```
app/src/main/java/com/balius/galius/
  core/          # DI, database, navigation, shared MVI
  common/        # Shared UI / utilities
  feature/       # home, search, media, tags, more
  ui/theme/      # Design system tokens
```
