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

package me.asone.datasyncer.nms;

import me.asone.datasyncer.DataSyncer;
import me.asone.datasyncer.util.VersionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class NMSHandler {
    private static NMSHandler instance = null;

    public static NMSHandler getInstance() {
        if (instance == null) {
            instance = createHandler();
        }
        return instance;
    }

    private static NMSHandler createHandler() {
        String version = VersionUtils.getPackageVersion();
        try {
            Class<?> clazz = Class.forName("me.asone.datasyncer.nms." + version + ".NMSHandlerImpl");
            return (NMSHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            DataSyncer.getInstance().getLogger().warning("Unsupported server version: " + VersionUtils.getPackageVersion());
            return null;
        }
    }

    public abstract void sendTagQueryResponse(Player player, int transactionId, Object tag);

    public abstract Material getBlockMaterialAt(Location location);

    public abstract Object getBlockTag(Location location, Player player);

    public abstract Location getEntityLocation(int entityId, World world);

    public abstract boolean isPlayerEntity(int entityId, World world);

    public abstract boolean isCommandMinecart(int entityId, World world);

    public abstract Object getEntityTag(int entityId, World world);
}