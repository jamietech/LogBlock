package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.listeners.BanListener;
import de.diddiz.LogBlock.listeners.BlockBreakLogging;
import de.diddiz.LogBlock.listeners.BlockBurnLogging;
import de.diddiz.LogBlock.listeners.BlockPlaceLogging;
import de.diddiz.LogBlock.listeners.BlockSpreadLogging;
import de.diddiz.LogBlock.listeners.ChatLogging;
import de.diddiz.LogBlock.listeners.ChestAccessLogging;
import de.diddiz.LogBlock.listeners.CreatureInteractLogging;
import de.diddiz.LogBlock.listeners.EndermenLogging;
import de.diddiz.LogBlock.listeners.ExplosionLogging;
import de.diddiz.LogBlock.listeners.FluidFlowLogging;
import de.diddiz.LogBlock.listeners.InteractLogging;
import de.diddiz.LogBlock.listeners.KillLogging;
import de.diddiz.LogBlock.listeners.LeavesDecayLogging;
import de.diddiz.LogBlock.listeners.LockedChestDecayLogging;
import de.diddiz.LogBlock.listeners.PlayerInfoLogging;
import de.diddiz.LogBlock.listeners.SignChangeLogging;
import de.diddiz.LogBlock.listeners.SnowFadeLogging;
import de.diddiz.LogBlock.listeners.SnowFormLogging;
import de.diddiz.LogBlock.listeners.StructureGrowLogging;
import de.diddiz.LogBlock.listeners.ToolListener;
import de.diddiz.LogBlock.listeners.WitherLogging;
import de.diddiz.util.MySQLConnectionPool;
import de.diddiz.worldedit.LogBlockEditSessionFactory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;

import static de.diddiz.LogBlock.config.Config.*;
import static org.bukkit.Bukkit.getPluginManager;

public class LogBlock extends JavaPlugin
{
	private static LogBlock logblock = null;
	private MySQLConnectionPool pool;
	private Consumer consumer = null;
	private CommandsHandler commandsHandler;
	private Updater updater = null;
	private Timer timer = null;
	private boolean errorAtLoading = false, noDb = false, connected = true;

	public static LogBlock getInstance() {
		return LogBlock.logblock;
	}

	public Consumer getConsumer() {
		return this.consumer;
	}

	public CommandsHandler getCommandsHandler() {
		return this.commandsHandler;
	}

	Updater getUpdater() {
		return this.updater;
	}

	@Override
	public void onLoad() {
		LogBlock.logblock = this;
		try {
			this.updater = new Updater(this);
			Config.load(this);
			this.getLogger().info("Connecting to " + Config.user + "@" + Config.url + "...");
			this.pool = new MySQLConnectionPool(Config.url, Config.user, Config.password);
			final Connection conn = this.getConnection();
			if (conn == null) {
				this.noDb = true;
				return;
			}
			conn.close();
			if (this.updater.update()) {
				Config.load(this);
			}
			this.updater.checkTables();
		} catch (final NullPointerException ex) {
			this.getLogger().log(Level.SEVERE, "Error while loading: ", ex);
		} catch (final Exception ex) {
			this.getLogger().severe("Error while loading: " + ex.getMessage());
			this.errorAtLoading = true;
			return;
		}
		this.consumer = new Consumer(this);
	}

