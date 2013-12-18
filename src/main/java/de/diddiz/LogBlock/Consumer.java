package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.events.BlockChangePreLogEvent;
import de.diddiz.util.BukkitUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static de.diddiz.LogBlock.config.Config.*;
import static de.diddiz.util.BukkitUtils.*;
import static org.bukkit.Bukkit.getLogger;

public class Consumer extends TimerTask
{
	private final Queue<Row> queue = new LinkedBlockingQueue<Row>();
	private final Set<String> failedPlayers = new HashSet<String>();
	private final LogBlock logblock;
	private final Map<String, Integer> playerIds = new HashMap<String, Integer>();
	private final Lock lock = new ReentrantLock();

	Consumer(LogBlock logblock) {
		this.logblock = logblock;
		try {
			Class.forName("PlayerLeaveRow");
		} catch (final ClassNotFoundException ex) {
		}
	}

	/**
	 * Logs any block change. Don't try to combine broken and placed blocks. Queue two block changes or use the queueBLockReplace methods.
	 */
	public void queueBlock(String playerName, Location loc, int typeBefore, int typeAfter, byte data) {
		this.queueBlock(playerName, loc, typeBefore, typeAfter, data, null, null);
	}

	/**
	 * Logs a block break. The type afterwards is assumed to be o (air).
	 *
	 * @param before
	 * Blockstate of the block before actually being destroyed.
	 */
	public void queueBlockBreak(String playerName, BlockState before) {
		this.queueBlockBreak(playerName, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getTypeId(), before.getRawData());
	}

	/**
	 * Logs a block break. The block type afterwards is assumed to be o (air).
	 */
	public void queueBlockBreak(String playerName, Location loc, int typeBefore, byte dataBefore) {
		this.queueBlock(playerName, loc, typeBefore, 0, dataBefore);
	}

	/**
	 * Logs a block place. The block type before is assumed to be o (air).
	 *
	 * @param after
	 * Blockstate of the block after actually being placed.
	 */
	public void queueBlockPlace(String playerName, BlockState after) {
		this.queueBlockPlace(playerName, new Location(after.getWorld(), after.getX(), after.getY(), after.getZ()), after.getBlock().getTypeId(), after.getBlock().getData());
	}

	/**
	 * Logs a block place. The block type before is assumed to be o (air).
	 */
	public void queueBlockPlace(String playerName, Location loc, int type, byte data) {
		this.queueBlock(playerName, loc, 0, type, data);
	}

	/**
	 * @param before
	 * Blockstate of the block before actually being destroyed.
	 * @param after
	 * Blockstate of the block after actually being placed.
	 */
	public void queueBlockReplace(String playerName, BlockState before, BlockState after) {
		this.queueBlockReplace(playerName, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getTypeId(), before.getRawData(), after.getTypeId(), after.getRawData());
	}

	/**
	 * @param before
	 * Blockstate of the block before actually being destroyed.
	 */
	public void queueBlockReplace(String playerName, BlockState before, int typeAfter, byte dataAfter) {
		this.queueBlockReplace(playerName, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getTypeId(), before.getRawData(), typeAfter, dataAfter);
	}

	/**
	 * @param after
	 * Blockstate of the block after actually being placed.
	 */
	public void queueBlockReplace(String playerName, int typeBefore, byte dataBefore, BlockState after) {
		this.queueBlockReplace(playerName, new Location(after.getWorld(), after.getX(), after.getY(), after.getZ()), typeBefore, dataBefore, after.getTypeId(), after.getRawData());
	}

	public void queueBlockReplace(String playerName, Location loc, int typeBefore, byte dataBefore, int typeAfter, byte dataAfter) {
		if ((dataBefore == 0) && (typeBefore != typeAfter)) {
			this.queueBlock(playerName, loc, typeBefore, typeAfter, dataAfter);
		} else {
			this.queueBlockBreak(playerName, loc, typeBefore, dataBefore);
			this.queueBlockPlace(playerName, loc, typeAfter, dataAfter);
		}
	}

