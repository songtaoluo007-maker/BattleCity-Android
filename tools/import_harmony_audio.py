#!/usr/bin/env python3
"""Import compatible audio resources from the HarmonyOS BattleCity project.

Usage:
    python tools/import_harmony_audio.py E:/BattleCity_Deveco_Project_v3

The argument may point to the HarmonyOS repository root or directly to its
`entry/src/main/resources/rawfile` directory.
"""

from __future__ import annotations

import argparse
import json
import shutil
from pathlib import Path

AUDIO_STEMS = (
    "shoot",
    "explode",
    "hit",
    "pickup",
    "repair",
    "spot",
    "support",
    "command",
    "aircraft_inbound",
    "aircraft_dive",
    "aircraft_depart",
    "bomb_explosion",
    "ricochet_whiz",
    "shell_hit",
    "one_minute_of_proud",
    "german_theme",
    "german_battle",
    "soviet_theme",
    "soviet_battle",
    "soviet_battle_moscow",
    "victory_german",
    "defeat_german",
    "victory",
    "defeat",
    "british_menu",
    "british_battle",
    "victory_allied",
    "american_menu",
    "american_battle",
    "bgm",
)
SUPPORTED_EXTENSIONS = ("wav", "ogg", "mp3")


def resolve_raw_directory(source: Path) -> Path:
    direct = source.resolve()
    nested = direct / "entry" / "src" / "main" / "resources" / "rawfile"
    if nested.is_dir():
        return nested
    if direct.is_dir() and direct.name == "rawfile":
        return direct
    raise FileNotFoundError(
        f"Could not find HarmonyOS rawfile directory under: {source}"
    )


def import_audio(source: Path, destination: Path) -> dict[str, object]:
    raw_directory = resolve_raw_directory(source)
    destination.mkdir(parents=True, exist_ok=True)

    imported: list[str] = []
    missing: list[str] = []
    for stem in AUDIO_STEMS:
        source_file = next(
            (
                raw_directory / f"{stem}.{extension}"
                for extension in SUPPORTED_EXTENSIONS
                if (raw_directory / f"{stem}.{extension}").is_file()
            ),
            None,
        )
        if source_file is None:
            missing.append(stem)
            continue

        target = destination / source_file.name.lower()
        for extension in SUPPORTED_EXTENSIONS:
            alternate = destination / f"{stem}.{extension}"
            if alternate != target and alternate.exists():
                alternate.unlink()
        shutil.copy2(source_file, target)
        imported.append(target.name)

    report = {
        "source": str(raw_directory),
        "destination": str(destination.resolve()),
        "imported": imported,
        "missing": missing,
    }
    (destination / "import-report.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return report


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="HarmonyOS repository or rawfile path")
    parser.add_argument(
        "--destination",
        type=Path,
        default=Path("app/src/main/res/raw"),
        help="Android raw resource directory",
    )
    args = parser.parse_args()

    try:
        report = import_audio(args.source, args.destination)
    except (FileNotFoundError, OSError) as error:
        parser.error(str(error))

    print(f"Imported {len(report['imported'])} audio files")
    for filename in report["imported"]:
        print(f"  + {filename}")
    if report["missing"]:
        print(f"Missing {len(report['missing'])} optional stems; runtime fallback remains active")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