	@Override
	public void onEnable() {
		final PluginManager pm = Bukkit.getPluginManager();
		if (this.errorAtLoading) {
			pm.disablePlugin(this);
			return;
		}
		if (this.noDb) {
			return;
		}
		if (pm.getPlugin("WorldEdit") != null) {
			LogBlockEditSessionFactory.initialize(this);
		}
		this.commandsHandler = new CommandsHandler(this);
		this.getCommand("lb").setExecutor(this.commandsHandler);
		if (Config.enableAutoClearLog && (Config.autoClearLogDelay > 0)) {
			this.getServer().getScheduler().runTaskTimerAsynchronously(this, new AutoClearLog(this), 6000, Config.autoClearLogDelay * 60 * 20);
		}
		this.getServer().getScheduler().runTaskAsynchronously(this, new DumpedLogImporter(this));
		this.registerEvents();
		if (Config.useBukkitScheduler) {
			if (this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.consumer, Config.delayBetweenRuns * 20, Config.delayBetweenRuns * 20).getTaskId() > 0) {
				this.getLogger().info("Scheduled consumer with bukkit scheduler.");
			} else {
				this.getLogger().warning("Failed to schedule consumer with bukkit scheduler. Now trying schedule with timer.");
				this.timer = new Timer();
				this.timer.scheduleAtFixedRate(this.consumer, Config.delayBetweenRuns * 1000, Config.delayBetweenRuns * 1000);
			}
		} else {
			this.timer = new Timer();
			this.timer.scheduleAtFixedRate(this.consumer, Config.delayBetweenRuns * 1000, Config.delayBetweenRuns * 1000);
			this.getLogger().info("Scheduled consumer with timer.");
		}
		this.getServer().getScheduler().runTaskAsynchronously(this, new Updater.PlayerCountChecker(this));
		for (final Tool tool : Config.toolsByType.values()) {
			if (pm.getPermission("logblock.tools." + tool.name) == null) {
				final Permission perm = new Permission("logblock.tools." + tool.name, tool.permissionDefault);
				pm.addPermission(perm);
			}
		}
		try {
			final Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (final IOException ex) {
			this.getLogger().info("Could not start metrics: " + ex.getMessage());
		}
	}

