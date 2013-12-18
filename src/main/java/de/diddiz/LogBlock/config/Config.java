package de.diddiz.LogBlock.config;

import de.diddiz.LogBlock.*;
import de.diddiz.util.BukkitUtils;
import de.diddiz.util.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.DataFormatException;

import static de.diddiz.util.BukkitUtils.friendlyWorldname;
import static de.diddiz.util.Utils.parseTimeSpec;
import static org.bukkit.Bukkit.*;

public class Config
{
	private static LoggingEnabledMapping superWorldConfig;
	private static Map<String, WorldConfig> worldConfigs;
	public static String url, user, password;
	public static int delayBetweenRuns, forceToProcessAtLeast, timePerRun;
	public static boolean fireCustomEvents;
	public static boolean useBukkitScheduler;
	public static int queueWarningSize;
	public static boolean enableAutoClearLog;
	public static List<String> autoClearLog;
	public static int autoClearLogDelay;
	public static boolean dumpDeletedLog;
	public static boolean logCreeperExplosionsAsPlayerWhoTriggeredThese, logPlayerInfo;
	public static LogKillsLevel logKillsLevel;
	public static Set<Integer> dontRollback, replaceAnyway;
	public static int rollbackMaxTime, rollbackMaxArea;
	public static Map<String, Tool> toolsByName;
	public static Map<Integer, Tool> toolsByType;
	public static int defaultDist, defaultTime;
	public static int linesPerPage, linesLimit;
	public static boolean askRollbacks, askRedos, askClearLogs, askClearLogAfterRollback, askRollbackAfterBan;
	public static String banPermission;
	public static Set<Integer> hiddenBlocks;
	public static Set<String> hiddenPlayers;
	public static Set<String> ignoredChat;
	public static SimpleDateFormat formatter;
	public static boolean safetyIdCheck;
	public static boolean logEnvironmentalKills;

	public static enum LogKillsLevel
	{
		PLAYERS, MONSTERS, ANIMALS;
	}

