# DataSyncer

[![License](https://img.shields.io/github/license/AsOne2020/DataSyncer.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![Issues](https://img.shields.io/github/issues/AsOne2020/DataSyncer.svg)](https://github.com/AsOne2020/DataSyncer/issues)
[![Modrinth](https://img.shields.io/modrinth/dt/KInBrN57?label=Modrinth%20Downloads)](https://modrinth.com/plugin/datasyncer)

A plugin that allows players to sync entity and block entity data by vanilla's tag query protocol

## Depend

- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Features

- **Block Data Syncing**: Enables querying of entity data through BlockEntityNbtQueryPacket
- **Entity Data Syncing**: Enables querying of entity data through EntityNbtQueryPacket
- **Region Protection Compatibility**: Integrates with popular protection plugins:
    - [Residence](https://www.spigotmc.org/resources/11480/) : By using "container" flag
    - [Dominion](https://modrinth.com/plugin/lunadeer-dominion) : By using "container" flag
- **Multi-Version & Folia Support**: Compatible with Minecraft 1.21.x and Folia/Paper/Purpur server software

## Integration with ForceDataSync Mod

To fully utilize the capabilities of this plugin, we recommend the following components based on your role:

- **For Server Administrators**: Install the DataSyncer plugin on your server, which allows players to query entity and block entity data through vanilla's tag query protocol.
- **For Players**: Install the [ForceDataSync](https://github.com/AsOne2020/ForceDataSync) client mod, which bypasses local permission checks in client-side mods such as Tweakeroo, MiniHUD, Litematica, and Tweakermore. This enables their data synchronization features to work properly without requiring operator permissions on the server. [Download ForceDataSync mod here](https://github.com/AsOne2020/ForceDataSync/releases/download/1.0/forcedatasync-1.0.jar)

This combination provides a complete data synchronization experience, allowing you to use these features without needing operator permissions on the client.

## Permissions
| Permission               | Description                                                             | Default |
|--------------------------|-------------------------------------------------------------------------|---------|
| datasyncer.block         | Has permission to query block data                                      | op      |
| datasyncer.entity        | Has permission to query entity data                                     | op      |
| datasyncer.entity.player | Has permission to query player's entity data                            | op      |
| datasyncer.op            | Has permission to query operator utilities data, such as command blocks | op      |

## Client Mod Configuration Recommendations

### About the ForceDataSync Client Mod
To solve the issue of permission inconsistency between client and server, it is highly recommended to install the ForceDataSync mod. Although the DataSyncer server plugin already allows players to query data, client-side mods such as Tweakeroo, Litematica, MiniHUD, and Tweakermore may perform local permission checks (requiring permission level 2 or higher) before enabling data synchronization features. The ForceDataSync mod addresses this permission check inconsistency by bypassing the local permission checks of these client mods, allowing these features to function properly.

### Tweakeroo Mod
**Tweaks Category:**
- `tweakServerDataSync` - **Enable** (Server data synchronization feature)
- `tweakServerDataSyncBackup` - **Enable** (Server data synchronization backup feature)

### Litematica Mod
**Generic Category:**
- `entityDataSync` - **Enable** (Entity data synchronization feature)
- `entityDataSyncBackup` - **Enable** (Entity data synchronization backup feature)

**Info Overlay Category:**
- `blockInfoOverlayEnabled` - **Enable** (Block information overlay display)
  - **Shortcut Key Usage Prerequisite**: This option must be enabled before the `I` key can be used to view block information

### MiniHUD Mod
**Generic Category:**
- `entityDataSync` - **Enable** (Entity data synchronization feature)
- `entityDataSyncBackup` - **Enable** (Entity data synchronization backup feature)
- `inventoryPreviewEnabled` - **Enable** (Container preview feature)
  - **Shortcut Key Usage Prerequisite**: This option must be enabled before the `Alt` key can be used to preview container contents

### Tweakermore Mod
- `serverDataSyncer` - **Enable** (Server data syncer feature)

**Parameter Settings:**
- `serverDataSyncerQueryInterval` - Set to `1` (Fastest query interval)
- `serverDataSyncerQueryLimit` - Set to `25` (Reasonable query quantity limit)
  - **Note**: Users can adjust these parameters according to server conditions. If set too high, players may be kicked by the server for sending too many packets.
