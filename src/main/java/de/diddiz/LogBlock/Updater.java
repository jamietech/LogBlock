package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import static de.diddiz.LogBlock.config.Config.getLoggedWorlds;
import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.BukkitUtils.friendlyWorldname;
import static org.bukkit.Bukkit.getLogger;

class Updater
{
	private final LogBlock logblock;

	Updater(LogBlock logblock) {
		this.logblock = logblock;
	}

	boolean update() {
		final ConfigurationSection config = this.logblock.getConfig();
		if (config.getString("version").compareTo(this.logblock.getDescription().getVersion()) >= 0) {
			return false;
		}
		if (config.getString("version").compareTo("1.27") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.27 ...");
			if (Config.isLogging(Logging.CHAT)) {
				final Connection conn = this.logblock.getConnection();
				try {
					conn.setAutoCommit(true);
					final Statement st = conn.createStatement();
					st.execute("ALTER TABLE `lb-chat` ENGINE = MyISAM, ADD FULLTEXT message (message)");
					st.close();
					conn.close();
				} catch (final SQLException ex) {
					Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
					return false;
				}
			}
			config.set("version", "1.27");
		}
		if (config.getString("version").compareTo("1.30") < 0) {
			Bukkit.getLogger().info("Updating config to 1.30 ...");
			for (final String tool : config.getConfigurationSection("tools").getKeys(false)) {
				if (config.get("tools." + tool + ".permissionDefault") == null) {
					config.set("tools." + tool + ".permissionDefault", "OP");
				}
			}
			config.set("version", "1.30");
		}
		if (config.getString("version").compareTo("1.31") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.31 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				st.execute("ALTER TABLE `lb-players` ADD COLUMN lastlogin DATETIME NOT NULL, ADD COLUMN onlinetime TIME NOT NULL, ADD COLUMN ip VARCHAR(255) NOT NULL");
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.31");
		}
		if (config.getString("version").compareTo("1.32") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.32 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				st.execute("ALTER TABLE `lb-players` ADD COLUMN firstlogin DATETIME NOT NULL AFTER playername");
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.32");
		}
		if (config.getString("version").compareTo("1.40") < 0) {
			Bukkit.getLogger().info("Updating config to 1.40 ...");
			config.set("clearlog.keepLogDays", null);
			config.set("version", "1.40");
		}
		if (config.getString("version").compareTo("1.42") < 0) {
			Bukkit.getLogger().info("Updating config to 1.42 ...");
			for (final String world : config.getStringList("loggedWorlds")) {
				final File file = new File(this.logblock.getDataFolder(), BukkitUtils.friendlyWorldname(world) + ".yml");
				final YamlConfiguration wcfg = YamlConfiguration.loadConfiguration(file);
				if (wcfg.contains("logBlockCreations")) {
					wcfg.set("logging.BLOCKPLACE", wcfg.getBoolean("logBlockCreations"));
				}
				if (wcfg.contains("logBlockDestroyings")) {
					wcfg.set("logging.BLOCKBREAK", wcfg.getBoolean("logBlockDestroyings"));
				}
				if (wcfg.contains("logSignTexts")) {
					wcfg.set("logging.SIGNTEXT", wcfg.getBoolean("logSignTexts"));
				}
				if (wcfg.contains("logFire")) {
					wcfg.set("logging.FIRE", wcfg.getBoolean("logFire"));
				}
				if (wcfg.contains("logLeavesDecay")) {
					wcfg.set("logging.LEAVESDECAY", wcfg.getBoolean("logLeavesDecay"));
				}
				if (wcfg.contains("logLavaFlow")) {
					wcfg.set("logging.LAVAFLOW", wcfg.getBoolean("logLavaFlow"));
				}
				if (wcfg.contains("logWaterFlow")) {
					wcfg.set("logging.WATERFLOW", wcfg.getBoolean("logWaterFlow"));
				}
				if (wcfg.contains("logChestAccess")) {
					wcfg.set("logging.CHESTACCESS", wcfg.getBoolean("logChestAccess"));
				}
				if (wcfg.contains("logButtonsAndLevers")) {
					wcfg.set("logging.SWITCHINTERACT", wcfg.getBoolean("logButtonsAndLevers"));
				}
				if (wcfg.contains("logKills")) {
					wcfg.set("logging.KILL", wcfg.getBoolean("logKills"));
				}
				if (wcfg.contains("logChat")) {
					wcfg.set("logging.CHAT", wcfg.getBoolean("logChat"));
				}
				if (wcfg.contains("logSnowForm")) {
					wcfg.set("logging.SNOWFORM", wcfg.getBoolean("logSnowForm"));
				}
				if (wcfg.contains("logSnowFade")) {
					wcfg.set("logging.SNOWFADE", wcfg.getBoolean("logSnowFade"));
				}
				if (wcfg.contains("logDoors")) {
					wcfg.set("logging.DOORINTERACT", wcfg.getBoolean("logDoors"));
				}
				if (wcfg.contains("logCakes")) {
					wcfg.set("logging.CAKEEAT", wcfg.getBoolean("logCakes"));
				}
				if (wcfg.contains("logEndermen")) {
					wcfg.set("logging.ENDERMEN", wcfg.getBoolean("logEndermen"));
				}
				if (wcfg.contains("logExplosions")) {
					final boolean logExplosions = wcfg.getBoolean("logExplosions");
					wcfg.set("logging.TNTEXPLOSION", logExplosions);
					wcfg.set("logging.MISCEXPLOSION", logExplosions);
					wcfg.set("logging.CREEPEREXPLOSION", logExplosions);
					wcfg.set("logging.GHASTFIREBALLEXPLOSION", logExplosions);
				}
				wcfg.set("logBlockCreations", null);
				wcfg.set("logBlockDestroyings", null);
				wcfg.set("logSignTexts", null);
				wcfg.set("logExplosions", null);
				wcfg.set("logFire", null);
				wcfg.set("logLeavesDecay", null);
				wcfg.set("logLavaFlow", null);
				wcfg.set("logWaterFlow", null);
				wcfg.set("logChestAccess", null);
				wcfg.set("logButtonsAndLevers", null);
				wcfg.set("logKills", null);
				wcfg.set("logChat", null);
				wcfg.set("logSnowForm", null);
				wcfg.set("logSnowFade", null);
				wcfg.set("logDoors", null);
				wcfg.set("logCakes", null);
				wcfg.set("logEndermen", null);
				try {
					wcfg.save(file);
				} catch (final IOException ex) {
					Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				}
			}
			config.set("clearlog.keepLogDays", null);
			config.set("version", "1.42");
		}
		if (config.getString("version").compareTo("1.51") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.51 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				for (final WorldConfig wcfg : Config.getLoggedWorlds()) {
					if (wcfg.isLogging(Logging.KILL)) {
						st.execute("ALTER TABLE `" + wcfg.table + "-kills` ADD (x MEDIUMINT NOT NULL DEFAULT 0, y SMALLINT NOT NULL DEFAULT 0, z MEDIUMINT NOT NULL DEFAULT 0)");
					}
				}
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.51");
		}
		if (config.getString("version").compareTo("1.52") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.52 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				final ResultSet rs = st.executeQuery("SHOW COLUMNS FROM `lb-players` WHERE field = 'onlinetime'");
				if (rs.next() && rs.getString("Type").equalsIgnoreCase("time")) {
					st.execute("ALTER TABLE `lb-players` ADD onlinetime2 INT UNSIGNED NOT NULL");
					st.execute("UPDATE `lb-players` SET onlinetime2 = HOUR(onlinetime) * 3600 + MINUTE(onlinetime) * 60 + SECOND(onlinetime)");
					st.execute("ALTER TABLE `lb-players` DROP onlinetime");
					st.execute("ALTER TABLE `lb-players` CHANGE onlinetime2 onlinetime INT UNSIGNED NOT NULL");
				} else {
					Bukkit.getLogger().info("Column lb-players was already modified, skipping it.");
				}
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.52");
		}
		// Ensure charset for free-text fields is UTF-8
		// As this may be an expensive operation and the database default may already be UTF-8, check on a table-by-table basis before converting
		if (config.getString("version").compareTo("1.71") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.71 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				if (Config.isLogging(Logging.CHAT)) {
					final ResultSet rs = st.executeQuery("SHOW FULL COLUMNS FROM `lb-chat` WHERE field = 'message'");
					if (rs.next() && !rs.getString("Collation").substring(0,4).equalsIgnoreCase("utf8")) {
						st.execute("ALTER TABLE `lb-chat` CONVERT TO CHARSET utf8");
						Bukkit.getLogger().info("Table lb-chat modified");
					} else {
						Bukkit.getLogger().info("Table lb-chat already fine, skipping it");
					}
				}
				for (final WorldConfig wcfg : Config.getLoggedWorlds()) {
					if (wcfg.isLogging(Logging.SIGNTEXT)) {
						final ResultSet rs = st.executeQuery("SHOW FULL COLUMNS FROM `"+wcfg.table+"-sign` WHERE field = 'signtext'");
						if (rs.next() && !rs.getString("Collation").substring(0,4).equalsIgnoreCase("utf8")) {
							st.execute("ALTER TABLE `"+wcfg.table+"-sign` CONVERT TO CHARSET utf8");
							Bukkit.getLogger().info("Table "+wcfg.table+"-sign modified");
						} else {
							Bukkit.getLogger().info("Table "+wcfg.table+"-sign already fine, skipping it");
						}
					}
				}
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.71");
		}
		if (config.getString("version").compareTo("1.81") < 0) {
			Bukkit.getLogger().info("Updating tables to 1.81 ...");
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				for (final WorldConfig wcfg : Config.getLoggedWorlds()) {
					if (wcfg.isLogging(Logging.CHESTACCESS)) {
						st.execute("ALTER TABLE `"+wcfg.table+"-chest` CHANGE itemdata itemdata SMALLINT NOT NULL");
						Bukkit.getLogger().info("Table "+wcfg.table+"-chest modified");
					}
				}
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
				return false;
			}
			config.set("version", "1.81");
		}
		
