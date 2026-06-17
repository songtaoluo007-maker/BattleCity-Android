# BattleCity Android

Android migration of the HarmonyOS `BattleCity_Deveco_Project_v3` project.

The original HarmonyOS repository remains unchanged. This repository is a native Android implementation using Kotlin, Jetpack Compose, and Compose Canvas.

## Current migration status

Implemented:

- Native Android application module
- Landscape full-screen activity
- Frame-synchronised game loop with `withFrameNanos`
- HarmonyOS core enums and domain types
- Complete 24-vehicle catalog across German, Soviet, British, and American factions
- Original vehicle speed, HP, reload, shell, penetration, armour, price, colour, and history data
- Front, side, and rear armour resolution
- Ricochet, blocked hit, reduced damage, destruction, score, and credit rewards
- Six historical campaign definitions and unlock progression
- Both Kursk scenario definitions
- Validated 17 x 17 HarmonyOS tile-map parser
- Scaled Compose Canvas rendering for the Kursk battlefield
- Unit tests for vehicles, campaign order, map parsing, armour, penetration, and rewards
- GitHub Actions test and debug APK build pipeline

Next migration stages:

1. Wall, water, forest, and tank collision
2. Enemy AI and multiple active enemies
3. Campaign, faction, garage, and briefing screens
4. Original tank image assets and effects
5. Audio system, save data, achievements, and progression
6. Complete scenario catalog

## Build

Recommended environment:

- Android Studio
- JDK 17
- Gradle 8.9
- Android SDK 35

Open the repository in Android Studio and sync the Gradle project. For command-line builds with Gradle installed:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

Every push to `main` runs the unit tests, builds a debug APK, and uploads `BattleCity-Android-debug` as a GitHub Actions artifact.

## Project structure

```text
app/src/main/java/com/songtaoluo/battlecity/
├── MainActivity.kt
├── game/
│   ├── CampaignCatalog.kt
│   ├── CombatResolver.kt
│   ├── GameConstants.kt
│   ├── GameEngine.kt
│   ├── GameModels.kt
│   ├── ScenarioCatalog.kt
│   ├── TileMapParser.kt
│   └── VehicleCatalog.kt
├── model/
│   ├── CampaignModels.kt
│   ├── GameTypes.kt
│   └── ScenarioModels.kt
└── ui/
    └── BattleScreen.kt
```
