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

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import me.asone.datasyncer.Action;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DominionCompact implements Compatibility {

    @Override
    public boolean check(Player player, Location location, Action action) {
        try {
            UUID uuid = player.getUniqueId();
            DominionAPI api = DominionAPI.getInstance();
            DominionDTO dominion = api.getDominion(location);

            if (dominion == null) return true;

            if (uuid.equals(dominion.getOwner())) return true;

            if (dominion.getGuestFlagValue(Flags.CONTAINER)) return true;

            MemberDTO member = api.getMember(dominion, uuid);
            return member != null && member.getFlagValue(Flags.CONTAINER);

        } catch (Exception ignored) {
            return true;
        }
    }
}