	/**
	 * @param container
	 * The respective container. Must be an instance of an InventoryHolder.
	 */
	public void queueChestAccess(String playerName, BlockState container, short itemType, short itemAmount, short itemData) {
		if (!(container instanceof InventoryHolder)) {
			return;
		}
		this.queueChestAccess(playerName, new Location(container.getWorld(), container.getX(), container.getY(), container.getZ()), container.getTypeId(), itemType, itemAmount, itemData);
	}

	/**
	 * @param type
	 * Type id of the container.
	 */
	public void queueChestAccess(String playerName, Location loc, int type, short itemType, short itemAmount, short itemData) {
		this.queueBlock(playerName, loc, type, type, (byte)0, null, new ChestAccess(itemType, itemAmount, itemData));
	}

	/**
	 * Logs a container block break. The block type before is assumed to be o (air). All content is assumed to be taken.
	 *
	 * @param container
	 * Must be an instance of InventoryHolder
	 */
	public void queueContainerBreak(String playerName, BlockState container) {
		if (!(container instanceof InventoryHolder)) {
			return;
		}
		this.queueContainerBreak(playerName, new Location(container.getWorld(), container.getX(), container.getY(), container.getZ()), container.getTypeId(), container.getRawData(), ((InventoryHolder)container).getInventory());
	}

	/**
	 * Logs a container block break. The block type before is assumed to be o (air). All content is assumed to be taken.
	 */
	public void queueContainerBreak(String playerName, Location loc, int type, byte data, Inventory inv) {
		final ItemStack[] items = BukkitUtils.compressInventory(inv.getContents());
		for (final ItemStack item : items) {
			this.queueChestAccess(playerName, loc, type, (short)item.getTypeId(), (short)(item.getAmount() * -1), BukkitUtils.rawData(item));
		}
		this.queueBlockBreak(playerName, loc, type, data);
	}

	/**
	 * @param killer
	 * Can't be null
	 * @param victim
	 * Can't be null
	 */
	public void queueKill(Entity killer, Entity victim) {
		if ((killer == null) || (victim == null)) {
			return;
		}
		int weapon = 0;
		if ((killer instanceof Player) && (((Player)killer).getItemInHand() != null)) {
			weapon = ((Player)killer).getItemInHand().getTypeId();
		}
		this.queueKill(victim.getLocation(), BukkitUtils.entityName(killer), BukkitUtils.entityName(victim), weapon);
	}

	/**
	 * This form should only be used when the killer is not an entity e.g. for fall or suffocation damage
	 * @param killer
	 * Can't be null
	 * @param victim
	 * Can't be null
	 */
	public void queueKill(String killer, Entity victim) {
		if ((killer == null) || (victim == null)) {
			return;
		}
		this.queueKill(victim.getLocation(), killer, BukkitUtils.entityName(victim), 0);
	}

	/**
	 * @param world
	 * World the victim was inside.
	 * @param killerName
	 * Name of the killer. Can be null.
	 * @param victimName
	 * Name of the victim. Can't be null.
	 * @param weapon
	 * Item id of the weapon. 0 for no weapon.
	 * @deprecated Use {@link #queueKill(Location,String,String,int)} instead
	 */
	@Deprecated
	public void queueKill(World world, String killerName, String victimName, int weapon) {
		this.queueKill(new Location(world, 0, 0, 0), killerName, victimName, weapon);
	}

	/**
	 * @param location
	 * Location of the victim.
	 * @param killerName
	 * Name of the killer. Can be null.
	 * @param victimName
	 * Name of the victim. Can't be null.
	 * @param weapon
	 * Item id of the weapon. 0 for no weapon.
	 */
	public void queueKill(Location location, String killerName, String victimName, int weapon) {
		if ((victimName == null) || !Config.isLogged(location.getWorld())) {
			return;
		}
		this.queue.add(new KillRow(location, killerName == null ? null : killerName.replaceAll("[^a-zA-Z0-9_]", ""), victimName.replaceAll("[^a-zA-Z0-9_]", ""), weapon));
	}

