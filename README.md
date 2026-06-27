# Waffle

My personal fork of [Paper](https://github.com/PaperMC/Paper) — not meant for
distribution, just my own server with my own features.

Waffle stays 100% identical to Paper apart from what I add on top. Paper is a
strict upstream: every Waffle change is a patch layered on Paper, kept clean and
stable, tracking the latest Minecraft (currently 26.2).

## Building

JDK 25 and Git:

```
./gradlew applyAllPatches
./gradlew createPaperclipJar
```

Output lands in `waffle-server/build/libs/`. See [CONTRIBUTING.md](CONTRIBUTING.md)
for the patch workflow.

## What differs from Paper

Only branding, version tracking (`/ver` follows this repo's releases), and the
features listed in the [wiki](https://github.com/rozqn/Waffle/wiki/Waffle-Originals).
The full delta is always one diff away:

```
git -C paper-server diff HEAD~1 HEAD
```

New text follows Paper's style first, vanilla Minecraft's second — see
[STYLE.md](STYLE.md).
