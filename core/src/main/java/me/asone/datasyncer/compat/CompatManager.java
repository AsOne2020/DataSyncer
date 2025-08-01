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

package me.asone.datasyncer.compat;

import me.asone.datasyncer.Action;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CompatManager implements Compatibility {
    private final static CompatManager instance = new CompatManager();
    private final List<Compatibility> compatibilities = new ArrayList<>();


    public static CompatManager getInstance() {
        return instance;
    }

    public CompatManager() {
        if (Bukkit.getPluginManager().getPlugin("Residence") != null) {
            compatibilities.add(new ResidenceCompact());
        }
        if (Bukkit.getPluginManager().getPlugin("Dominion") != null) {
            compatibilities.add(new DominionCompact());
        }
    }

    @Override
    public boolean check(Player player, Location location, Action action) {
        for (Compatibility compatibility : compatibilities) {
            if (compatibility.check(player, location, action)) continue;
            return false;
        }
        return true;
    }
}
