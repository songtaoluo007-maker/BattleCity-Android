# BattleCity Android

Native Android migration and engineering rewrite of the HarmonyOS `BattleCity_Deveco_Project_v3` project, built with Kotlin, Jetpack Compose, and Compose Canvas.

The original HarmonyOS repository remains unchanged. Historical scenarios, vehicles, and combat intent are preserved, while implementation details may be redesigned for Android reliability and maintainability.

## Implemented

- Landscape Android game loop and scaled 17 x 17 battlefield renderer
- Complete 24-vehicle catalog and six historical campaign definitions
- Kursk scenarios, objectives, enemy budgets, and reinforcement waves
- Armour-facing, penetration, ricochet, damage, score, credits, and command points
- Swept movement that prevents wall tunnelling and approaches the last legal position
- Terrain, tank-to-tank, water, mine, and broken-track movement rules
- Destructible terrain and projectile-to-projectile collision
- Enemy pursuit, visibility-gated fire, obstacle detours, and wall escape
- Two ally tanks with follow, hold, assault, and focus-fire orders
- Trailing-side formation safety so allies do not cross through the player after turns
- Forest concealment, smoke concealment, recon reveal, and spotted-state rendering
- Shield, speed, APCR, repair, and freeze supplies
- Timed supply spawning, kill drops, and pity-drop guarantee
- Collector-owned supply effects for player and allies
- Artillery, recon, emergency repair, and smoke support skills
- Command-point costs and per-skill cooldowns
- Manual battlefield tap targeting for artillery and focus fire
- Shared viewport mapping so rendered and tapped battlefield coordinates stay aligned
- Focus-target marker, target-selection prompt, cancellation, and no-cost selection state
- Tactical-area, supply, shield, APCR, track-damage, hit, and destruction visuals
- JVM unit tests and GitHub Actions test/APK pipeline

Architecture and intentional improvements are documented in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Next stages

1. Campaign, faction, garage, and briefing screens
2. Original tank assets and richer effects
3. Audio, save data, achievements, and progression
4. Remaining scenarios and static battlefield units
5. Touch-control polish and device adaptation
6. Battle replay and diagnostic telemetry

## Build

Recommended environment: Android Studio, JDK 17, Gradle 8.9, Android SDK 35.

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

Every push to `main` is configured to run unit tests, build a debug APK, and upload the `BattleCity-Android-debug` artifact.

## Main source layout

```text
app/src/main/java/com/songtaoluo/battlecity/
├── game/
│   ├── AllyAiController.kt
│   ├── AllyAiSystem.kt
│   ├── BoardViewport.kt
│   ├── BulletCollisionSystem.kt
│   ├── CombatResolver.kt
│   ├── EnemyAiController.kt
│   ├── EnemyAiSystem.kt
│   ├── GameEngine.kt
│   ├── MovementSystem.kt
│   ├── PowerUpSystem.kt
│   ├── ProjectileSystem.kt
│   ├── TacticalSystems.kt
│   └── TargetingSystem.kt
├── model/
└── ui/
    ├── BattleContent.kt
    ├── BattlefieldCanvas.kt
    ├── SquadControls.kt
    └── SupportControls.kt
```
