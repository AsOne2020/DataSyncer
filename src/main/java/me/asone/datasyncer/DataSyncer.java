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

package me.asone.datasyncer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.asone.datasyncer.compat.CompatManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

import static com.comphenix.protocol.PacketType.Play.Client.ENTITY_NBT_QUERY;
import static com.comphenix.protocol.PacketType.Play.Client.TILE_NBT_QUERY;
import static me.asone.datasyncer.Action.*;

public final class DataSyncer extends JavaPlugin {
    private ProtocolManager protocolManager;
    private PacketAdapter adapter;
    private final CompatManager compatManager = CompatManager.getInstance();

    private static final Set<Material> OPERATOR_BLOCKS = EnumSet.of(
            Material.COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.JIGSAW,
            Material.STRUCTURE_BLOCK
    );

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = createPacketAdapter();
        protocolManager.addPacketListener(adapter);
    }

    @Override
    public void onDisable() {
        if (protocolManager != null && adapter != null) {
            protocolManager.removePacketListener(adapter);
        }
    }

    private PacketAdapter createPacketAdapter() {
        return new PacketAdapter(this, ListenerPriority.NORMAL,
                ENTITY_NBT_QUERY,
                TILE_NBT_QUERY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Action action = getAction(event.getPacketType());
                if (action == null) return;

                CraftPlayer craftPlayer = (CraftPlayer) event.getPlayer();
                if (!craftPlayer.isOnline() || !action.hasPerm(craftPlayer)) return;

                Bukkit.getRegionScheduler().run(DataSyncer.this, event.getPlayer().getLocation(), task -> {
                    ServerPlayer nmsPlayer = craftPlayer.getHandle();
                    ServerLevel level = nmsPlayer.serverLevel();
                    PacketContainer packet = event.getPacket();
                    int transactionId = packet.getIntegers().read(0);
                    CompoundTag tag = switch (action) {
                        case BLOCK -> getBlockTag(packet, craftPlayer, level, nmsPlayer);
                        case ENTITY -> getEntityTag(packet, craftPlayer, level);
                    };
                    nmsPlayer.connection.send(new ClientboundTagQueryPacket(transactionId, tag));
                });
            }
        };
    }

    private Action getAction(PacketType type) {
        if (type == PacketType.Play.Client.TILE_NBT_QUERY) return BLOCK;
        if (type == PacketType.Play.Client.ENTITY_NBT_QUERY) return ENTITY;
        return null;
    }

    private CompoundTag getBlockTag(PacketContainer packet, CraftPlayer player, ServerLevel level, ServerPlayer nmsPlayer) {
        BlockPosition pos = packet.getBlockPositionModifier().read(0);
        Location location = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());

        if (!location.isChunkLoaded() || !compatManager.check(player, location, BLOCK)) return null;

        BlockEntity blockEntity = level.getBlockEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
        if (blockEntity == null) return null;

        Material material = blockEntity.getBlockState().getBukkitMaterial();
        if (OPERATOR_BLOCKS.contains(material) && !player.hasPermission("datasyncer.op")) return null;

        return blockEntity.saveWithoutMetadata(nmsPlayer.registryAccess());
    }

    private CompoundTag getEntityTag(PacketContainer packet, CraftPlayer player, ServerLevel level) {
        int entityId = packet.getIntegers().read(1);
        Entity entity = level.getEntity(entityId);
        if (entity == null) return null;

        if (entity instanceof Player && !player.hasPermission("datasyncer.entity.player")) return null;
        if (entity instanceof MinecartCommandBlock && !player.hasPermission("datasyncer.op")) return null;

        Location location = entity.getBukkitEntity().getLocation();
        if (!location.isChunkLoaded() || !compatManager.check(player, location, ENTITY)) return null;

        return entity.saveWithoutId(new CompoundTag());
    }
}
