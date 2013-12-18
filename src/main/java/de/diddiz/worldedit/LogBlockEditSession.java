package de.diddiz.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class LogBlockEditSession extends EditSession {

	private final LocalPlayer player;
	private final LogBlock plugin;

	/**
	 * {@inheritDoc}
	 */
	public LogBlockEditSession(LocalWorld world, int maxBlocks, LocalPlayer player, LogBlock lb) {
		super(world, maxBlocks);
		this.player = player;
		this.plugin = lb;
	}

	/**
	 * {@inheritDoc}
	 */
	public LogBlockEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player, LogBlock lb) {
		super(world, maxBlocks, blockBag);
		this.player = player;
		this.plugin = lb;
	}

	@Override
	public boolean rawSetBlock(Vector pt, BaseBlock block) {
		if (!(this.player.getWorld() instanceof BukkitWorld) || !(Config.isLogging(this.player.getWorld().getName(), Logging.WORLDEDIT))) {
			return super.rawSetBlock(pt, block);
		}

		final int typeBefore = ((BukkitWorld) this.player.getWorld()).getWorld().getBlockTypeIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
		final byte dataBefore = ((BukkitWorld) this.player.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getData();
		// If we're dealing with a sign, store the block state to read the text off
		BlockState stateBefore = null;
		if ((typeBefore == Material.SIGN_POST.getId()) || (typeBefore == Material.SIGN.getId())) {
			stateBefore = ((BukkitWorld) this.player.getWorld()).getWorld().getBlockAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()).getState();
		}
		final boolean success = super.rawSetBlock(pt, block);
		if (success) {
			final Location location = new Location(((BukkitWorld) this.player.getWorld()).getWorld(), pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

			// Check to see if we've broken a sign
			if (Config.isLogging(location.getWorld().getName(), Logging.SIGNTEXT) && ((typeBefore == Material.SIGN_POST.getId()) || (typeBefore == Material.SIGN.getId()))) {
				this.plugin.getConsumer().queueSignBreak(this.player.getName(), (Sign) stateBefore);
				if (block.getType() != Material.AIR.getId()) {
					this.plugin.getConsumer().queueBlockPlace(this.player.getName(), location, block.getType(), (byte) block.getData());
				}
			} else {
				if (dataBefore != 0) {
					this.plugin.getConsumer().queueBlockBreak(this.player.getName(), location, typeBefore, dataBefore);
					if (block.getType() != Material.AIR.getId()) {
						this.plugin.getConsumer().queueBlockPlace(this.player.getName(), location, block.getType(), (byte) block.getData());
					}
				} else {
					this.plugin.getConsumer().queueBlock(this.player.getName(), location, typeBefore, block.getType(), (byte) block.getData());
				}
			}
		}
		return success;
	}

}