	private void registerEvents() {
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new ToolListener(this), this);
		if (Config.askRollbackAfterBan) {
			pm.registerEvents(new BanListener(this), this);
		}
		if (Config.isLogging(Logging.BLOCKPLACE)) {
			pm.registerEvents(new BlockPlaceLogging(this), this);
		}
		if (Config.isLogging(Logging.BLOCKPLACE) || Config.isLogging(Logging.LAVAFLOW) || Config.isLogging(Logging.WATERFLOW)) {
			pm.registerEvents(new FluidFlowLogging(this), this);
		}
		if (Config.isLogging(Logging.BLOCKBREAK)) {
			pm.registerEvents(new BlockBreakLogging(this), this);
		}
		if (Config.isLogging(Logging.SIGNTEXT)) {
			pm.registerEvents(new SignChangeLogging(this), this);
		}
		if (Config.isLogging(Logging.FIRE)) {
			pm.registerEvents(new BlockBurnLogging(this), this);
		}
		if (Config.isLogging(Logging.SNOWFORM)) {
			pm.registerEvents(new SnowFormLogging(this), this);
		}
		if (Config.isLogging(Logging.SNOWFADE)) {
			pm.registerEvents(new SnowFadeLogging(this), this);
		}
		if (Config.isLogging(Logging.CREEPEREXPLOSION) || Config.isLogging(Logging.TNTEXPLOSION) || Config.isLogging(Logging.GHASTFIREBALLEXPLOSION) || Config.isLogging(Logging.ENDERDRAGON) || Config.isLogging(Logging.MISCEXPLOSION)) {
			pm.registerEvents(new ExplosionLogging(this), this);
		}
		if (Config.isLogging(Logging.LEAVESDECAY)) {
			pm.registerEvents(new LeavesDecayLogging(this), this);
		}
		if (Config.isLogging(Logging.CHESTACCESS)) {
			pm.registerEvents(new ChestAccessLogging(this), this);
		}
		if (Config.isLogging(Logging.SWITCHINTERACT) || Config.isLogging(Logging.DOORINTERACT) || Config.isLogging(Logging.CAKEEAT) || Config.isLogging(Logging.DIODEINTERACT) || Config.isLogging(Logging.COMPARATORINTERACT) || Config.isLogging(Logging.NOTEBLOCKINTERACT) || Config.isLogging(Logging.PRESUREPLATEINTERACT) || Config.isLogging(Logging.TRIPWIREINTERACT) || Config.isLogging(Logging.CROPTRAMPLE)) {
			pm.registerEvents(new InteractLogging(this), this);
		}
		if (Config.isLogging(Logging.CREATURECROPTRAMPLE)) {
			pm.registerEvents(new CreatureInteractLogging(this), this);
		}
		if (Config.isLogging(Logging.KILL)) {
			pm.registerEvents(new KillLogging(this), this);
		}
		if (Config.isLogging(Logging.CHAT)) {
			pm.registerEvents(new ChatLogging(this), this);
		}
		if (Config.isLogging(Logging.ENDERMEN)) {
			pm.registerEvents(new EndermenLogging(this), this);
		}
		if (Config.isLogging(Logging.WITHER)) {
			pm.registerEvents(new WitherLogging(this), this);
		}
		if (Config.isLogging(Logging.NATURALSTRUCTUREGROW) || Config.isLogging(Logging.BONEMEALSTRUCTUREGROW)) {
			pm.registerEvents(new StructureGrowLogging(this), this);
		}
		if (Config.isLogging(Logging.GRASSGROWTH) || Config.isLogging(Logging.MYCELIUMSPREAD) || Config.isLogging(Logging.VINEGROWTH) || Config.isLogging(Logging.MUSHROOMSPREAD)) {
			pm.registerEvents(new BlockSpreadLogging(this), this);
		}
		if (Config.isLogging(Logging.LOCKEDCHESTDECAY)) {
			pm.registerEvents(new LockedChestDecayLogging(this), this);
		}
		if (Config.logPlayerInfo) {
			pm.registerEvents(new PlayerInfoLogging(this), this);
		}
	}

	@Override
	public void onDisable() {
		if (this.timer != null) {
			this.timer.cancel();
		}
		this.getServer().getScheduler().cancelTasks(this);
		if (this.consumer != null) {
			if (Config.logPlayerInfo && (this.getServer().getOnlinePlayers() != null)) {
				for (final Player player : this.getServer().getOnlinePlayers()) {
					this.consumer.queueLeave(player);
				}
			}
			if (this.consumer.getQueueSize() > 0) {
				this.getLogger().info("Waiting for consumer ...");
				int tries = 10;
				while (this.consumer.getQueueSize() > 0) {
					this.getLogger().info("Remaining queue size: " + this.consumer.getQueueSize());
					if (tries > 0) {
						this.getLogger().info("Remaining tries: " + tries);
					} else {
						this.getLogger().info("Unable to save queue to database. Trying to write to a local file.");
						try {
							this.consumer.writeToFile();
							this.getLogger().info("Successfully dumped queue.");
						} catch (final FileNotFoundException ex) {
							this.getLogger().info("Failed to write. Given up.");
							break;
						}
					}
					this.consumer.run();
					tries--;
				}
			}
		}
		if (this.pool != null) {
			this.pool.close();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (this.noDb) {
			sender.sendMessage(ChatColor.RED + "No database connected. Check your MySQL user/pw and database for typos. Start/restart your MySQL server.");
		}
		return true;
	}

	public boolean hasPermission(CommandSender sender, String permission) {
		return sender.hasPermission(permission);
	}

	public Connection getConnection() {
		try {
			final Connection conn = this.pool.getConnection();
			if (!this.connected) {
				this.getLogger().info("MySQL connection rebuild");
				this.connected = true;
			}
			return conn;
		} catch (final Exception ex) {
			if (this.connected) {
				this.getLogger().log(Level.SEVERE, "Error while fetching connection: ", ex);
				this.connected = false;
			} else {
				this.getLogger().severe("MySQL connection lost");
			}
			return null;
		}
	}

	/**
	 * @param params
	 * QueryParams that contains the needed columns (all other will be filled with default values) and the params. World is required.
	 */
	public List<BlockChange> getBlockChanges(QueryParams params) throws SQLException {
		final Connection conn = this.getConnection();
		Statement state = null;
		if (conn == null) {
			throw new SQLException("No connection");
		}
		try {
			state = conn.createStatement();
			final ResultSet rs = state.executeQuery(params.getQuery());
			final List<BlockChange> blockchanges = new ArrayList<BlockChange>();
			while (rs.next()) {
				blockchanges.add(new BlockChange(rs, params));
			}
			return blockchanges;
		} finally {
			if (state != null) {
				state.close();
			}
			conn.close();
		}
	}

	public int getCount(QueryParams params) throws SQLException {
		final Connection conn = this.getConnection();
		Statement state = null;
		if (conn == null) {
			throw new SQLException("No connection");
		}
		try {
			state = conn.createStatement();
			final QueryParams p = params.clone();
			p.needCount = true;
			final ResultSet rs = state.executeQuery(p.getQuery());
			if (!rs.next()) {
				return 0;
			}
			return rs.getInt(1);
		} finally {
			if (state != null) {
				state.close();
			}
			conn.close();
		}
	}
}
