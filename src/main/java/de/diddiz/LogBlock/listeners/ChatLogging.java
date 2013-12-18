package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.isLogging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;

public class ChatLogging extends LoggingListener
{
	public ChatLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (Config.isLogging(event.getPlayer().getWorld(), Logging.CHAT)) {
			this.consumer.queueChat(event.getPlayer().getName(), event.getMessage());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (Config.isLogging(event.getPlayer().getWorld(), Logging.CHAT)) {
			this.consumer.queueChat(event.getPlayer().getName(), event.getMessage());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onServerCommand(ServerCommandEvent event) {
		this.consumer.queueChat("Console", "/" + event.getCommand());
	}
}
