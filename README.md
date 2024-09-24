# NoxesiumUtils
Communicate with the [Noxesium Mod](https://github.com/Noxcrew/noxesium) with ease.

![Modrinth Downloads](https://img.shields.io/modrinth/dt/noxesiumutils?style=for-the-badge)
![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/SuperNeon4ik/NoxesiumUtils/maven.yml?style=for-the-badge)
![GitHub commits since latest release (by SemVer including pre-releases)](https://img.shields.io/github/commits-since/SuperNeon4ik/NoxesiumUtils/latest?style=for-the-badge)

> [!IMPORTANT]
> This branch is still **under development**.

🇺🇦 Made in Ukraine! Зроблено в Україні!\
❤️ Support me on [Patreon](https://patreon.com/SuperNeon4ik)

> [!NOTE]
> With **NoxesiumUtils 2.0.0** I am switching to the [com.noxcrew.noxesium.paper](https://github.com/Noxcrew/noxesium/tree/main/paper)
> implementation, instead of my own. This makes maintaining NoxesiumUtils easier.

### v2.0.0 checklist
Move to the [PR](https://github.com/SuperNeon4ik/NoxesiumUtils/pull/19)

### What is Noxesium and why does it need Utils?
[Noxesium Mod](https://github.com/Noxcrew/noxesium) _(by Noxcrew)_ is a fabric mod with feature additions, bugfixes, and performance improvements. 
It allows servers to offer a better experience to clients through access to additional features beyond vanilla limitations. 
However, servers need to have a plugin to communicate with the mod to actually be able to use it's features, and that's where **NoxesiumUtils** comes in. 
It allows server owners to easily communicate with the mod via commands and can also be used as a dependency for plugin developers.

### Features
- Send server rules on join
- Send server rules to players with commands
- Check player's client settings.
- There is a little more to it, lol.
- Maybe more soon 🤔

### Requirements
- Paper 1.20+
- [CommandAPI](https://modrinth.com/plugin/commandapi)
- _The plugin won't do anything to players without the mod_

### Commands
**Send Server Rule.** Sends a server rule to a selection of players.

_More command examples coming later..._

#### Check player's Noxesium Protocol Version.
```html
/noxesiumutils check <player: player>
/noxesiumutils check <players: selector>
```
#### Check player's settings.
```html
/noxesiumutils clientSettings <player: player>
```

For more detailed information on how everything here works, please refer to the [Noxesium Mod README](https://github.com/Noxcrew/noxesium/#readme)!

### Config
The default config looks like this. 
Right now it only contains a setting to send server rules to a player on join.
```yaml
# View Protocol documentation and information about the mod here:
# https://github.com/Noxcrew/noxesium

# If true will check the plugin's version once in a while.
checkForUpdates: true

# It true will send defaults to Noxesium Players on join.
sendDefaultsOnJoin: false

# Comment out the line if you don't want to send that rule.
defaults:
    #cameraLocked: false
    #disableBoatCollisions: false
    #disableDeferredChunkUpdates: false
    #disableMapUi: false
    #disableSpinAttackCollisions: false
    #disableUiOptimizations: false
    #disableVanillaMusic: false
    #enableSmootherClientTrident: false
    #heldItemNameOffset: 0
    #overrideGraphicsMode: FAST
    #riptideCoyoteTime: 0
    #showMapInUi: false
```
For example, the following config will automatically send the players `disableSpinAttackCollisions = true` on join.
```yaml
# View Protocol documentation and information about the mod here:
# https://github.com/Noxcrew/noxesium

# If true will check the plugin's version once in a while.
checkForUpdates: true

# It true will send defaults to Noxesium Players on join.
sendDefaultsOnJoin: false

# Comment out the line if you don't want to send that rule.
defaults:
    #cameraLocked: false
    #disableBoatCollisions: false
    #disableDeferredChunkUpdates: false
    #disableMapUi: false
    disableSpinAttackCollisions: true
    #disableUiOptimizations: false
    #disableVanillaMusic: false
    #enableSmootherClientTrident: false
    #heldItemNameOffset: 0
    #overrideGraphicsMode: FAST
    #riptideCoyoteTime: 0
    #showMapInUi: false
```


### For Developers
**Adding NoxesiumUtils to your project**

You can easily add NoxesiumUtils to your project from the [Modrinth Maven Repository](https://docs.modrinth.com/docs/tutorials/maven/)!

> [!NOTE] 
> Since NoxesiumUtils is now using [Noxesium/paper](https://github.com/Noxcrew/noxesium/tree/main/paper)
> it might actually be better for you to just implement it yourself. However your can still

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
    <version>LATEST_VERSION</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.noxcrew.noxesium</groupId>
    <artifactId>api</artifactId>
    <version>LATEST_VERSION</version>
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
    url "https://maven.noxcrew.com/public"
}
```
```gradle
implementation "me.superneon4ik:NoxesiumUtils:LATEST_VERSION"
implementation "com.noxcrew.noxesium:api:LATEST_VERSION"
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

_More code examples coming in the near future..._

### Screenshots
![image](https://user-images.githubusercontent.com/52915540/233479245-01f9fb7e-1d7c-4e98-83ed-ee235e696ff2.png)
![image](https://user-images.githubusercontent.com/52915540/233479405-5f7a96b1-8676-4ea9-9328-0988d348b1ed.png)
![image](https://github.com/SuperNeon4ik/NoxesiumUtils/assets/52915540/08710e28-619d-4fb7-9d38-2b41240c96c1)
