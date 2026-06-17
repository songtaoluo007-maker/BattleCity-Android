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
- Tank collision against brick, steel, base, and village tiles
- Tank-to-tank collision and movement blocking
- Water terrain slowdown using the original 0.52 multiplier
- Destructible brick and village terrain
- Steel destruction for shells with at least 130 penetration
- Projectile blocking, base impact reporting, and terrain score rewards
- Opposing projectile-to-projectile collision
- Short-lived spark, hit-flash, and destruction visual feedback
- Deterministic enemy pursuit with obstacle detours
- Enemy line-of-fire checks and autonomous fire
- Trapped enemy wall escape through destructible terrain
- Scenario enemy budget, reinforcement timing, and five-unit active cap
- Enemy damage against the player and mission failure state
- Kursk breakthrough objective: required kills plus target-zone entry
- Multi-unit rendering, hostile projectile colour, battle HUD, victory and defeat panels
- Unit tests for vehicles, campaigns, maps, movement, terrain, AI, waves, projectile collisions, visual feedback, armour, penetration, and rewards
- GitHub Actions test and debug APK build pipeline

Next migration stages:

1. Ally tanks, squad orders, and formation movement
2. Power-ups and battlefield support skills
3. Campaign, faction, garage, and briefing screens
4. Original tank image assets and richer visual effects
5. Audio system, save data, achievements, and progression
6. Remaining historical scenarios

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

Every push to `main` is configured to run the unit tests, build a debug APK, and upload `BattleCity-Android-debug` as a GitHub Actions artifact.

## Project structure

```text
app/src/main/java/com/songtaoluo/battlecity/
├── MainActivity.kt
├── game/
│   ├── BulletCollisionSystem.kt
│   ├── CampaignCatalog.kt
│   ├── CombatResolver.kt
│   ├── EnemyAiController.kt
│   ├── EnemyAiSystem.kt
│   ├── GameConstants.kt
│   ├── GameEngine.kt
│   ├── GameModels.kt
│   ├── ImpactEffects.kt
│   ├── MovementSystem.kt
│   ├── ProjectileSystem.kt
│   ├── ScenarioCatalog.kt
│   ├── TileMapParser.kt
│   └── VehicleCatalog.kt
├── model/
│   ├── CampaignModels.kt
│   ├── GameTypes.kt
│   └── ScenarioModels.kt
└── ui/
    ├── BattleContent.kt
    ├── BattlefieldCanvas.kt
    ├── BattleScreen.kt
    └── ImpactEffectRenderer.kt
```
