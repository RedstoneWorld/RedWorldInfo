package de.redstoneworld.redworldinfo;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedWorldInfoCommand implements CommandExecutor {
	private final RedWorldInfo plugin;

	public RedWorldInfoCommand(RedWorldInfo plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("rwm.redworldinfo.use")) {
			if (args.length > 0) {
				if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("rwm.redworldinfo.reload")) {
					plugin.loadConfig();
					sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
				} else if ("on".equalsIgnoreCase(args[0])) {
					if (sender instanceof Player) {
						if (plugin.isVisibleInWorld(((Player) sender).getWorld())) {
							plugin.enableBar((Player) sender);
							plugin.sendMessage(sender, "bar-enabled");
						} else {
							plugin.sendMessage(sender, "wrong-world", "world", ((Player) sender).getWorld().getName());
						}
					} else {
						plugin.sendMessage(sender, "player-command", "input", args[0]);
					}
				} else if ("off".equalsIgnoreCase(args[0])) {
					if (sender instanceof Player) {
						if (plugin.isVisibleInWorld(((Player) sender).getWorld())) {
							plugin.disableBar((Player) sender);
							plugin.sendMessage(sender, "bar-disabled");
						} else {
							plugin.sendMessage(sender, "wrong-world", "world", ((Player) sender).getWorld().getName());
						}
					} else {
						plugin.sendMessage(sender, "player-command", "input", args[0]);
					}
				} else {
					plugin.sendMessage(sender, "wrong-syntax", "input", args[0]);
				}
				return true;
			}
			if (sender instanceof Player) {
				if (plugin.isVisibleInWorld(((Player) sender).getWorld())) {
					if (plugin.hasBarDisabled((Player) sender)) {
						plugin.enableBar((Player) sender);
						plugin.sendMessage(sender, "bar-enabled");
					} else {
						plugin.disableBar((Player) sender);
						plugin.sendMessage(sender, "bar-disabled");
					}
				} else {
					plugin.sendMessage(sender, "wrong-world", "world", ((Player) sender).getWorld().getName());
				}
				return true;
			} else {
				plugin.sendMessage(sender, "player-command", "input", "worldinfo");
				return false;
			}
		} else {
			return false;
		}
	}
}
