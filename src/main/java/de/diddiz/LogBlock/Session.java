package de.diddiz.LogBlock;

import static de.diddiz.LogBlock.config.Config.toolsByType;
import static org.bukkit.Bukkit.getServer;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.diddiz.LogBlock.config.Config;

public class Session
{
	private static final Map<String, Session> sessions = new HashMap<String, Session>();
	public QueryParams lastQuery = null;
	public LookupCacheElement[] lookupCache = null;
	public int page = 1;
	public Map<Tool, ToolData> toolData;

	private Session(Player player) {
		this.toolData = new HashMap<Tool, ToolData>();
		final LogBlock logblock = LogBlock.getInstance();
		if (player != null) {
			for (final Tool tool : Config.toolsByType.values()) {
				this.toolData.put(tool, new ToolData(tool, logblock, player));
			}
		}
	}

	public static boolean hasSession(CommandSender sender) {
		return Session.sessions.containsKey(sender.getName().toLowerCase());
	}

	public static boolean hasSession(String playerName) {
		return Session.sessions.containsKey(playerName.toLowerCase());
	}

	public static Session getSession(CommandSender sender) {
		return Session.getSession(sender.getName());
	}

	public static Session getSession(String playerName) {
		Session session = Session.sessions.get(playerName.toLowerCase());
		if (session == null) {
			session = new Session(Bukkit.getServer().getPlayer(playerName));
			Session.sessions.put(playerName.toLowerCase(), session);
		}
		return session;
	}
}
