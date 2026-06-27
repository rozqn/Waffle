# Contributing to Waffle

Waffle is built with [paperweight](https://github.com/PaperMC/paperweight), the
same tooling Paper uses. Waffle is a hard fork: Paper is checked out at a pinned
commit and Waffle's changes are applied on top as patches.

## Requirements

- JDK 25
- Git

## Setup

```
./gradlew applyAllPatches
```

This clones Paper at the pinned `paperRef` (see `gradle.properties`), decompiles
Minecraft, and materializes the patched sources into `paper-server/` and
`paper-api/` (working directories, gitignored).

## Making a change

For a brand-new file, drop a plain `.java` under `waffle-server/src/main/java` or
`waffle-api/src/main/java` and commit it — no patch needed.

To change existing Paper or Minecraft code, edit the materialized source under
`paper-server/` or `paper-api/`, then turn the edit into a file patch:

```
git -C paper-server add -A
./gradlew waffle-server:fixupPaperServerFilePatches waffle-server:rebuildPaperServerFilePatches
```

The API equivalents are `fixupPaperApiFilePatches` / `rebuildPaperApiFilePatches`;
for the build scripts, `rebuildPaperSingleFilePatches`. Larger, self-describing
changes are better kept as feature patches (real commits in the materialized repo),
rebuilt with `waffle-server:rebuildPaperServerFeaturePatches`. Commit the
regenerated patch files in this repository.

### Patch layout

- `waffle-server/minecraft-patches/` — changes to vanilla Minecraft code
  (`features/` for staged commits, `sources/` for direct file patches)
- `waffle-server/paper-patches/` — changes to Paper's server code
- `waffle-api/paper-patches/` — changes to the Paper API
- `waffle-server/src`, `waffle-api/src` — brand-new files Waffle adds
- `build-data/fork.at` — access transformers

## Building a runnable jar

```
./gradlew createPaperclipJar
```

The paperclip jar is written to `waffle-server/build/libs/`.

## Updating upstream Paper

Bump `paperRef` (and, on a Minecraft version bump, `mcVersion` / `apiVersion`) in
`gradle.properties`, then:

```
./gradlew applyAllPatches    # resolve any rejected hunks
./gradlew rebuildPaperPatches waffle-server:rebuildAllServerPatches
```

Commit the bumped `paperRef` together with the rebuilt patches.

## Message style

All user-facing text follows [STYLE.md](STYLE.md): Paper conventions first, vanilla
Minecraft conventions for anything that has to be newly created.
