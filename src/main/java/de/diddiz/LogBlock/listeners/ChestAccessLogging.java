package de.diddiz.LogBlock.listeners;

import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.BukkitUtils.compareInventories;
import static de.diddiz.util.BukkitUtils.compressInventory;
import static de.diddiz.util.BukkitUtils.getInventoryHolderLocation;
import static de.diddiz.util.BukkitUtils.getInventoryHolderType;
import static de.diddiz.util.BukkitUtils.rawData;
import java.util.HashMap;
import java.util.Map;

import de.diddiz.LogBlock.Logging;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.util.BukkitUtils;

public class ChestAccessLogging extends LoggingListener
{
	private final Map<HumanEntity, ItemStack[]> containers = new HashMap<HumanEntity, ItemStack[]>();

	public ChestAccessLogging(LogBlock lb) {
		super(lb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {

		if (!Config.isLogging(event.getPlayer().getWorld(), Logging.CHESTACCESS)) {
			return;
		}
		final InventoryHolder holder = event.getInventory().getHolder();
		if ((holder instanceof BlockState) || (holder instanceof DoubleChest)) {
			final HumanEntity player = event.getPlayer();
			final ItemStack[] before = this.containers.get(player);
			if (before != null) {
				final ItemStack[] after = BukkitUtils.compressInventory(event.getInventory().getContents());
				final ItemStack[] diff = BukkitUtils.compareInventories(before, after);
				final Location loc = BukkitUtils.getInventoryHolderLocation(holder);
				for (final ItemStack item : diff) {
					this.consumer.queueChestAccess(player.getName(), loc, loc.getWorld().getBlockTypeIdAt(loc), (short)item.getTypeId(), (short)item.getAmount(), BukkitUtils.rawData(item));
				}
				this.containers.remove(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {

		if (!Config.isLogging(event.getPlayer().getWorld(), Logging.CHESTACCESS)) {
			return;
		}
		if (event.getInventory() != null) {
			final InventoryHolder holder = event.getInventory().getHolder();
			if ((holder instanceof BlockState) || (holder instanceof DoubleChest)) {
				if (BukkitUtils.getInventoryHolderType(holder) != 58) {
					this.containers.put(event.getPlayer(), BukkitUtils.compressInventory(event.getInventory().getContents()));
				}
			}
		}
	}
}
