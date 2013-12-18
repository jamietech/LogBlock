package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;

import static de.diddiz.LogBlock.config.Config.isLogging;

public class BlockSpreadLogging extends LoggingListener
{

	public BlockSpreadLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {

		String name;

		final World world  = event.getBlock().getWorld();
		final Material type = event.getSource().getType();

		switch (type) {
			case GRASS:
				if (!Config.isLogging(world, Logging.GRASSGROWTH)) {
					return;
				}
				name = "GrassGrowth";
				break;
			case MYCEL:
				if (!Config.isLogging(world, Logging.MYCELIUMSPREAD)) {
					return;
				}
				name = "MyceliumSpread";
				break;
			case VINE:
				if (!Config.isLogging(world, Logging.VINEGROWTH)) {
					return;
				}
				name = "VineGrowth";
				break;
			case RED_MUSHROOM:
			case BROWN_MUSHROOM:
				if (!Config.isLogging(world, Logging.MUSHROOMSPREAD)) {
					return;
				}
				name = "MushroomSpread";
				break;
			default:
				return;
		}

		this.consumer.queueBlockReplace(name, event.getBlock().getState(), event.getNewState());
	}
}
