/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyWolf
 *
 * MyWolf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWolf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWolf. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyWolf.util;

import de.Keyle.MyWolf.MyWolfPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class MyWolfPermissions
{
    private static Object Permissions;

    public enum PermissionsType
    {
        NONE, bPermissions, PermissionsEX, BukkitPermissions//, Permissions, GroupManager
    }

    private static PermissionsType PermissionsMode = PermissionsType.NONE;


    public static boolean has(Player player, String node)
    {
        if (player.isOp())
        {
            return true;
        }
        else if (PermissionsMode == PermissionsType.NONE || Permissions == null)
        {
            return true;
        }
        /*
        else if (PermissionsMode == PermissionsType.Permissions && Permissions instanceof PermissionHandler)
        {
            return ((PermissionHandler) Permissions).has(player, node);
        }
        else if (PermissionsMode == PermissionsType.GroupManager && Permissions instanceof GroupManager)
        {
            return ((GroupManager) Permissions).getWorldsHolder().getWorldPermissions(player).has(player, node);
        }
        */
        else if (PermissionsMode == PermissionsType.PermissionsEX && Permissions instanceof PermissionManager)
        {
            return ((PermissionManager) Permissions).has(player, node);
        }
        else if (PermissionsMode == PermissionsType.BukkitPermissions || PermissionsMode == PermissionsType.bPermissions)
        {
            player.hasPermission(node);
        }
        return false;

    }

    public static boolean has(String player, String node)
    {
        OfflinePlayer offlinePlayer = MyWolfPlugin.getPlugin().getServer().getOfflinePlayer(player);
        PermissibleBase perm = new PermissibleBase(offlinePlayer);

        if (PermissionsMode == PermissionsType.NONE || Permissions == null || player == null || offlinePlayer.isOp())
        {
            return true;
        }
        else if (PermissionsMode == PermissionsType.PermissionsEX && Permissions instanceof PermissionManager)
        {
            return ((PermissionManager) Permissions).has(player, node, MyWolfPlugin.getPlugin().getServer().getWorlds().get(0).getName());
        }
        else if (PermissionsMode == PermissionsType.BukkitPermissions || PermissionsMode == PermissionsType.bPermissions)
        {
            perm.hasPermission(node);
        }
        return false;
    }

    public static void setup(PermissionsType pt)
    {
        PermissionsMode = pt;
    }

    public static void setup()
    {
        Plugin p;
        /*
        p = MyWolfPlugin.Plugin.getServer().getPluginManager().getPlugin("GroupManager");
        if (p != null && PermissionsMode == PermissionsType.NONE)
        {
            PermissionsMode = PermissionsType.GroupManager;
            Permissions = p;
            MyWolfUtil.Log.info("[MyWolf] GroupManager integration enabled!");
            return;
        }
        */
        p = MyWolfPlugin.getPlugin().getServer().getPluginManager().getPlugin("bPermissions");
        if (p != null && PermissionsMode == PermissionsType.NONE)
        {
            PermissionsMode = PermissionsType.bPermissions;
            Permissions = null;
            MyWolfUtil.getLogger().info("[MyWolf] bPermissions integration enabled!");
            return;
        }

        p = MyWolfPlugin.getPlugin().getServer().getPluginManager().getPlugin("PermissionsEx");
        if (p != null && PermissionsMode == PermissionsType.NONE)
        {
            PermissionsMode = PermissionsType.PermissionsEX;
            Permissions = PermissionsEx.getPermissionManager();
            MyWolfUtil.getLogger().info("[MyWolf] PermissionsEX integration enabled!");
            return;
        }
        /*
        p = MyWolfPlugin.Plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (p != null && PermissionsMode == PermissionsType.NONE)
        {
            PermissionsMode = PermissionsType.Permissions;
            Permissions = ((Permissions) p).getHandler();
            MyWolfUtil.Log.info("[MyWolf] Permissions integration enabled!");
            return;
        }
        */
        MyWolfUtil.getLogger().info("[MyWolf] No permissions system fund!");
    }
}
