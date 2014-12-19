/* Multichat - A bukkit chat plugin that allows you to use 
 * Copyright (C) 2014 Fernando
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 3.0 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */

package me.ferry.bukkit.plugins.multichat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.util.StringUtil;

/**
 *
 * @author ferrybig
 */
public class MultiChatCommand implements TabExecutor {

    private final MultiChat plugin;
    private static final List<String> SUBCOMMANDS = ImmutableList.of(
            "refresh", "reformat", "reload", "reloadall", "debug");

    public MultiChatCommand(MultiChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
            String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS,
                    new ArrayList<String>());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String label, String[] args) {
        if (!cmd.testPermission(sender)) {
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "debug": {
                for (Map.Entry<UUID, String> entry
                        : plugin.chat.getFormat().entrySet()) {
                    final String name = plugin.
                            getServer().
                            getPlayer(entry.getKey()).
                            getName();
                    this.plugin.sendMessage(sender,
                            name + ": " + (entry.getValue().replace(ChatColor.COLOR_CHAR, '&')));
                }
            }
            return true;
            case "refresh": {
                plugin.sendMessage(sender, "Resyncing chat-formats...");
                plugin.chat.pushUpdate();
                plugin.sendMessage(sender, "Resynced chat-formats!");
            }
            return true;
            case "reformat": {
                plugin.sendMessage(sender, "Regenerating chat-formats...");
                plugin.chat.updateAll();
                plugin.sendMessage(sender, "Regenerated chat-formats!");
            }
            return true;
            case "reload": {
                plugin.sendMessage(sender, "Reloading config...");
                try {
                    plugin.reloadConfigWithErrors();
                } catch (IOException | InvalidConfigurationException ex) {
                    ex.printStackTrace(System.out);
                    plugin.sendMessage(sender, "Problem with configuration:");
                    plugin.sendMessage(sender, ex.getMessage());
                    plugin.sendMessage(sender, "See console for stacktrace!");
                    return true;
                }
                plugin.sendMessage(sender, "Reloaded config!");
            }
            return true;
            case "reloadall": {
                plugin.sendMessage(sender, "Reloading config...");
                try {
                    plugin.reloadConfigWithErrors();
                } catch (IOException | InvalidConfigurationException ex) {
                    ex.printStackTrace(System.out);
                    plugin.sendMessage(sender, ex.getMessage());
                    return true;
                }
                plugin.sendMessage(sender, "Reloaded config!");
                plugin.sendMessage(sender, "Regenerating chat-formats...");
                plugin.chat.updateAll();
                plugin.sendMessage(sender, "Regenerated chat-formats!");
                plugin.sendMessage(sender, "Resyncing chat-formats...");
                plugin.chat.pushUpdate();
                plugin.sendMessage(sender, "Resynced chat-formats!");
            }
            return true;
        }
        return false;
    }
}
