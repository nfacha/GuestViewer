package dev.facha.guestViewer.commands;

import dev.facha.guestViewer.GuestViewer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuestViewerCommand implements CommandExecutor, TabCompleter {
    private final GuestViewer plugin;

    public GuestViewerCommand(GuestViewer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guestviewer.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "GuestViewer v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Use /guestviewer reload to reload the configuration.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            sender.sendMessage(ChatColor.GREEN + "GuestViewer configuration reloaded!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command. Use /guestviewer reload to reload the configuration.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }
        return new ArrayList<>();
    }
} 