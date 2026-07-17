# InlineHeadsLib

Lightweight library for rendering player heads as inline text in Minecraft. Works in chat, scoreboards, titles, action bars, and any other text component location.

## Requirements

- Java 21+
- Paper/Folia 1.21.4+
- [Pixelized resource pack](https://github.com/BertTowne/Pixelized) installed on clients

## Installation

### Gradle (build.gradle.kts)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("frqme.isa:InlineHeadsLib:1.0.0")
}
```

### Maven (pom.xml)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>frqme.isa</groupId>
    <artifactId>InlineHeadsLib</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
import frqme.isa.headlib.HeadRenderer;
import frqme.isa.headlib.HeadUtils;

// Initialize (in your plugin's onEnable)
private HeadRenderer headRenderer = new HeadRenderer();

// Get a player's head (async - safe for main thread)
headRenderer.getHeadAsync("Notch").thenAccept(head -> {
    Component message = HeadUtils.buildChatMessage(head, "Notch", Component.text("Hello!"));
    getServer().sendMessage(message);
});

// Pre-cache on player join (async)
headRenderer.warmCache(player.getName());

// Count totems
int totems = HeadUtils.countTotems(player);

// Build HUD line
Component hud = HeadUtils.buildHudLine(head, totems);
```

## API Reference

### HeadRenderer

| Method | Description |
|--------|-------------|
| `HeadRenderer()` | Creates renderer with 10-minute cache |
| `HeadRenderer(Duration)` | Creates renderer with custom cache duration |
| `getHead(String)` | Get head synchronously (blocks - use async!) |
| `getHeadAsync(String)` | Get head as CompletableFuture |
| `warmCache(String)` | Pre-cache a player's head (call async) |
| `invalidate(String)` | Remove a player from cache |
| `invalidateAll()` | Clear entire cache |

### HeadUtils

| Method | Description |
|--------|-------------|
| `countTotems(Player)` | Count totems in inventory (main + armor + offhand) |
| `buildChatMessage(Component, String, Component)` | Format: `HEAD PlayerName: message` |
| `buildHudLine(Component, int)` | Format: `HEAD X count` |
| `getOnlinePlayerTotems(Server)` | Map of all online players to totem counts |

## How It Works

1. Fetches 8x8 skin texture from [Minotar API](https://minotar.net/)
2. Maps each pixel to a Unicode character in the `pixelized` font
3. Colors each character with the pixel's exact RGB value
4. Arranges in 8x8 grid using negative-space font characters
5. Result: 64 tiny colored characters that visually form the player's head

## Resource Pack

The [Pixelized resource pack](https://github.com/BertTowne/Pixelized) must be installed on clients. Host it and force it via server properties or a plugin like ForcePack.

Pack URL: `https://download.mc-packs.net/pack/e0b2a9e2459f597c0336fcae710c45d43a61f0de.zip`

## License

MIT
