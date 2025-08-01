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
import me.asone.datasyncer.nms.NMSHandler;
import me.asone.datasyncer.permission.Permissions;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.comphenix.protocol.PacketType.Play.Client.ENTITY_NBT_QUERY;
import static com.comphenix.protocol.PacketType.Play.Client.TILE_NBT_QUERY;
import static me.asone.datasyncer.Action.*;

public final class DataSyncer extends JavaPlugin {
    boolean devMode;
    private ProtocolManager protocolManager;
    private PacketAdapter adapter;
    private NMSHandler nmsHandler;
    private CompatManager compatManager;
    private static DataSyncer instance;
    public static final int METRICS_SERVICE_ID = 26729;
    @SuppressWarnings("FieldCanBeLocal")
    private Metrics metric;

    public static DataSyncer getInstance() {
        return instance;
    }

    private static final Set<Material> OPERATOR_BLOCKS = EnumSet.of(
            Material.COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.JIGSAW,
            Material.STRUCTURE_BLOCK
    );

    @Override
    public void onEnable() {
        instance = this;
        devMode = Boolean.parseBoolean(System.getProperty(this.getName() + ".DEV_MODE"));
        this.nmsHandler = NMSHandler.getInstance();
        if (nmsHandler == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!devMode) {
            metric = new Metrics(this, METRICS_SERVICE_ID);
            metric.addCustomChart(new AdvancedPie("minecraft_version_player_amount", () -> Map.of(Bukkit.getMinecraftVersion(), Bukkit.getOnlinePlayers().size())));
        }
        compatManager = CompatManager.getInstance();
        protocolManager = ProtocolLibrary.getProtocolManager();
        adapter = createPacketAdapter();
        protocolManager.addPacketListener(adapter);
        getLogger().info("DataSyncer has been enabled!");
    }

    @Override
    public void onDisable() {
        if (protocolManager != null && adapter != null) {
            protocolManager.removePacketListener(adapter);
        }
        getLogger().info("DataSyncer has been disabled!");
    }

    private PacketAdapter createPacketAdapter() {
        return new PacketAdapter(this, ListenerPriority.NORMAL,
                ENTITY_NBT_QUERY,
                TILE_NBT_QUERY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Action action = getAction(event.getPacketType());
                if (action == null) return;

                Player player = event.getPlayer();
                if (!player.isOnline() || !action.hasPerm(player)) return;

                Bukkit.getRegionScheduler().run(DataSyncer.this, player.getLocation(), task -> {
                    PacketContainer packet = event.getPacket();
                    int transactionId = packet.getIntegers().read(0);

                    Object tag = switch (action) {
                        case BLOCK -> getBlockTag(packet, player);
                        case ENTITY -> getEntityTag(packet, player);
                    };

                    if (tag != null) {
                        nmsHandler.sendTagQueryResponse(player, transactionId, tag);
                    }
                });
            }
        };
    }

    private Action getAction(PacketType type) {
        if (type == PacketType.Play.Client.TILE_NBT_QUERY) return BLOCK;
        if (type == PacketType.Play.Client.ENTITY_NBT_QUERY) return ENTITY;
        return null;
    }

    private Object getBlockTag(PacketContainer packet, Player player) {
        BlockPosition pos = packet.getBlockPositionModifier().read(0);
        Location location = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());

        if (!location.isChunkLoaded() || !compatManager.check(player, location, BLOCK)) return null;

        Material material = nmsHandler.getBlockMaterialAt(location);
        if (OPERATOR_BLOCKS.contains(material) && !player.hasPermission(Permissions.OP)) return null;

        return nmsHandler.getBlockTag(location, player);
    }

    private Object getEntityTag(PacketContainer packet, Player player) {
        int entityId = packet.getIntegers().read(1);
        Location location = nmsHandler.getEntityLocation(entityId, player.getWorld());
        if (location == null) return null;

        if (!location.isChunkLoaded() || !compatManager.check(player, location, ENTITY)) return null;

        if (nmsHandler.isPlayerEntity(entityId, player.getWorld()) && !player.hasPermission(Permissions.ENTITY_PLAYER)) {
            return null;
        }

        if (nmsHandler.isCommandMinecart(entityId, player.getWorld()) && !player.hasPermission(Permissions.OP)) {
            return null;
        }

        return nmsHandler.getEntityTag(entityId, player.getWorld());
    }
}