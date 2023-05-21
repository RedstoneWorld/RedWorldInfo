package de.redstoneworld.redworldinfo;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class RedSeedInfoCommand implements CommandExecutor {
	private final RedWorldInfo plugin;

	public RedSeedInfoCommand(RedWorldInfo plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("rwm.redseedinfo.use")) {
			if (args.length > 0) {
				if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("rwm.redseedinfo.reload")) {
					plugin.loadConfig();
					sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
				} else {
					plugin.sendMessage(sender, "wrong-seed-syntax", "input", args[0]);
				}
				return true;
			}
			if (sender instanceof Player) {
				StringBuilder message = new StringBuilder();
				message.append(plugin.getLang("world-seed-prefix", args) + " ");
				World world = ((Player) sender).getWorld();
				String worldInfo = plugin.getWorldInfos(world.getName(), args);
				if (worldInfo != null) {
					if (worldInfo.contains("%seed%")) {
						String seed = String.valueOf(world.getSeed());
						message.append(worldInfo.replace("%seed%", seed));
						TextComponent hoverMessage = new TextComponent(message.toString());
						hoverMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.getLang("copy-seed"))));
						hoverMessage.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, seed));
						sender.spigot().sendMessage(hoverMessage);
					} else {
						message.append(worldInfo);
						sender.sendMessage(message.toString());
					}
				} else {
					plugin.sendMessage(sender, "no-world-seed");
					return true;
				}
				return true;
			} else {
				plugin.sendMessage(sender, "player-command", "input", "seed");
				return false;
			}
		} else {
			return false;
		}
	}
}
