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

package me.asone.datasyncer.util;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class VersionUtils {
    private static final String SERVER_VERSION = "v" + Bukkit.getMinecraftVersion().replace(".", "_");
    private static final Map<String, String> VERSION_MAPPING = new HashMap<>();

    static {
        VERSION_MAPPING.put("v1_21_1", "v1_21");
        VERSION_MAPPING.put("v1_21_3", "v1_21_2");
        VERSION_MAPPING.put("v1_21_7", "v1_21_6");
        VERSION_MAPPING.put("v1_21_8", "v1_21_6");
    }

    public static String getPackageVersion() {
        return VERSION_MAPPING.getOrDefault(SERVER_VERSION, SERVER_VERSION);
    }
}