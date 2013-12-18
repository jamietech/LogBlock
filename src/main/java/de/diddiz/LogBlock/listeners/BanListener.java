package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.banPermission;
import static de.diddiz.LogBlock.config.Config.isLogged;
import static org.bukkit.Bukkit.getScheduler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import de.diddiz.LogBlock.CommandsHandler;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.config.Config;

public class BanListener implements Listener
{
	private final CommandsHandler handler;
	private final LogBlock logblock;

	public BanListener(LogBlock logblock) {
		this.logblock = logblock;
		this.handler = logblock.getCommandsHandler();
	}

	@EventHandler
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		final String[] split = event.getMessage().split(" ");
		if ((split.length > 1) && split[0].equalsIgnoreCase("/ban") && this.logblock.hasPermission(event.getPlayer(), Config.banPermission)) {
			final QueryParams p = new QueryParams(this.logblock);
			p.setPlayer(split[1].equalsIgnoreCase("g") ? split[2] : split[1]);
			p.since = 0;
			p.silent = false;
			Bukkit.getScheduler().scheduleAsyncDelayedTask(this.logblock, new Runnable()
			{
				@Override
				public void run() {
					for (final World world : BanListener.this.logblock.getServer().getWorlds()) {
						if (Config.isLogged(world)) {
							p.world = world;
							try {
								BanListener.this.handler.new CommandRollback(event.getPlayer(), p, false);
							} catch (final Exception ex) {
							}
						}
					}
				}
			});
		}
	}
}
