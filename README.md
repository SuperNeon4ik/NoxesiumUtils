# NoxesiumUtils
----------------
Communicate with the [Noxesium Mod](https://github.com/Noxcrew/noxesium) with ease.

Made in Ukraine! Зроблено в Україні! 🇺🇦\
Support me on [Patreon](https://patreon.com/SuperNeon4ik) ❤️

### What is Noxesium and why does it need Utils?
[Noxesium Mod](https://github.com/Noxcrew/noxesium) _(by Noxcrew)_ is a fabric mod with feature additions, bugfixes, and performance improvements. It allows servers to offer a better experience to clients through access to additional features beyond vanilla limitations. However, servers need to have a plugin to communicate with the mod to actually be able to use it's features, and that's where **NoxesiumUtils** comes in. It allows server owners to easily communicate with the mod via commands and can also be used as a dependency for plugin developers.

### Features
- Send server rules on join
- Send server rules to players with commands
- *(For developers)* Check player's client settings.
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

**Check player's Noxesium Protocol Version.**
```html
/noxesiumutils check <player: player>
```
**Reset player's Noxesium Session data.**
```html
<!-- Since Noxesium Protocol Version 3 -->
/noxesiumutils reset <players: selector> <all | cachedPlayerSkulls>
```

For more detailed information on how everything here works, please refer to the [Noxesium Mod README](https://github.com/Noxcrew/noxesium/#readme)!

### For Developers
**Run code for Noxesium players**
```java
NoxesiumUtils.forNoxesiumPlayers(int minProtocol, BiConsumer<Player, Integer> playerConsumer);
NoxesiumUtils.forNoxesiumPlayers(Collection<Player> players, int minProtocol, BiConsumer<Player, Integer> playerConsumer);
```
```java
NoxesiumUtils.forNoxesiumPlayers(1, (player, protocolVersion) -> {
  player.sendMessage("Your Noxesium Protocol Version is " + protocolVersion);
});
```

**Build a ServerRules packet**
```java
byte[] packet = new NoxesiumServerRuleBuilder(1)
  .add(0, true) // Disable Auto Spin Attack
  .add(3, 10) // Set Held Item Name Offset to 10
  .build(); // Build the packet
```

**Send ServerRules packet**
```java
NoxesiumUtils.sendServerRulesPacket(Player player, byte[] packet);
```

**Get player's protocol version**
```java
int protocolVersion = NoxesiumUtils.getPlayerProtocolVersion(player.getUniqueId());
```

**Get player's client settings**
```java
PlayerClientSettings playerClientSettings = NoxesiumUtils.getPlayerClientSettings(player.getUniqueId());
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
    public void on(NoxesiumPlayerClientInformationEvent event) {
        // Received player's client settings
        Player player = event.getPlayer();
        PlayerClientSettings playerClientSettings = event.getPlayerClientSettings();
    }
}
```

**Noxesium player skull component builder**
```java
Component component = new NoxesiumPlayerSkullBuilder(player.getUniqueID())
        .setGrayscale(true)
        .build();

```

### Screenshots
![image](https://user-images.githubusercontent.com/52915540/233479245-01f9fb7e-1d7c-4e98-83ed-ee235e696ff2.png)
![image](https://user-images.githubusercontent.com/52915540/233479405-5f7a96b1-8676-4ea9-9328-0988d348b1ed.png)