	public static void load(LogBlock logblock) throws DataFormatException, IOException {
		final ConfigurationSection config = logblock.getConfig();
		final Map<String, Object> def = new HashMap<String, Object>();
		def.put("version", logblock.getDescription().getVersion());
		final List<String> worldNames = new ArrayList<String>();
		for (final World world : Bukkit.getWorlds()) {
			worldNames.add(world.getName());
		}
		if (worldNames.isEmpty()) {
			worldNames.add("world");
			worldNames.add("world_nether");
			worldNames.add("world_the_end");
		}
		def.put("loggedWorlds", worldNames);
		def.put("mysql.host", "localhost");
		def.put("mysql.port", 3306);
		def.put("mysql.database", "minecraft");
		def.put("mysql.user", "username");
		def.put("mysql.password", "pass");
		def.put("consumer.delayBetweenRuns", 2);
		def.put("consumer.forceToProcessAtLeast", 200);
		def.put("consumer.timePerRun", 1000);
		def.put("consumer.fireCustomEvents", false);
		def.put("consumer.useBukkitScheduler", true);
		def.put("consumer.queueWarningSize", 1000);
		def.put("clearlog.dumpDeletedLog", false);
		def.put("clearlog.enableAutoClearLog", false);
		def.put("clearlog.auto", Arrays.asList("world \"world\" before 365 days all", "world \"world\" player lavaflow waterflow leavesdecay before 7 days all", "world world_nether before 365 days all", "world world_nether player lavaflow before 7 days all"));
		def.put("clearlog.autoClearLogDelay", "6h");
		def.put("logging.logCreeperExplosionsAsPlayerWhoTriggeredThese", false);
		def.put("logging.logKillsLevel", "PLAYERS");
		def.put("logging.logEnvironmentalKills", false);
		def.put("logging.logPlayerInfo", false);
		def.put("logging.hiddenPlayers", new ArrayList<String>());
		def.put("logging.hiddenBlocks", Arrays.asList(0));
		def.put("logging.ignoredChat", Arrays.asList("/register", "/login"));
		def.put("rollback.dontRollback", Arrays.asList(10, 11, 46, 51));
		def.put("rollback.replaceAnyway", Arrays.asList(8, 9, 10, 11, 51));
		def.put("rollback.maxTime", "2 days");
		def.put("rollback.maxArea", 50);
		def.put("lookup.defaultDist", 20);
		def.put("lookup.defaultTime", "30 minutes");
		def.put("lookup.linesPerPage", 15);
		def.put("lookup.linesLimit", 1500);
		try {
			Config.formatter = new SimpleDateFormat(config.getString("lookup.dateFormat", "MM-dd HH:mm:ss"));
		} catch (final IllegalArgumentException e) {
			throw new DataFormatException("Invalid specification for  date format, please see http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html : " + e.getMessage());
		}
		def.put("lookup.dateFormat", "MM-dd HH:mm:ss");
		def.put("questioner.askRollbacks", true);
		def.put("questioner.askRedos", true);
		def.put("questioner.askClearLogs", true);
		def.put("questioner.askClearLogAfterRollback", true);
		def.put("questioner.askRollbackAfterBan", false);
		def.put("questioner.banPermission", "mcbans.ban.local");
		def.put("tools.tool.aliases", Arrays.asList("t"));
		def.put("tools.tool.leftClickBehavior", "NONE");
		def.put("tools.tool.rightClickBehavior", "TOOL");
		def.put("tools.tool.defaultEnabled", true);
		def.put("tools.tool.item", 270);
		def.put("tools.tool.canDrop", true);
		def.put("tools.tool.params", "area 0 all sum none limit 15 desc silent");
		def.put("tools.tool.mode", "LOOKUP");
		def.put("tools.tool.permissionDefault", "OP");
		def.put("tools.toolblock.aliases", Arrays.asList("tb"));
		def.put("tools.toolblock.leftClickBehavior", "TOOL");
		def.put("tools.toolblock.rightClickBehavior", "BLOCK");
		def.put("tools.toolblock.defaultEnabled", true);
		def.put("tools.toolblock.item", 7);
		def.put("tools.toolblock.canDrop", false);
		def.put("tools.toolblock.params", "area 0 all sum none limit 15 desc silent");
		def.put("tools.toolblock.mode", "LOOKUP");
		def.put("tools.toolblock.permissionDefault", "OP");
		def.put("safety.id.check", true);
		for (final Entry<String, Object> e : def.entrySet()) {
			if (!config.contains(e.getKey())) {
				config.set(e.getKey(), e.getValue());
			}
		}
		logblock.saveConfig();
		Config.url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + Config.getStringIncludingInts(config, "mysql.database") + "?useUnicode=true&characterEncoding=utf-8";
		Config.user = Config.getStringIncludingInts(config, "mysql.user");
		Config.password = Config.getStringIncludingInts(config, "mysql.password");
		Config.delayBetweenRuns = config.getInt("consumer.delayBetweenRuns", 2);
		Config.forceToProcessAtLeast = config.getInt("consumer.forceToProcessAtLeast", 0);
		Config.timePerRun = config.getInt("consumer.timePerRun", 1000);
		Config.fireCustomEvents = config.getBoolean("consumer.fireCustomEvents", false);
		Config.useBukkitScheduler = config.getBoolean("consumer.useBukkitScheduler", true);
		Config.queueWarningSize = config.getInt("consumer.queueWarningSize", 1000);
		Config.enableAutoClearLog = config.getBoolean("clearlog.enableAutoClearLog");
		Config.autoClearLog = config.getStringList("clearlog.auto");
		Config.dumpDeletedLog = config.getBoolean("clearlog.dumpDeletedLog", false);
		Config.autoClearLogDelay = Utils.parseTimeSpec(config.getString("clearlog.autoClearLogDelay").split(" "));
		Config.logCreeperExplosionsAsPlayerWhoTriggeredThese = config.getBoolean("logging.logCreeperExplosionsAsPlayerWhoTriggeredThese", false);
		Config.logPlayerInfo = config.getBoolean("logging.logPlayerInfo", true);
		try {
			Config.logKillsLevel = LogKillsLevel.valueOf(config.getString("logging.logKillsLevel").toUpperCase());
		} catch (final IllegalArgumentException ex) {
			throw new DataFormatException("logging.logKillsLevel doesn't appear to be a valid log level. Allowed are 'PLAYERS', 'MONSTERS' and 'ANIMALS'");
		}
		Config.logEnvironmentalKills = config.getBoolean("logging.logEnvironmentalKills", false);
		Config.hiddenPlayers = new HashSet<String>();
		for (final String playerName : config.getStringList("logging.hiddenPlayers")) {
			Config.hiddenPlayers.add(playerName.toLowerCase().trim());
		}
		Config.hiddenBlocks = new HashSet<Integer>();
		for (final Object blocktype : config.getList("logging.hiddenBlocks")) {
			final Material mat = Material.matchMaterial(String.valueOf(blocktype));
			if (mat != null) {
				Config.hiddenBlocks.add(mat.getId());
			} else {
				throw new DataFormatException("Not a valid material: '" + blocktype + "'");
			}
		}
		Config.ignoredChat = new HashSet<String>();
		for (final String chatCommand : config.getStringList("logging.ignoredChat")) {
			Config.ignoredChat.add(chatCommand);
		}
		Config.dontRollback = new HashSet<Integer>(config.getIntegerList("rollback.dontRollback"));
		Config.replaceAnyway = new HashSet<Integer>(config.getIntegerList("rollback.replaceAnyway"));
		Config.rollbackMaxTime = Utils.parseTimeSpec(config.getString("rollback.maxTime").split(" "));
		Config.rollbackMaxArea = config.getInt("rollback.maxArea", 50);
		Config.defaultDist = config.getInt("lookup.defaultDist", 20);
		Config.defaultTime = Utils.parseTimeSpec(config.getString("lookup.defaultTime").split(" "));
		Config.linesPerPage = config.getInt("lookup.linesPerPage", 15);
		Config.linesLimit = config.getInt("lookup.linesLimit", 1500);
		Config.askRollbacks = config.getBoolean("questioner.askRollbacks", true);
		Config.askRedos = config.getBoolean("questioner.askRedos", true);
		Config.askClearLogs = config.getBoolean("questioner.askClearLogs", true);
		Config.askClearLogAfterRollback = config.getBoolean("questioner.askClearLogAfterRollback", true);
		Config.askRollbackAfterBan = config.getBoolean("questioner.askRollbackAfterBan", false);
		Config.safetyIdCheck = config.getBoolean("safety.id.check", true);
		Config.banPermission = config.getString("questioner.banPermission");
		final List<Tool> tools = new ArrayList<Tool>();
		final ConfigurationSection toolsSec = config.getConfigurationSection("tools");
		for (final String toolName : toolsSec.getKeys(false)) {
			try {
				final ConfigurationSection tSec = toolsSec.getConfigurationSection(toolName);
				final List<String> aliases = tSec.getStringList("aliases");
				final ToolBehavior leftClickBehavior = ToolBehavior.valueOf(tSec.getString("leftClickBehavior").toUpperCase());
				final ToolBehavior rightClickBehavior = ToolBehavior.valueOf(tSec.getString("rightClickBehavior").toUpperCase());
				final boolean defaultEnabled = tSec.getBoolean("defaultEnabled", false);
				final int item = tSec.getInt("item", 0);
				final boolean canDrop = tSec.getBoolean("canDrop", false);
				final QueryParams params = new QueryParams(logblock);
				params.prepareToolQuery = true;
				params.parseArgs(Bukkit.getConsoleSender(), Arrays.asList(tSec.getString("params").split(" ")));
				final ToolMode mode = ToolMode.valueOf(tSec.getString("mode").toUpperCase());
				final PermissionDefault pdef = PermissionDefault.valueOf(tSec.getString("permissionDefault").toUpperCase());
				tools.add(new Tool(toolName, aliases, leftClickBehavior, rightClickBehavior, defaultEnabled, item, canDrop, params, mode, pdef));
			} catch (final Exception ex) {
				Bukkit.getLogger().log(Level.WARNING, "Error at parsing tool '" + toolName + "': ", ex);
			}
		}
		Config.toolsByName = new HashMap<String, Tool>();
		Config.toolsByType = new HashMap<Integer, Tool>();
		for (final Tool tool : tools) {
			Config.toolsByType.put(tool.item, tool);
			Config.toolsByName.put(tool.name.toLowerCase(), tool);
			for (final String alias : tool.aliases) {
				Config.toolsByName.put(alias, tool);
			}
		}
		final List<String> loggedWorlds = config.getStringList("loggedWorlds");
		Config.worldConfigs = new HashMap<String, WorldConfig>();
		if (loggedWorlds.isEmpty()) {
			throw new DataFormatException("No worlds configured");
		}
		for (final String world : loggedWorlds) {
			Config.worldConfigs.put(world, new WorldConfig(new File(logblock.getDataFolder(), BukkitUtils.friendlyWorldname(world) + ".yml")));
		}
		Config.superWorldConfig = new LoggingEnabledMapping();
		for (final WorldConfig wcfg : Config.worldConfigs.values()) {
			for (final Logging l : Logging.values()) {
				if (wcfg.isLogging(l)) {
					Config.superWorldConfig.setLogging(l, true);
				}
			}
		}
	}