	/**
	 * @param type
	 * Type of the sign. Must be 63 or 68.
	 * @param lines
	 * The four lines on the sign.
	 */
	public void queueSignBreak(String playerName, Location loc, int type, byte data, String[] lines) {
		if (((type != 63) && (type != 68)) || (lines == null) || (lines.length != 4)) {
			return;
		}
		this.queueBlock(playerName, loc, type, 0, data, lines[0] + "\0" + lines[1] + "\0" + lines[2] + "\0" + lines[3], null);
	}

	public void queueSignBreak(String playerName, Sign sign) {
		this.queueSignBreak(playerName, new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ()), sign.getTypeId(), sign.getRawData(), sign.getLines());
	}

	/**
	 * @param type
	 * Type of the sign. Must be 63 or 68.
	 * @param lines
	 * The four lines on the sign.
	 */
	public void queueSignPlace(String playerName, Location loc, int type, byte data, String[] lines) {
		if (((type != 63) && (type != 68)) || (lines == null) || (lines.length != 4)) {
			return;
		}
		this.queueBlock(playerName, loc, 0, type, data, lines[0] + "\0" + lines[1] + "\0" + lines[2] + "\0" + lines[3], null);
	}

	public void queueSignPlace(String playerName, Sign sign) {
		this.queueSignPlace(playerName, new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ()), sign.getTypeId(), sign.getRawData(), sign.getLines());
	}

	public void queueChat(String player, String message) {
		for (final String ignored : Config.ignoredChat) {
			if (message.startsWith(ignored)) {
				return;
			}
		}
		this.queue.add(new ChatRow(player, message));
	}

	public void queueJoin(Player player) {
		this.queue.add(new PlayerJoinRow(player));
	}

	public void queueLeave(Player player) {
		this.queue.add(new PlayerLeaveRow(player));
	}

	@Override
	public void run() {
		if (this.queue.isEmpty() || !this.lock.tryLock()) {
			return;
		}
		final Connection conn = this.logblock.getConnection();
		Statement state = null;
		if ((Config.queueWarningSize > 0) && (this.queue.size() >= Config.queueWarningSize)) {
			Bukkit.getLogger().info("[Consumer] Queue overloaded. Size: " + this.getQueueSize());
		}

		try {
			if (conn == null) {
				return;
			}
			conn.setAutoCommit(false);
			state = conn.createStatement();
			final long start = System.currentTimeMillis();
			int count = 0;
			process:
			while (!this.queue.isEmpty() && (((System.currentTimeMillis() - start) < Config.timePerRun) || (count < Config.forceToProcessAtLeast))) {
				final Row r = this.queue.poll();
				if (r == null) {
					continue;
				}
				for (final String player : r.getPlayers()) {
					if (!this.playerIds.containsKey(player)) {
						if (!this.addPlayer(state, player)) {
							if (!this.failedPlayers.contains(player)) {
								this.failedPlayers.add(player);
								Bukkit.getLogger().warning("[Consumer] Failed to add player " + player);
							}
							continue process;
						}
					}
				}
				if (r instanceof PreparedStatementRow) {
					final PreparedStatementRow PSRow = (PreparedStatementRow) r;
					PSRow.setConnection(conn);
					try {
						PSRow.executeStatements();
					} catch (final SQLException ex) {
						Bukkit.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on insertion: ", ex);
						break;
					}
				} else {
					for (final String insert : r.getInserts()) {
						try {
							state.execute(insert);
						} catch (final SQLException ex) {
							Bukkit.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on " + insert + ": ", ex);
							break process;
						}
					}
				}

				count++;
			}
			conn.commit();
		} catch (final SQLException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "[Consumer] SQL exception", ex);
		} finally {
			try {
				if (state != null) {
					state.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (final SQLException ex) {
				Bukkit.getLogger().log(Level.SEVERE, "[Consumer] SQL exception on close", ex);
			}
			this.lock.unlock();
		}
	}

	public void writeToFile() throws FileNotFoundException {
		final long time = System.currentTimeMillis();
		final Set<String> insertedPlayers = new HashSet<String>();
		int counter = 0;
		new File("plugins/LogBlock/import/").mkdirs();
		PrintWriter writer = new PrintWriter(new File("plugins/LogBlock/import/queue-" + time + "-0.sql"));
		while (!this.queue.isEmpty()) {
			final Row r = this.queue.poll();
			if (r == null) {
				continue;
			}
			for (final String player : r.getPlayers()) {
				if (!this.playerIds.containsKey(player) && !insertedPlayers.contains(player)) {
					// Odd query contruction is to work around innodb auto increment behaviour - bug #492
					writer.println("INSERT IGNORE INTO `lb-players` (playername) SELECT '" + player + "' FROM `lb-players` WHERE NOT EXISTS (SELECT NULL FROM `lb-players` WHERE playername = '" + player + "') LIMIT 1;");
					insertedPlayers.add(player);
				}
			}
			for (final String insert : r.getInserts()) {
				writer.println(insert);
			}
			counter++;
			if ((counter % 1000) == 0) {
				writer.close();
				writer = new PrintWriter(new File("plugins/LogBlock/import/queue-" + time + "-" + (counter / 1000) + ".sql"));
			}
		}
		writer.close();
	}

	int getQueueSize() {
		return this.queue.size();
	}

	static boolean hide(Player player) {
		final String playerName = player.getName().toLowerCase();
		if (Config.hiddenPlayers.contains(playerName)) {
			Config.hiddenPlayers.remove(playerName);
			return false;
		}
		Config.hiddenPlayers.add(playerName);
		return true;
	}

	private boolean addPlayer(Statement state, String playerName) throws SQLException {
		// Odd query contruction is to work around innodb auto increment behaviour - bug #492
		state.execute("INSERT IGNORE INTO `lb-players` (playername) SELECT '" + playerName + "' FROM `lb-players` WHERE NOT EXISTS (SELECT NULL FROM `lb-players` WHERE playername = '" + playerName + "') LIMIT 1;");
		final ResultSet rs = state.executeQuery("SELECT playerid FROM `lb-players` WHERE playername = '" + playerName + "'");
		if (rs.next()) {
			this.playerIds.put(playerName, rs.getInt(1));
		}
		rs.close();
		return this.playerIds.containsKey(playerName);
	}

	private void queueBlock(String playerName, Location loc, int typeBefore, int typeAfter, byte data, String signtext, ChestAccess ca) {

		if (Config.fireCustomEvents) {
			// Create and call the event
			final BlockChangePreLogEvent event = new BlockChangePreLogEvent(playerName, loc, typeBefore, typeAfter, data, signtext, ca);
			this.logblock.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}

			// Update variables
			playerName = event.getOwner();
			loc = event.getLocation();
			typeBefore = event.getTypeBefore();
			typeAfter = event.getTypeAfter();
			data = event.getData();
			signtext = event.getSignText();
			ca = event.getChestAccess();
		}
		// Do this last so LogBlock still has final say in what is being added
		if ((playerName == null) || (loc == null) || (typeBefore < 0) || (typeAfter < 0) || (Config.safetyIdCheck && ((typeBefore > 255) || (typeAfter > 255))) || Config.hiddenPlayers.contains(playerName.toLowerCase()) || !Config.isLogged(loc.getWorld()) || ((typeBefore != typeAfter) && Config.hiddenBlocks.contains(typeBefore) && Config.hiddenBlocks.contains(typeAfter))) {
			return;
		}
		this.queue.add(new BlockRow(loc, playerName.replaceAll("[^a-zA-Z0-9_]", ""), typeBefore, typeAfter, data, signtext, ca));
	}

	private String playerID(String playerName) {
		if (playerName == null) {
			return "NULL";
		}
		final Integer id = this.playerIds.get(playerName);
		if (id != null) {
			return id.toString();
		}
		return "(SELECT playerid FROM `lb-players` WHERE playername = '" + playerName + "')";
	}

	private Integer playerIDAsInt(String playerName) {
		if (playerName == null) {
			return null;
		}
		return this.playerIds.get(playerName);
	}

	private static interface Row
	{
		String[] getInserts();

		String[] getPlayers();
	}

	private interface PreparedStatementRow extends Row
	{

		abstract void setConnection(Connection connection);
		abstract void executeStatements() throws SQLException;

	}

	private class BlockRow extends BlockChange implements PreparedStatementRow
	{
		private Connection connection;

		public BlockRow(Location loc, String playerName, int replaced, int type, byte data, String signtext, ChestAccess ca) {
			super(System.currentTimeMillis() / 1000, loc, playerName, replaced, type, data, signtext, ca);
		}

		@Override
		public String[] getInserts() {
			final String table = Config.getWorldConfig(this.loc.getWorld()).table;
			final String[] inserts = new String[(this.ca != null) || (this.signtext != null) ? 2 : 1];
			inserts[0] = "INSERT INTO `" + table + "` (date, playerid, replaced, type, data, x, y, z) VALUES (FROM_UNIXTIME(" + this.date + "), " + Consumer.this.playerID(this.playerName) + ", " + this.replaced + ", " + this.type + ", " + this.data + ", '" + this.loc.getBlockX() + "', " + this.loc.getBlockY() + ", '" + this.loc.getBlockZ() + "');";
			if (this.signtext != null) {
				inserts[1] = "INSERT INTO `" + table + "-sign` (id, signtext) values (LAST_INSERT_ID(), '" + this.signtext.replace("\\", "\\\\").replace("'", "\\'") + "');";
			}
			else if (this.ca != null) {
				inserts[1] = "INSERT INTO `" + table + "-chest` (id, itemtype, itemamount, itemdata) values (LAST_INSERT_ID(), " + this.ca.itemType + ", " + this.ca.itemAmount + ", " + this.ca.itemData + ");";
			}
			return inserts;
		}

		@Override
		public String[] getPlayers() {
			return new String[]{this.playerName};
		}

		@Override
		public void setConnection(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void executeStatements() throws SQLException {
			final String table = Config.getWorldConfig(this.loc.getWorld()).table;

			PreparedStatement ps1 = null;
			PreparedStatement ps = null;
			try {
				ps1 = this.connection.prepareStatement("INSERT INTO `" + table + "` (date, playerid, replaced, type, data, x, y, z) VALUES(FROM_UNIXTIME(?), " + Consumer.this.playerID(this.playerName) + ", ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				ps1.setLong(1, this.date );
				ps1.setInt(2, this.replaced);
				ps1.setInt(3, this.type);
				ps1.setInt(4, this.data);
				ps1.setInt(5, this.loc.getBlockX());
				ps1.setInt(6, this.loc.getBlockY());
				ps1.setInt(7, this.loc.getBlockZ());
				ps1.executeUpdate();
	
				int id;
				final ResultSet rs = ps1.getGeneratedKeys();
				rs.next();
				id = rs.getInt(1);
	
				if (this.signtext != null) {
					ps = this.connection.prepareStatement("INSERT INTO `" + table + "-sign` (signtext, id) VALUES(?, ?)");
					ps.setString(1, this.signtext);
					ps.setInt(2, id);
					ps.executeUpdate();
				} else if (this.ca != null) {
					ps = this.connection.prepareStatement("INSERT INTO `" + table + "-chest` (itemtype, itemamount, itemdata, id) values (?, ?, ?, ?)");
					ps.setInt(1, this.ca.itemType);
					ps.setInt(2, this.ca.itemAmount);
					ps.setInt(3, this.ca.itemData);
					ps.setInt(4, id);
					ps.executeUpdate();
				}
			}
			// we intentionally do not catch SQLException, it is thrown to the caller
			finally {
				// individual try/catch here, though ugly, prevents resource leaks
				if( ps1 != null ) {
					try {
						ps1.close();
					}
					catch(final SQLException e) {
						// ideally should log to logger, none is available in this class
						// at the time of this writing, so I'll leave that to the plugin
						// maintainers to integrate if they wish
						e.printStackTrace();
					}
				}
				
				if( ps != null ) {
					try {
						ps.close();
					}
					catch(final SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class KillRow implements Row
	{
		final long date;
		final String killer, victim;
		final int weapon;
		final Location loc;

		KillRow(Location loc, String attacker, String defender, int weapon) {
			this.date = System.currentTimeMillis() / 1000;
			this.loc = loc;
			this.killer = attacker;
			this.victim = defender;
			this.weapon = weapon;
		}

		@Override
		public String[] getInserts() {
			return new String[]{"INSERT INTO `" + Config.getWorldConfig(this.loc.getWorld()).table + "-kills` (date, killer, victim, weapon, x, y, z) VALUES (FROM_UNIXTIME(" + this.date + "), " + Consumer.this.playerID(this.killer) + ", " + Consumer.this.playerID(this.victim) + ", " + this.weapon + ", " + this.loc.getBlockX() + ", " + (this.loc.getBlockY() < 0 ? 0 : this.loc.getBlockY()) + ", " + this.loc.getBlockZ() + ");"};
		}

		@Override
		public String[] getPlayers() {
			return new String[]{this.killer, this.victim};
		}
	}

	private class ChatRow extends ChatMessage implements PreparedStatementRow
	{
		private Connection connection;

		ChatRow(String player, String message) {
			super(player, message);
		}

		@Override
		public String[] getInserts() {
			return new String[]{"INSERT INTO `lb-chat` (date, playerid, message) VALUES (FROM_UNIXTIME(" + this.date + "), " + Consumer.this.playerID(this.playerName) + ", '" + this.message.replace("\\", "\\\\").replace("'", "\\'") + "');"};
		}

		@Override
		public String[] getPlayers() {
			return new String[]{this.playerName};
		}

		@Override
		public void setConnection(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void executeStatements() throws SQLException {
			boolean noID = false;
			Integer id;

			String sql = "INSERT INTO `lb-chat` (date, playerid, message) VALUES (FROM_UNIXTIME(?), ";
			if ((id = Consumer.this.playerIDAsInt(this.playerName)) == null) {
				noID = true;
				sql += Consumer.this.playerID(this.playerName) + ", ";
			} else {
				sql += "?, ";
			}
			sql += "?)";
			
			PreparedStatement ps = null;
			try {
				ps = this.connection.prepareStatement(sql);
				ps.setLong(1, this.date);
				if (!noID) {
					ps.setInt(2, id);
					ps.setString(3, this.message);
				} else {
					ps.setString(2, this.message);
				}
				ps.execute();
			}
			// we intentionally do not catch SQLException, it is thrown to the caller
			finally {
				if( ps != null ) {
					try {
						ps.close();
					}
					catch(final SQLException e) {
						// should print to a Logger instead if one is ever added to this class
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class PlayerJoinRow implements Row
	{
		private final String playerName;
		private final long lastLogin;
		private final String ip;

		PlayerJoinRow(Player player) {
			this.playerName = player.getName();
			this.lastLogin = System.currentTimeMillis() / 1000;
			this.ip = player.getAddress().toString().replace("'", "\\'");
		}

		@Override
		public String[] getInserts() {
			return new String[]{"UPDATE `lb-players` SET lastlogin = FROM_UNIXTIME(" + this.lastLogin + "), firstlogin = IF(firstlogin = 0, FROM_UNIXTIME(" + this.lastLogin + "), firstlogin), ip = '" + this.ip + "' WHERE " + (Consumer.this.playerIds.containsKey(this.playerName) ? "playerid = " + Consumer.this.playerIds.get(this.playerName) : "playerName = '" + this.playerName + "'") + ";"};
		}

		@Override
		public String[] getPlayers() {
			return new String[]{this.playerName};
		}
	}

	private class PlayerLeaveRow implements Row
	{
		private final String playerName;
		private final long leaveTime;

		PlayerLeaveRow(Player player) {
			this.playerName = player.getName();
			this.leaveTime = System.currentTimeMillis() / 1000;
		}

		@Override
		public String[] getInserts() {
			return new String[]{"UPDATE `lb-players` SET onlinetime = onlinetime + TIMESTAMPDIFF(SECOND, lastlogin, FROM_UNIXTIME('" + this.leaveTime + "')) WHERE lastlogin > 0 && " + (Consumer.this.playerIds.containsKey(this.playerName) ? "playerid = " + Consumer.this.playerIds.get(this.playerName) : "playerName = '" + this.playerName + "'") + ";"};
		}

		@Override
		public String[] getPlayers() {
			return new String[]{this.playerName};
		}
	}
}
