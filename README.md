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


## Permissions
| Permission               | Description                                                             | Default |
|--------------------------|-------------------------------------------------------------------------|---------|
| datasyncer.block         | Has permission to query block data                                      | op      |
| datasyncer.entity        | Has permission to query entity data                                     | op      |
| datasyncer.entity.player | Has permission to query player's entity data                            | op      |
| datasyncer.op            | Has permission to query operator utilities data, such as command blocks | op      |