# Audio architecture and migration

## Runtime design

- Short combat effects use one `SoundPool` with eight concurrent streams.
- Background music uses one looping `MediaPlayer` instance.
- `BattleAudioObserver` converts battle-state transitions into platform-neutral `AudioCue` values.
- The game engine remains free of Android `Context`, player instances, and lifecycle calls.
- Missing `res/raw` files fall back to short generated tones, so incomplete asset migration never crashes a battle.

This replaces the HarmonyOS design that repeatedly reset pooled AVPlayer instances. Separating concurrent effects from the single music player removes most reset, prepare, and stale-callback races.

## Importing the original resources

With both repositories available locally:

```bash
python tools/import_harmony_audio.py E:/BattleCity_Deveco_Project_v3
```

The importer searches `entry/src/main/resources/rawfile` for WAV, OGG, and MP3 files and copies compatible names into `app/src/main/res/raw`. It also removes alternate extensions for the same resource stem so Android never receives duplicate resource names.

The report is written to:

```text
build/reports/audio-import.json
```

Confirmed original resource stems include `shoot` and `explode`. Other effects and faction music are optional: the importer records missing stems and the runtime keeps its fallback behaviour.

## Resource naming

Android raw resources must remain lowercase and use only letters, numbers, and underscores. Keep the original stems, for example:

```text
shoot.wav
explode.wav
german_battle.ogg
soviet_battle.mp3
```
