# Waffle

Waffle is a fork of [Paper](https://github.com/PaperMC/Paper) that tracks the
latest Minecraft release. Paper is treated as a strict upstream: every Waffle
change is a patch layered on top of Paper, so staying current is a clean upstream
bump rather than a merge, and the fork stays maintainable long-term.

Currently targeting **Minecraft 26.2**.

## Downloads

Builds are published on the [releases page](https://github.com/rozqn/Waffle/releases).

In game, `/version` (alias `/versions`) reports the running Waffle build and
compares it against the latest Waffle release.

## Building

Waffle builds with [paperweight](https://github.com/PaperMC/paperweight), the same
tooling Paper uses. You need JDK 25 and Git:

```
./gradlew applyAllPatches
./gradlew createPaperclipJar
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow.

## Style

User-facing messages follow Paper's conventions first, and vanilla Minecraft's
conventions where something new has to be created. See [STYLE.md](STYLE.md).

## License

Waffle is licensed under the GNU General Public License v3.0, the same license as
Paper's server. See [LICENSE](LICENSE). Waffle is not affiliated with PaperMC or
Mojang Studios.
