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
			StringBuilder sb = new StringBuilder();
			sb.append(plugin.getLang("seed-info", args) + " ");
			World world = ((Player) sender).getWorld();
			String infoLang = plugin.getWorldInfos(world.getName(), args);
			if (infoLang != null) {
				sb.append(infoLang);
				sender.sendMessage(sb.toString());
			} else {
				plugin.sendMessage(sender, "no-world-seed");
				return true;
			}
			if (plugin.isSeedWorld(world)) {
				String seed = String.valueOf(world.getSeed());
				TextComponent message = new TextComponent(ChatColor.GRAY + "Seed: " + ChatColor.DARK_GREEN + seed);
				message.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Seed kopieren")));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, seed));
				sender.spigot().sendMessage(message);
			}
			return true;
		} else {
			plugin.sendMessage(sender, "player-command", "input", args[0]);
			return false;
		}
	}
}
