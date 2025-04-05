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

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DataSyncer extends JavaPlugin {
    private ProtocolManager protocolManager;
    private PacketAdapter adapter;
    private final ConcurrentLinkedQueue<QueryRequest> queryRequests = new ConcurrentLinkedQueue<>();
    private static final boolean DEBUG = false;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = createPacketAdapter();
        protocolManager.addPacketListener(adapter);

        // 注册每 tick 处理任务
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::processQueries, 0L, 1L);

        if (DEBUG) {
            getLogger().info("[Debug] DataSyncer enabled - Debug mode active");
        }
    }

    @Override
    public void onDisable() {
        if (protocolManager != null && adapter != null) {
            protocolManager.removePacketListener(adapter);
        }
        Bukkit.getScheduler().cancelTasks(this);
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
                PacketContainer packet = event.getPacket();
                if (!event.getPlayer().hasPermission("datasyncer")) return;

                int transactionId = packet.getIntegers().read(0);
                UUID playerUUID = event.getPlayer().getUniqueId();
                PacketType type = event.getPacketType();

                if (type == PacketType.Play.Client.ENTITY_NBT_QUERY) {
                    int entityId = packet.getIntegers().read(1);
                    queryRequests.add(new QueryRequest(type, playerUUID, transactionId, entityId));
                } else if (type == PacketType.Play.Client.TILE_NBT_QUERY) {
                    BlockPosition blockPos = packet.getBlockPositionModifier().read(0);
                    queryRequests.add(new QueryRequest(type, playerUUID, transactionId, blockPos));
                }
            }
        };
    }

    private void processQueries() {
        QueryRequest request;
        if (!queryRequests.isEmpty()) {
            getLogger().info("[DataSyncer] Processing query requests");
        }
        while ((request = queryRequests.poll()) != null) {
            // 获取玩家对象
            CraftPlayer craftPlayer = (CraftPlayer) Bukkit.getPlayer(request.playerUUID);
            if (craftPlayer == null || !craftPlayer.isOnline()) continue;

            ServerPlayer player = craftPlayer.getHandle();
            ServerLevel level = player.serverLevel();

            if (request.type == PacketType.Play.Client.ENTITY_NBT_QUERY) {
                handleEntityQuery(request, player, level);
            } else if (request.type == PacketType.Play.Client.TILE_NBT_QUERY) {
                handleBlockQuery(request, player, level);
            }
        }
    }

    private void handleEntityQuery(QueryRequest request, ServerPlayer player, ServerLevel level) {
        int entityId = request.entityId;
        Entity entity = level.getEntity(entityId);

        if (entity != null) {
            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
            sendResponse(player, request.transactionId, tag, "entity", entity.getClass().getSimpleName(), null, entityId);
        } else {
            logWarning("Entity", request.transactionId, entityId);
        }
    }

    private void handleBlockQuery(QueryRequest request, ServerPlayer player, ServerLevel level) {
        BlockPosition bp = request.blockPosition;
        BlockPos pos = new BlockPos(bp.getX(), bp.getY(), bp.getZ());
        BlockEntity blockEntity = level.getBlockEntity(pos);

        CompoundTag tag = blockEntity != null
                ? blockEntity.saveWithoutMetadata(player.registryAccess())
                : null;

        sendResponse(player, request.transactionId, tag, "block", null, pos, -1);
    }

    private void sendResponse(ServerPlayer player, int transactionId, CompoundTag tag,
                              String type, String entityType, BlockPos pos, int entityId) {
        player.connection.send(new ClientboundTagQueryPacket(transactionId, tag));

        if (DEBUG) {
            if (tag != null) {
                String info = type.equals("entity")
                        ? String.format("Entity type: %s | Entity ID: %d", entityType, entityId)
                        : String.format("Position: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());

                getLogger().info(String.format(
                        "[Debug] Sent %s NBT response | %s | Transaction ID: %d | NBT size: %d bytes",
                        type, info, transactionId, tag.size()
                ));
            } else {
                String errorInfo = type.equals("entity")
                        ? String.format("Entity ID: %d", entityId)
                        : String.format("Position: [%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());

                getLogger().warning(String.format(
                        "[Debug] %s not found | %s | Transaction ID: %d",
                        type.substring(0, 1).toUpperCase() + type.substring(1),
                        errorInfo,
                        transactionId
                ));
            }
        }
    }

    private void logWarning(String type, int transactionId, int entityId) {
        if (DEBUG) {
            getLogger().warning(String.format(
                    "[Debug] %s not found | ID: %d | Transaction ID: %d",
                    type, entityId, transactionId
            ));
        }
    }

    private static class QueryRequest {
        final PacketType type;
        final UUID playerUUID;
        final int transactionId;
        final int entityId;
        final BlockPosition blockPosition;

        QueryRequest(PacketType type, UUID playerUUID, int transactionId, int entityId) {
            this.type = type;
            this.playerUUID = playerUUID;
            this.transactionId = transactionId;
            this.entityId = entityId;
            this.blockPosition = null;
        }

        QueryRequest(PacketType type, UUID playerUUID, int transactionId, BlockPosition blockPosition) {
            this.type = type;
            this.playerUUID = playerUUID;
            this.transactionId = transactionId;
            this.entityId = -1;
            this.blockPosition = blockPosition;
        }
    }
}