/*
 * This file is part of the DataSyncer project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  As_One and contributors
 *
 * DataSyncer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DataSyncer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DataSyncer.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.asone.dataSyncer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.java.JavaPlugin;

public final class DataSyncer extends JavaPlugin {
    private ProtocolManager protocolManager;
    private PacketAdapter adapter;
    private static final boolean DEBUG = false;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = createPacketAdapter();
        protocolManager.addPacketListener(adapter);

        if (DEBUG) {
            getLogger().info("[Debug] DataSyncer enabled - Debug mode active");
        }
    }

    @Override
    public void onDisable() {
        if (protocolManager != null && adapter != null) {
            protocolManager.removePacketListener(adapter);
        }
        if (DEBUG) {
            getLogger().info("[Debug] DataSyncer disabled");
        }
    }

    private PacketAdapter createPacketAdapter() {
        return new PacketAdapter(this, ListenerPriority.NORMAL,
                PacketType.Play.Client.ENTITY_NBT_QUERY,
                PacketType.Play.Client.TILE_NBT_QUERY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPlayer().hasPermission("datasyncer")) return;

                PacketContainer packet = event.getPacket();
                int transactionId = packet.getIntegers().read(0);
                PacketType type = event.getPacketType();

                // 用区域调度器处理
                Bukkit.getRegionScheduler().run(DataSyncer.this,event.getPlayer().getLocation(), (task) -> {
                    CraftPlayer craftPlayer = (CraftPlayer) event.getPlayer();
                    if (!craftPlayer.isOnline()) return;

                    ServerPlayer player = craftPlayer.getHandle();
                    ServerLevel level = player.serverLevel();

                    if (type == PacketType.Play.Client.ENTITY_NBT_QUERY) {
                        int entityId = packet.getIntegers().read(1);
                        handleEntityQuery(player, level, transactionId, entityId);
                    } else if (type == PacketType.Play.Client.TILE_NBT_QUERY) {
                        BlockPosition blockPos = packet.getBlockPositionModifier().read(0);
                        BlockPos pos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        handleBlockQuery(player, level, transactionId, pos);
                    }
                });
            }
        };
    }

    private void handleEntityQuery(ServerPlayer player, ServerLevel level, int transactionId, int entityId) {
        Entity entity = level.getEntity(entityId);
        CompoundTag tag = entity != null ?
                entity.saveWithoutId(new CompoundTag()) : null;

        sendResponse(player, transactionId, tag, "entity",
                entity != null ? entity.getClass().getSimpleName() : null,
                null, entityId);
    }

    private void handleBlockQuery(ServerPlayer player, ServerLevel level, int transactionId, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity != null ?
                blockEntity.saveWithoutMetadata(player.registryAccess()) : null;

        sendResponse(player, transactionId, tag, "block",
                null, pos, -1);
    }

    private void sendResponse(ServerPlayer player, int transactionId, CompoundTag tag,
                              String type, String entityType, BlockPos pos, int entityId) {
        player.connection.send(new ClientboundTagQueryPacket(transactionId, tag));

        if (DEBUG) {
            if (tag != null) {
                String info = type.equals("entity") ?
                        String.format("Entity type: %s | Entity ID: %d", entityType, entityId) :
                        String.format("Position: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());

                getLogger().info(String.format(
                        "[Debug] Sent %s NBT response | %s | Transaction ID: %d | NBT size: %d bytes",
                        type, info, transactionId, tag.size()
                ));
            } else {
                String errorInfo = type.equals("entity") ?
                        String.format("Entity ID: %d", entityId) :
                        String.format("Position: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());

                getLogger().warning(String.format(
                        "[Debug] %s not found | %s | Transaction ID: %d",
                        type.substring(0, 1).toUpperCase() + type.substring(1),
                        errorInfo,
                        transactionId
                ));
            }
        }
    }
}