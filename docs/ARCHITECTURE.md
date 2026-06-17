# BattleCity Android architecture

## Direction

The Android version preserves the historical scenarios, vehicle data, battlefield rules, and campaign intent of the HarmonyOS project, but it is not a line-by-line ArkTS translation.

The implementation follows three rules:

1. Pure Kotlin systems own deterministic game rules.
2. `GameEngine` coordinates state and delegates specialised work.
3. Compose renders a snapshot of engine state and does not contain combat rules.

## Runtime layers

### Domain

`model/` contains enums and immutable catalog-facing models shared by scenarios, campaigns, vehicles, support skills, and UI.

### Game systems

- `MovementSystem`: swept movement, terrain speed, wall collision, and tank blocking.
- `EnemyAiSystem` / `EnemyAiController`: public facade plus pursuit and firing implementation.
- `AllyAiSystem` / `AllyAiController`: public facade plus squad formation implementation.
- `VisibilitySystem`: forest, smoke, recon, and spotted-state rules.
- `TankStatusSystem`: cooldown and timed-effect lifecycle.
- `PowerUpSystem`: deterministic drops, stage spawning, TTL, and collector detection.
- `SupportSkillSystem`: command-point cost, cooldown, and artillery damage tiers.
- `ProjectileSystem`: terrain impact.
- `BulletCollisionSystem`: opposing projectile collision.
- `CombatResolver`: armour-facing and penetration resolution.
- `ImpactEffectSystem`: temporary visual feedback state.

### Coordinator

`GameEngine` owns one battle session. It controls update ordering, exposes commands to the UI, and converts system results into score, credits, messages, effects, and mission state.

The update order is intentional:

1. expire visual and tactical effects;
2. update tank status timers;
3. move player;
4. refresh spotting;
5. update allies;
6. update enemies unless frozen;
7. resolve mines;
8. resolve projectiles;
9. resolve supplies;
10. remove destroyed units;
11. spawn reinforcements and supplies;
12. refresh spotting and objectives.

Changing this order can cause one-frame targeting errors, expired shields absorbing damage, or destroyed units acting again.

### UI

- `BattleContent`: Compose state bridge and control layout.
- `BattlefieldCanvas`: board, units, tactical areas, supplies, and effects.
- `SquadControls`: squad orders.
- `SupportControls`: support skills and cooldown labels.

UI code may call engine commands but must not directly modify tank combat state.

## Deliberate improvements over the HarmonyOS source

### Swept movement

Movement is divided into bounded substeps and then binary-searches the final legal distance. This prevents high-frame-distance wall tunnelling while still allowing a tank to approach a wall without leaving a visible gap.

### Safe formation changes

A following ally only reforms when it is already on the trailing side of the player. A sudden player turn therefore does not make allies drive through the player body.

### Collector-owned supplies

Shield, speed, and APCR supplies apply to the friendly tank that actually collected them. The HarmonyOS implementation always modified the player even when an ally picked up the item.

### Explicit timed speed boost

The speed bonus uses `speedBoostMs`; it does not mutate base speed and slowly subtract speed every frame. This makes duration deterministic and prevents floating-point drift.

### Friendly projectile category

Player and ally projectiles share a friendly category for projectile collision. Different enum values do not make them hostile.

### Recon ownership

Friendly recon reveals enemy units only. It cannot accidentally improve enemy vision against friendly units in forests.

### Support cooldowns

Support skills require both command points and a completed cooldown. This prevents repeated low-cost smoke spam and gives each skill a clear tactical cadence.

## Test policy

Every extracted system should have pure JVM unit tests. Engine integration tests cover cross-system ordering such as projectile collision effects, supply pickup, support cost, and mine activation.

The repository CI runs:

```bash
gradle :app:testDebugUnitTest --stacktrace
gradle :app:assembleDebug --stacktrace
```