		this.logblock.saveConfig();
		return true;
	}

	void checkTables() throws SQLException {
		final Connection conn = this.logblock.getConnection();
		if (conn == null) {
			throw new SQLException("No connection");
		}
		final Statement state = conn.createStatement();
		final DatabaseMetaData dbm = conn.getMetaData();
		conn.setAutoCommit(true);
		Updater.createTable(dbm, state, "lb-players", "(playerid INT UNSIGNED NOT NULL AUTO_INCREMENT, playername varchar(32) NOT NULL, firstlogin DATETIME NOT NULL, lastlogin DATETIME NOT NULL, onlinetime INT UNSIGNED NOT NULL, ip varchar(255) NOT NULL, PRIMARY KEY (playerid), UNIQUE (playername))");
		// Players table must not be empty or inserts won't work - bug #492
		final ResultSet rs = state.executeQuery("SELECT NULL FROM `lb-players` LIMIT 1;");
		if (!rs.next()) {
			state.execute("INSERT IGNORE INTO `lb-players` (playername) VALUES ('dummy_record')");
		}
		if (Config.isLogging(Logging.CHAT)) {
			Updater.createTable(dbm, state, "lb-chat", "(id INT UNSIGNED NOT NULL AUTO_INCREMENT, date DATETIME NOT NULL, playerid INT UNSIGNED NOT NULL, message VARCHAR(255) NOT NULL, PRIMARY KEY (id), KEY playerid (playerid), FULLTEXT message (message)) ENGINE=MyISAM DEFAULT CHARSET utf8");
		}
		for (final WorldConfig wcfg : Config.getLoggedWorlds()) {
			Updater.createTable(dbm, state, wcfg.table, "(id INT UNSIGNED NOT NULL AUTO_INCREMENT, date DATETIME NOT NULL, playerid INT UNSIGNED NOT NULL, replaced TINYINT UNSIGNED NOT NULL, type TINYINT UNSIGNED NOT NULL, data TINYINT UNSIGNED NOT NULL, x MEDIUMINT NOT NULL, y SMALLINT UNSIGNED NOT NULL, z MEDIUMINT NOT NULL, PRIMARY KEY (id), KEY coords (x, z, y), KEY date (date), KEY playerid (playerid))");
			Updater.createTable(dbm, state, wcfg.table + "-sign", "(id INT UNSIGNED NOT NULL, signtext VARCHAR(255) NOT NULL, PRIMARY KEY (id)) DEFAULT CHARSET utf8");
			Updater.createTable(dbm, state, wcfg.table + "-chest", "(id INT UNSIGNED NOT NULL, itemtype SMALLINT UNSIGNED NOT NULL, itemamount SMALLINT NOT NULL, itemdata SMALLINT NOT NULL, PRIMARY KEY (id))");
			if (wcfg.isLogging(Logging.KILL)) {
				Updater.createTable(dbm, state, wcfg.table + "-kills", "(id INT UNSIGNED NOT NULL AUTO_INCREMENT, date DATETIME NOT NULL, killer INT UNSIGNED, victim INT UNSIGNED NOT NULL, weapon SMALLINT UNSIGNED NOT NULL, x MEDIUMINT NOT NULL, y SMALLINT NOT NULL, z MEDIUMINT NOT NULL, PRIMARY KEY (id))");
			}
		}
		state.close();
		conn.close();
	}

	private static void createTable(DatabaseMetaData dbm, Statement state, String table, String query) throws SQLException {
		if (!dbm.getTables(null, null, table, null).next()) {
			Bukkit.getLogger().log(Level.INFO, "Creating table " + table + ".");
			state.execute("CREATE TABLE `" + table + "` " + query);
			if (!dbm.getTables(null, null, table, null).next()) {
				throw new SQLException("Table " + table + " not found and failed to create");
			}
		}
	}

	public static class PlayerCountChecker implements Runnable {

		private final LogBlock logblock;

		public PlayerCountChecker(LogBlock logblock) {
			this.logblock = logblock;
		}

		@Override
		public void run() {
			final Connection conn = this.logblock.getConnection();
			try {
				conn.setAutoCommit(true);
				final Statement st = conn.createStatement();
				final ResultSet rs = st.executeQuery("SELECT auto_increment FROM information_schema.columns AS col join information_schema.tables AS tab ON (col.table_schema=tab.table_schema AND col.table_name=tab.table_name) WHERE col.table_name = 'lb-players' AND col.column_name = 'playerid' AND col.data_type = 'smallint' AND col.table_schema = DATABASE() AND auto_increment > 65000;");
				if (rs.next()) {
					for (int i = 0; i < 6; i++) {
						this.logblock.getLogger().warning("Your server reached 65000 players. You should soon update your database table schema - see FAQ: https://github.com/LogBlock/LogBlock/wiki/FAQ#logblock-your-server-reached-65000-players-");
					}
				}
				st.close();
				conn.close();
			} catch (final SQLException ex) {
				this.logblock.getLogger().log(Level.SEVERE, "[Updater] Error: ", ex);
			}
		}
	}
}
