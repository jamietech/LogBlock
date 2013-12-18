package de.diddiz.LogBlock;

import static de.diddiz.LogBlock.config.Config.autoClearLog;
import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import de.diddiz.LogBlock.config.Config;

public class AutoClearLog implements Runnable
{
	private final LogBlock logblock;

	AutoClearLog(LogBlock logblock) {
		this.logblock = logblock;
	}

	@Override
	public void run() {
		final CommandsHandler handler = this.logblock.getCommandsHandler();
		for (final String paramStr : Config.autoClearLog) {
			try {
				final QueryParams params = new QueryParams(this.logblock, Bukkit.getConsoleSender(), Arrays.asList(paramStr.split(" ")));
				handler.new CommandClearLog(Bukkit.getServer().getConsoleSender(), params, false);
			} catch (final Exception ex) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to schedule auto ClearLog: ", ex);
			}
		}
	}
}