	private static String getStringIncludingInts(ConfigurationSection cfg, String key) {
		String str = cfg.getString(key);
		if (str == null) {
			str = String.valueOf(cfg.getInt(key));
		}
		if (str == null) {
			str = "No value set for '" + key + "'";
		}
		return str;
	}

	public static boolean isLogging(World world, Logging l) {
		final WorldConfig wcfg = Config.worldConfigs.get(world.getName());
		return (wcfg != null) && wcfg.isLogging(l);
	}

	public static boolean isLogging(String worldName, Logging l) {
		final WorldConfig wcfg = Config.worldConfigs.get(worldName);
		return (wcfg != null) && wcfg.isLogging(l);
	}

	public static boolean isLogged(World world) {
		return Config.worldConfigs.containsKey(world.getName());
	}

	public static WorldConfig getWorldConfig(World world) {
		return Config.worldConfigs.get(world.getName());
	}

	public static WorldConfig getWorldConfig(String world) {
		return Config.worldConfigs.get(world);
	}

	public static boolean isLogging(Logging l) {
		return Config.superWorldConfig.isLogging(l);
	}

	public static Collection<WorldConfig> getLoggedWorlds() {
		return Config.worldConfigs.values();
	}
}

class LoggingEnabledMapping
{
	private final boolean[] logging = new boolean[Logging.length];

	public void setLogging(Logging l, boolean enabled) {
		this.logging[l.ordinal()] = enabled;
	}

	public boolean isLogging(Logging l) {
		return this.logging[l.ordinal()];
	}
}
