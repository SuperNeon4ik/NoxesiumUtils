# NoxesiumUtils
----------------
Communicate with the [Noxesium Mod](https://github.com/Noxcrew/noxesium) with ease.

![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/noxesiumutils?style=for-the-badge)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/noxesiumutils?style=for-the-badge)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/SuperNeon4ik/NoxesiumUtils/maven.yml?style=for-the-badge)
![GitHub commits since latest release (by SemVer including pre-releases)](https://img.shields.io/github/commits-since/SuperNeon4ik/NoxesiumUtils/latest?style=for-the-badge)


Made in Ukraine! Зроблено в Україні! 🇺🇦\
Support me on [Patreon](https://patreon.com/SuperNeon4ik) ❤️

> **Warning**
> With **Noxesium release 1.0.0** there were massive changes in the API, which forced me to almost
> completely rewrite my code, so if you ever depended on NoxesiumUtils, please go over the changes.

### What is Noxesium and why does it need Utils?
[Noxesium Mod](https://github.com/Noxcrew/noxesium) _(by Noxcrew)_ is a fabric mod with feature additions, bugfixes, and performance improvements. It allows servers to offer a better experience to clients through access to additional features beyond vanilla limitations. However, servers need to have a plugin to communicate with the mod to actually be able to use it's features, and that's where **NoxesiumUtils** comes in. It allows server owners to easily communicate with the mod via commands and can also be used as a dependency for plugin developers.

### Features
- Send server rules on join
- Send server rules to players with commands
- Check player's client settings.
- There is a little more to it, lol.
- Maybe more soon 🤔

### Requirements
- Spigot 1.19.2+
- The plugin won't do anything to players without the mod

### Commands
**Send Server Rule.** Sends a server rule to a selection of players.
```html
/noxesiumutils disableAutoSpinAttack <players: selector> <value: boolean>
```
```html
/noxesiumutils globalCanDestroy <players: selector> <value: item> <value: item> <value: item> ...
```
```html
/noxesiumutils globalCanPlaceOn <players: selector> <value: item> <value: item> <value: item> ...
```
```html
<!-- Since Noxesium Protocol Version 2 -->
/noxesiumutils heldItemNameOffset <players: selector> <value: number>
```
```html
<!-- Since Noxesium Protocol Version 2 -->
/noxesiumutils cameraLocked <players: selector> <value: boolean>
```

#### Reset stuff
```html
<!-- Since Noxesium Protocol Version 3 -->
/noxesiumutils resetServerRules <players: selector>
```
```html
<!-- Since Noxesium Protocol Version 3 -->
/noxesiumutils reset <players: selector> <ALL_SERVER_RULES|CACHED_PLAYER_HEADS>
```

#### Check player's Noxesium Protocol Version.
```html
/noxesiumutils check <player: player>
/noxesiumutils check <players: selector>
```
#### Check player's settings.
**Note:** Players with protocol version less than 3 will have most of their settings set to zeros.
```html
<!-- Since Noxesium Protocol Version 3 -->
/noxesiumutils clientSettings <player: player>
```

For more detailed information on how everything here works, please refer to the [Noxesium Mod README](https://github.com/Noxcrew/noxesium/#readme)!

### For Developers
**Adding NoxesiumUtils to your project**

You can easily add NoxesiumUtils to your project from the [Modrinth Maven Repository](https://docs.modrinth.com/docs/tutorials/maven/)!
### Maven:
```xml
<repository>
    <id>neon-repository-releases</id>
    <name>Neon Repository</name>
    <url>https://repo.superneon4ik.me/releases</url>
</repository>
<repository>
    <id>noxcrew-maven</id>
    <name>Noxcrew Public Maven Repository</name>
    <url>https://maven.noxcrew.com/public</url>
</repository>
```
```xml
<dependency>
    <groupId>me.superneon4ik</groupId>
    <artifactId>NoxesiumUtils</artifactId>
    <version>1.5.0</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.noxcrew.noxesium</groupId>
    <artifactId>api</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle:
```gradle
maven {
    name "neonRepositoryReleases"
    url "https://repo.superneon4ik.me/releases"
}
maven {
    name "noxcrewMaven"
    url "https://maven.noxcrew.com/releases"
}
```
```gradle
implementation "me.superneon4ik:NoxesiumUtils:1.5.0"
implementation "com.noxcrew.noxesium:api:1.0.0"
```

**Run code for Noxesium players**
```java
NoxesiumUtils.getManager().forNoxesiumPlayers(NoxesiumFeature minProtocol, Consumer<Player> action);
NoxesiumUtils.getManager().forNoxesiumPlayers(Collection<Player> players, NoxesiumFeature minProtocol, Consumer<Player> action);
```
```java
NoxesiumUtils.getManager().forNoxesiumPlayers(NoxesiumFeature.ANY, p -> p.sendMessage(Component.text("Hello!")));
```

**Send Server Rules**
```java
var rule = NoxesiumUtils.getManager().<Boolean>getServerRule(player, ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS);
rule.setValue(true);
new ClientboundChangeServerRulesPacket(List.of(rule)).send(player);
```

**Get player's protocol version**
```java
int protocolVersion = NoxesiumUtils.getManager().getProtocolVersion(player);
```

**Get player's client settings**
```java
ClientSettings clientSettings = NoxesiumUtils.getManager().getClientSettings(player);
```

**Noxesium events**
```java
public class EventListener implements Listener {
    @EventHandler
    public void on(NoxesiumPlayerJoinEvent event) {
        // Received player's noxesium protocol version
        Player player = event.getPlayer();
        int protocolVersion = event.getProtocolVersion();
    }

    @EventHandler
    public void on(NoxesiumPlayerClientSettingsEvent event) {
        // Received player's client settings
        Player player = event.getPlayer();
        ClientSettings clientSettings = event.getClientSettings();
    }
}
```

### Screenshots
![image](https://user-images.githubusercontent.com/52915540/233479245-01f9fb7e-1d7c-4e98-83ed-ee235e696ff2.png)
![image](https://user-images.githubusercontent.com/52915540/233479405-5f7a96b1-8676-4ea9-9328-0988d348b1ed.png)
![зображення](https://github.com/SuperNeon4ik/NoxesiumUtils/assets/52915540/08710e28-619d-4fb7-9d38-2b41240c96c1)
