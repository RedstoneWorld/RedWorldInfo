package de.redstoneworld.redworldinfo;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;

public class RedWorldInfo extends JavaPlugin {
    
    private String barText;
    private SimpleDateFormat barTimeFormat;
    private BarColor barColor;
    private BarStyle barStyle;
    private long barUpdateTicks;
    
    private Set<String> visibleWorlds;
    private Map<UUID, BossBar> playerBars = new HashMap<>();
    private BukkitTask barTask = null;
    private Set<UUID> barDisabled = new HashSet<>();
    private boolean barStartAtMidnight;
    
    @Override
    public void onEnable() {
        loadConfig();
        getCommand("redworldinfo").setExecutor(new RedWorldInfoCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        visibleWorlds = new HashSet<>(getConfig().getStringList("visible-worlds"));
        for (String worldName : visibleWorlds) {
            if (!"*".equals(worldName) && getServer().getWorld(worldName) == null) {
                getLogger().log(Level.WARNING, "No world found with the name " + worldName + "!");
            }
        }
        barText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bar.text"));
        try {
            barTimeFormat = new SimpleDateFormat(getConfig().getString("bar.time-format"));
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, getConfig().getString("bar.time-format") + " is not a valid time format!");
            barTimeFormat = new SimpleDateFormat("HH:mm");
        }
        barTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            barColor = BarColor.valueOf(getConfig().getString("bar.color").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, getConfig().getString("bar.color") + " is not a valid bar color setting!");
            barColor = BarColor.GREEN;
        }
        try {
            barStyle = BarStyle.valueOf(getConfig().getString("bar.style").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, getConfig().getString("bar.style") + " is not a valid bar style setting!");
            barStyle = BarStyle.SEGMENTED_12;
        }
        barUpdateTicks = getConfig().getLong("bar.update-ticks");
        barStartAtMidnight = getConfig().getBoolean("bar.start-at-midnight");
        stopTask();
        startTask();
        for (BossBar bar : playerBars.values()) {
            bar.setColor(barColor);
            bar.setStyle(barStyle);
        }
    }
    
    public boolean isVisibleInWorld(World world) {
        return visibleWorlds.contains("*") || visibleWorlds.contains(world.getName());
    }
    
    public boolean hasBar(Player player) {
        return playerBars.containsKey(player.getUniqueId());
    }
    
    public boolean hasBarDisabled(Player player) {
        return barDisabled.contains(player.getUniqueId());
    }
    
    public void showBar(Player player) {
        if (hasBarDisabled(player) || hasBar(player) || !isVisibleInWorld(player.getWorld())) {
            return;
        }
        BossBar bar = getServer().createBossBar(getTitle(player.getWorld()), barColor, barStyle);
        bar.setProgress(getProgress(player.getWorld()));
        bar.addPlayer(player);
        playerBars.put(player.getUniqueId(), bar);
        startTask();
    }
    
    public void hideBar(Player player) {
        BossBar bar = playerBars.get(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
            playerBars.remove(player.getUniqueId());
        }
    }
    
    public void disableBar(Player player) {
        hideBar(player);
        barDisabled.add(player.getUniqueId());
    }
    
    public void enableBar(Player player) {
        barDisabled.remove(player.getUniqueId());
        showBar(player);
    }
    
    private void startTask() {
        if (barTask == null) {
            barTask = getServer().getScheduler().runTaskTimer(this, () -> {
                for (Iterator<Map.Entry<UUID, BossBar>> it = playerBars.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<UUID, BossBar> entry = it.next();
                    Player p = getServer().getPlayer(entry.getKey());
                    if (p != null) {
                        World world = p.getWorld();
                        if (isVisibleInWorld(world)) {
                            entry.getValue().setTitle(getTitle(world));
                            entry.getValue().setProgress(getProgress(world));
                        } else {
                            entry.getValue().removePlayer(p);
                            it.remove();
                        }
                    } else {
                        it.remove();
                    }
                }
                if (playerBars.isEmpty()) {
                    stopTask();
                }
            }, barUpdateTicks , barUpdateTicks);
        }
    }
    
    private void stopTask() {
        if (barTask != null) {
            barTask.cancel();
            barTask = null;
        }
    }
    
    private String getTitle(World world) {
        String time = barTimeFormat.format(new Date(6 * 60 * 60 * 1000 + (long) (world.getTime() * 3.6 * 1000L)));
        
        String weather = "sun";
        if (world.hasStorm()) {
            if (world.isThundering()) {
                weather = "storm";
            } else {
                weather = "rain";
            }
        }
        
        return replace(barText,
                "world", world.getName(),
                "time", time,
                "ticks", String.valueOf(world.getTime()),
                "fulltime", String.valueOf(world.getFullTime()),
                "weather", getLang("weather." + weather));
    }
    
    private double getProgress(World world) {
        long time = world.getTime();
        if (barStartAtMidnight) {
            time += 6000;
            time %= 24000;
        }
        return time / 24000D;
    }
    
    void sendMessage(CommandSender sender, String key, String... args) {
        String text = getLang(key, args);
        if (!text.isEmpty()) {
            sender.sendMessage(text);
        }
    }
    
    String getLang(String key, String... args) {
        String lang = getConfig().getString("lang." + key, "&cUnknown language key &6" + key);
        return ChatColor.translateAlternateColorCodes('&', replace(lang, args));
    }
    
    static String replace(String text, String... args) {
        for (int i = 0; i + 1 < args.length; i+=2) {
            text = text.replace("%" + args[i] + "%", args[i + 1]);
        }
        return text;
    }
}
