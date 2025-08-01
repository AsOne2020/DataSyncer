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

package me.asone.datasyncer.nms.v1_21;

import me.asone.datasyncer.nms.NMSHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class NMSHandlerImpl extends NMSHandler {

    @Override
    public void sendTagQueryResponse(org.bukkit.entity.Player player, int transactionId, Object tag) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundTagQueryPacket(transactionId, (CompoundTag) tag));
    }

    @Override
    public org.bukkit.Material getBlockMaterialAt(Location location) {
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return level.getBlockState(pos).getBukkitMaterial();
    }

    @Override
    public Object getBlockTag(Location location, org.bukkit.entity.Player player) {
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;

        return blockEntity.saveWithoutMetadata(nmsPlayer.registryAccess());
    }

    @Override
    public Location getEntityLocation(int entityId, World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Entity entity = level.getEntity(entityId);
        return entity != null ? entity.getBukkitEntity().getLocation() : null;
    }

    @Override
    public boolean isPlayerEntity(int entityId, World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Entity entity = level.getEntity(entityId);
        return entity instanceof Player;
    }

    @Override
    public boolean isCommandMinecart(int entityId, World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Entity entity = level.getEntity(entityId);
        return entity instanceof MinecartCommandBlock;
    }

    @Override
    public Object getEntityTag(int entityId, World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Entity entity = level.getEntity(entityId);
        return entity != null ? entity.saveWithoutId(new CompoundTag()) : null;
    }
}