package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.LoggingUtil.smartLogBlockBreak;
import static de.diddiz.util.LoggingUtil.smartLogFallables;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;
import de.diddiz.util.LoggingUtil;

public class BlockBreakLogging extends LoggingListener
{
	public BlockBreakLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (Config.isLogging(event.getBlock().getWorld(), Logging.BLOCKBREAK)) {
			final WorldConfig wcfg = Config.getWorldConfig(event.getBlock().getWorld());
			if (wcfg == null) {
				return;
			}

			final String playerName = event.getPlayer().getName();
			final Block origin = event.getBlock();
			final int typeId = origin.getTypeId();
			final Material type = origin.getType();

			if (wcfg.isLogging(Logging.SIGNTEXT) && ((typeId == 63) || (typeId == 68))) {
				this.consumer.queueSignBreak(playerName, (Sign) origin.getState());
			} else if (wcfg.isLogging(Logging.CHESTACCESS) && BukkitUtils.getContainerBlocks().contains(type)) {
				this.consumer.queueContainerBreak(playerName, origin.getState());
			} else if (type == Material.ICE) {
				// When in creative mode ice doesn't form water
				if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
					this.consumer.queueBlockBreak(playerName, origin.getState());
				} else {
					this.consumer.queueBlockReplace(playerName, origin.getState(), 9, (byte) 0);
				}
			} else {
				LoggingUtil.smartLogBlockBreak(this.consumer, playerName, origin);
			}
			LoggingUtil.smartLogFallables(this.consumer, playerName, origin);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (Config.isLogging(event.getBlockClicked().getWorld(), Logging.BLOCKBREAK)) {
			this.consumer.queueBlockBreak(event.getPlayer().getName(), event.getBlockClicked().getState());
		}
	}
}
