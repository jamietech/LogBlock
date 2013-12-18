package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.LoggingUtil.smartLogBlockBreak;
import static de.diddiz.util.LoggingUtil.smartLogFallables;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.util.LoggingUtil;

public class BlockBurnLogging extends LoggingListener
{
	public BlockBurnLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		if (Config.isLogging(event.getBlock().getWorld(), Logging.FIRE)) {
			LoggingUtil.smartLogBlockBreak(this.consumer, "Fire", event.getBlock());
			LoggingUtil.smartLogFallables(this.consumer, "Fire", event.getBlock());
		}
	}
}
