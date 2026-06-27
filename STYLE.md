# Waffle message & text style

Waffle is a fork of Paper. Every user-facing string Waffle adds or changes —
command output, log lines, config comments, kick/disconnect text, GUI labels —
follows one rule, in order:

1. **Paper first.** Match how Paper already writes the same kind of message.
   If Paper has a precedent, copy its construction and tone exactly.
2. **Vanilla Mojang second.** Only when a message must be newly created and Paper
   has no precedent (for example, a brand-new translation key), follow vanilla
   Minecraft's conventions.

Never invent a third style.

## 1. Paper style (primary)

### Programmatic messages — Adventure components

New code sends `net.kyori.adventure.text.Component`, never legacy `§`/`&` strings.

```java
import static net.kyori.adventure.text.Component.text;
import net.kyori.adventure.text.format.NamedTextColor;

sender.sendMessage(text("Usage: " + usage, NamedTextColor.RED));
```

- Errors / usage lines: `NamedTextColor.RED`.
- Success: `GREEN`; neutral info: `GRAY` / `WHITE` / `YELLOW`.
- Unset color renders white and is **not** italic in chat. Use
  `decorationIfAbsent(...)` so you never clobber a user's own formatting.
- Permission failures defer to `Bukkit.permissionMessage()`; don't hand-roll one.

### Config-driven player text — MiniMessage

Strings stored in config (kick messages, flying messages, …) are authored in
MiniMessage and parsed with the default tag set:

```yaml
# Message shown to kicked players. Formatted using MiniMessage.
kick-message: "<red>You were kicked from the server"
```

### Config comments

Defined in Java with Configurate `@Comment`. Keys are camelCase in Java and
serialize to kebab-case in YAML. Comment text is a short descriptive sentence,
first word capitalized, technical/imperative tone. No emoji, no filler.

```java
@Comment("Whether the waffle feature is enabled. Disable to fully turn off waffle handling.")
public boolean featureEnabled = true; // -> waffle.feature-enabled
```

- Multi-sentence comment → end with a period.
- Short single clause → trailing period optional, follow the nearest Paper key.
- Inline commands/code in backticks or single quotes: `` `/ride` ``, `'/ride'`.

### Log / startup lines

Use the SLF4J/Log4j2 logger, plain sentences, capitalized, professional. No color
codes, no emoji in log output.

### Re-pointing upstream messaging

Where Paper hardcodes "report this to Paper", redirect it to Waffle's issue
tracker (`https://github.com/rozqn/Waffle/issues`) — this is the standard fork
courtesy so Paper does not receive misattributed reports. Do not otherwise change
Paper's default brand/MOTD behavior unless a patch is specifically about branding.

## 2. Vanilla Mojang style (secondary — only for newly created messages)

Used when adding a brand-new translation key with no Paper analog. Keys live in
`assets/minecraft/lang/en_us.json` (American English is the source language).

- **Keys**: lowercase, dot-namespaced, grouped by domain —
  `commands.<name>.<result>`, `multiplayer.disconnect.<reason>`. Result suffixes:
  `.success`, `.failed`, `.single`, `.multiple`, `.self`, `.other`.
- **Capitalization**: sentence case — only the first letter capitalized (proper
  nouns and brands keep their caps). Never Title Case.
- **Punctuation**: **no trailing period** on single-clause command feedback
  (verified: 0 of 501 vanilla `commands.*` strings end with a period).
  Multi-sentence / instructional text (disconnect screens) uses full punctuation.
- **Tone**: concise, neutral, declarative — report the result, not the actor.
  "Set the time to %s", "Made %s a server operator", "There are no whitelisted players".
- **Placeholders**: `%s` for positional; `%1$s` / `%2$s` when order matters.
  Interpolated values in single quotes: "Invalid or unknown game mode '%s'".
- **Failures**: phrase as "Nothing changed. …" / "No home was found" rather than
  throwing a technical error.

### Examples

| Don't | Do |
| --- | --- |
| `§cYou don't have permission!!!` | `text("You do not have permission to use this command", RED)` |
| `"...success": "Successfully Reloaded The Config."` | `"...success": "Reloaded the Waffle config"` |
| `"...failed": "ERROR: home not found!!!"` | `"...failed": "No home was found"` |
| `# turn this on to enable waffles` | `@Comment("Whether the waffle feature is enabled. ...")` |

Rule of thumb: single-clause command feedback → no trailing period; multi-sentence
text → normal punctuation; programmatic messages → Adventure `Component` +
`NamedTextColor`; new persisted text → vanilla-style translation key.
