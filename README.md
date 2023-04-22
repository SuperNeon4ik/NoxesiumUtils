# NoxesiumUtils
----------------
Communicate with The [Noxesium](https://github.com/Noxcrew/noxesium) Mod with ease.

### Features
- Send server rules on join
- Send server rules to players with commands
- *(For developers)* Check player's client settings.
- Maybe more soon 🤔

### Requirements
- Spigot 1.19.2+
- The plugin won't do anything to players without the mod

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

### Screenshots
![image](https://user-images.githubusercontent.com/52915540/233479245-01f9fb7e-1d7c-4e98-83ed-ee235e696ff2.png)
![image](https://user-images.githubusercontent.com/52915540/233479405-5f7a96b1-8676-4ea9-9328-0988d348b1ed.png)

### TODO
- Option to kick people without Noxesium mod (or sertain Protocol Version of it)
- Global value for ServerRules
