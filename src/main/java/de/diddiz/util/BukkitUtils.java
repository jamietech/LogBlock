package de.diddiz.util;

import static de.diddiz.util.MaterialName.materialName;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class BukkitUtils
{
	private static final Set<Set<Integer>> blockEquivalents;
	private static final Set<Material> relativeBreakable;
	private static final Set<Material> relativeTopBreakable;
	private static final Set<Material> relativeTopFallables;
	private static final Set<Material> fallingEntityKillers;

	private static final Set<Material> cropBlocks;
	private static final Set<Material> containerBlocks;

	static {
		blockEquivalents = new HashSet<Set<Integer>>(7);
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(2, 3, 60)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(8, 9, 79)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(10, 11)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(61, 62)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(73, 74)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(75, 76)));
		BukkitUtils.blockEquivalents.add(new HashSet<Integer>(Arrays.asList(93, 94)));

		// Blocks that break when they are attached to a block
		relativeBreakable = new HashSet<Material>(11);
		BukkitUtils.relativeBreakable.add(Material.WALL_SIGN);
		BukkitUtils.relativeBreakable.add(Material.LADDER);
		BukkitUtils.relativeBreakable.add(Material.STONE_BUTTON);
		BukkitUtils.relativeBreakable.add(Material.WOOD_BUTTON);
		BukkitUtils.relativeBreakable.add(Material.REDSTONE_TORCH_ON);
		BukkitUtils.relativeBreakable.add(Material.REDSTONE_TORCH_OFF);
		BukkitUtils.relativeBreakable.add(Material.LEVER);
		BukkitUtils.relativeBreakable.add(Material.TORCH);
		BukkitUtils.relativeBreakable.add(Material.TRAP_DOOR);
		BukkitUtils.relativeBreakable.add(Material.TRIPWIRE_HOOK);
		BukkitUtils.relativeBreakable.add(Material.COCOA);

		// Blocks that break when they are on top of a block
		relativeTopBreakable = new HashSet<Material>(32);
		BukkitUtils.relativeTopBreakable.add(Material.SAPLING);
		BukkitUtils.relativeTopBreakable.add(Material.LONG_GRASS);
		BukkitUtils.relativeTopBreakable.add(Material.DEAD_BUSH);
		BukkitUtils.relativeTopBreakable.add(Material.YELLOW_FLOWER);
		BukkitUtils.relativeTopBreakable.add(Material.RED_ROSE);
		BukkitUtils.relativeTopBreakable.add(Material.BROWN_MUSHROOM);
		BukkitUtils.relativeTopBreakable.add(Material.RED_MUSHROOM);
		BukkitUtils.relativeTopBreakable.add(Material.CROPS);
		BukkitUtils.relativeTopBreakable.add(Material.POTATO);
		BukkitUtils.relativeTopBreakable.add(Material.CARROT);
		BukkitUtils.relativeTopBreakable.add(Material.WATER_LILY);
		BukkitUtils.relativeTopBreakable.add(Material.CACTUS);
		BukkitUtils.relativeTopBreakable.add(Material.SUGAR_CANE_BLOCK);
		BukkitUtils.relativeTopBreakable.add(Material.FLOWER_POT);
		BukkitUtils.relativeTopBreakable.add(Material.POWERED_RAIL);
		BukkitUtils.relativeTopBreakable.add(Material.DETECTOR_RAIL);
		BukkitUtils.relativeTopBreakable.add(Material.ACTIVATOR_RAIL);
		BukkitUtils.relativeTopBreakable.add(Material.RAILS);
		BukkitUtils.relativeTopBreakable.add(Material.REDSTONE_WIRE);
		BukkitUtils.relativeTopBreakable.add(Material.SIGN_POST);
		BukkitUtils.relativeTopBreakable.add(Material.STONE_PLATE);
		BukkitUtils.relativeTopBreakable.add(Material.WOOD_PLATE);
		BukkitUtils.relativeTopBreakable.add(Material.IRON_PLATE);
		BukkitUtils.relativeTopBreakable.add(Material.GOLD_PLATE);
		BukkitUtils.relativeTopBreakable.add(Material.SNOW);
		BukkitUtils.relativeTopBreakable.add(Material.DIODE_BLOCK_ON);
		BukkitUtils.relativeTopBreakable.add(Material.DIODE_BLOCK_OFF);
		BukkitUtils.relativeTopBreakable.add(Material.REDSTONE_COMPARATOR_ON);
		BukkitUtils.relativeTopBreakable.add(Material.REDSTONE_COMPARATOR_OFF);
		BukkitUtils.relativeTopBreakable.add(Material.WOODEN_DOOR);
		BukkitUtils.relativeTopBreakable.add(Material.IRON_DOOR);
		BukkitUtils.relativeTopBreakable.add(Material.CARPET);

		// Blocks that fall
		relativeTopFallables = new HashSet<Material>(4);
		BukkitUtils.relativeTopFallables.add(Material.SAND);
		BukkitUtils.relativeTopFallables.add(Material.GRAVEL);
		BukkitUtils.relativeTopFallables.add(Material.DRAGON_EGG);
		BukkitUtils.relativeTopFallables.add(Material.ANVIL);

		// Blocks that break falling entities
		fallingEntityKillers = new HashSet<Material>(32);
		BukkitUtils.fallingEntityKillers.add(Material.SIGN_POST);
		BukkitUtils.fallingEntityKillers.add(Material.WALL_SIGN);
		BukkitUtils.fallingEntityKillers.add(Material.STONE_PLATE);
		BukkitUtils.fallingEntityKillers.add(Material.WOOD_PLATE);
		BukkitUtils.fallingEntityKillers.add(Material.IRON_PLATE);
		BukkitUtils.fallingEntityKillers.add(Material.GOLD_PLATE);
		BukkitUtils.fallingEntityKillers.add(Material.SAPLING);
		BukkitUtils.fallingEntityKillers.add(Material.YELLOW_FLOWER);
		BukkitUtils.fallingEntityKillers.add(Material.RED_ROSE);
		BukkitUtils.fallingEntityKillers.add(Material.CROPS);
		BukkitUtils.fallingEntityKillers.add(Material.CARROT);
		BukkitUtils.fallingEntityKillers.add(Material.POTATO);
		BukkitUtils.fallingEntityKillers.add(Material.RED_MUSHROOM);
		BukkitUtils.fallingEntityKillers.add(Material.BROWN_MUSHROOM);
		BukkitUtils.fallingEntityKillers.add(Material.STEP);
		BukkitUtils.fallingEntityKillers.add(Material.WOOD_STEP);
		BukkitUtils.fallingEntityKillers.add(Material.TORCH);
		BukkitUtils.fallingEntityKillers.add(Material.FLOWER_POT);
		BukkitUtils.fallingEntityKillers.add(Material.POWERED_RAIL);
		BukkitUtils.fallingEntityKillers.add(Material.DETECTOR_RAIL);
		BukkitUtils.fallingEntityKillers.add(Material.ACTIVATOR_RAIL);
		BukkitUtils.fallingEntityKillers.add(Material.RAILS);
		BukkitUtils.fallingEntityKillers.add(Material.LEVER);
		BukkitUtils.fallingEntityKillers.add(Material.REDSTONE_WIRE);
		BukkitUtils.fallingEntityKillers.add(Material.REDSTONE_TORCH_ON);
		BukkitUtils.fallingEntityKillers.add(Material.REDSTONE_TORCH_OFF);
		BukkitUtils.fallingEntityKillers.add(Material.DIODE_BLOCK_ON);
		BukkitUtils.fallingEntityKillers.add(Material.DIODE_BLOCK_OFF);
		BukkitUtils.fallingEntityKillers.add(Material.REDSTONE_COMPARATOR_ON);
		BukkitUtils.fallingEntityKillers.add(Material.REDSTONE_COMPARATOR_OFF);
		BukkitUtils.fallingEntityKillers.add(Material.DAYLIGHT_DETECTOR);
		BukkitUtils.fallingEntityKillers.add(Material.CARPET);

		// Crop Blocks
		cropBlocks = new HashSet<Material>(5);
		BukkitUtils.cropBlocks.add(Material.CROPS);
		BukkitUtils.cropBlocks.add(Material.MELON_STEM);
		BukkitUtils.cropBlocks.add(Material.PUMPKIN_STEM);
		BukkitUtils.cropBlocks.add(Material.CARROT);
		BukkitUtils.cropBlocks.add(Material.POTATO);

		// Container Blocks
		containerBlocks = new HashSet<Material>(6);
		BukkitUtils.containerBlocks.add(Material.CHEST);
		BukkitUtils.containerBlocks.add(Material.TRAPPED_CHEST);
		BukkitUtils.containerBlocks.add(Material.DISPENSER);
		BukkitUtils.containerBlocks.add(Material.DROPPER);
		BukkitUtils.containerBlocks.add(Material.HOPPER);
		BukkitUtils.containerBlocks.add(Material.BREWING_STAND);
		BukkitUtils.containerBlocks.add(Material.FURNACE);
		BukkitUtils.containerBlocks.add(Material.BEACON);
		// Doesn't actually have a block inventory
		// containerBlocks.add(Material.ENDER_CHEST);
	}

	private static final BlockFace[] relativeBlockFaces = new BlockFace[] {
			BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN
	};

	/**
	 * Returns a list of block locations around the block that are of the type specified by the integer list parameter
	 * 
	 * @param block
	 * @param type
	 * @return List of block locations around the block that are of the type specified by the integer list parameter
	 */
	public static List<Location> getBlocksNearby(org.bukkit.block.Block block, Set<Material> type) {
		final ArrayList<Location> blocks = new ArrayList<Location>();
		for (final BlockFace blockFace : BukkitUtils.relativeBlockFaces) {
			if (type.contains(block.getRelative(blockFace).getType())) {
				blocks.add(block.getRelative(blockFace).getLocation());
			}
		}
		return blocks;
	}

	public static int getInventoryHolderType(InventoryHolder holder) {
		if (holder instanceof DoubleChest) {
			return ((DoubleChest)holder).getLocation().getBlock().getTypeId();
		} else if (holder instanceof BlockState) {
			return ((BlockState)holder).getTypeId();
		} else {
			return -1;
		}
	}

	public static Location getInventoryHolderLocation(InventoryHolder holder) {
		if (holder instanceof DoubleChest) {
			return ((DoubleChest)holder).getLocation();
		} else if (holder instanceof BlockState) {
			return ((BlockState)holder).getLocation();
		} else {
			return null;
		}
	}

	public static ItemStack[] compareInventories(ItemStack[] items1, ItemStack[] items2) {
		final ItemStackComparator comperator = new ItemStackComparator();
		final ArrayList<ItemStack> diff = new ArrayList<ItemStack>();
		final int l1 = items1.length, l2 = items2.length;
		int c1 = 0, c2 = 0;
		while ((c1 < l1) || (c2 < l2)) {
			if (c1 >= l1) {
				diff.add(items2[c2]);
				c2++;
				continue;
			}
			if (c2 >= l2) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
				continue;
			}
			final int comp = comperator.compare(items1[c1], items2[c2]);
			if (comp < 0) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
			} else if (comp > 0) {
				diff.add(items2[c2]);
				c2++;
			} else {
				final int amount = items2[c2].getAmount() - items1[c1].getAmount();
				if (amount != 0) {
					items1[c1].setAmount(amount);
					diff.add(items1[c1]);
				}
				c1++;
				c2++;
			}
		}
		return diff.toArray(new ItemStack[diff.size()]);
	}

	public static ItemStack[] compressInventory(ItemStack[] items) {
		final ArrayList<ItemStack> compressed = new ArrayList<ItemStack>();
		for (final ItemStack item : items) {
			if (item != null) {
				final int type = item.getTypeId();
				final short data = BukkitUtils.rawData(item);
				boolean found = false;
				for (final ItemStack item2 : compressed) {
					if ((type == item2.getTypeId()) && (data == BukkitUtils.rawData(item2))) {
						item2.setAmount(item2.getAmount() + item.getAmount());
						found = true;
						break;
					}
				}
				if (!found) {
					compressed.add(new ItemStack(type, item.getAmount(), data));
				}
			}
		}
		Collections.sort(compressed, new ItemStackComparator());
		return compressed.toArray(new ItemStack[compressed.size()]);
	}

	public static boolean equalTypes(int type1, int type2) {
		if (type1 == type2) {
			return true;
		}
		for (final Set<Integer> equivalent : BukkitUtils.blockEquivalents) {
			if (equivalent.contains(type1) && equivalent.contains(type2)) {
				return true;
			}
		}
		return false;
	}

	public static String friendlyWorldname(String worldName) {
		return new File(worldName).getName();
	}

	public static Set<Set<Integer>> getBlockEquivalents() {
		return BukkitUtils.blockEquivalents;
	}

	public static Set<Material> getRelativeBreakables() {
		return BukkitUtils.relativeBreakable;
	}

	public static Set<Material> getRelativeTopBreakabls() {
		return BukkitUtils.relativeTopBreakable;
	}

	public static Set<Material> getRelativeTopFallables() {
		return BukkitUtils.relativeTopFallables;
	}

	public static Set<Material> getFallingEntityKillers() {
		return BukkitUtils.fallingEntityKillers;
	}

	public static Set<Material> getCropBlocks() {
		return BukkitUtils.cropBlocks;
	}

	public static Set<Material> getContainerBlocks() {
		return BukkitUtils.containerBlocks;
	}

	public static String entityName(Entity entity) {
		if (entity instanceof Player) {
			return ((Player)entity).getName();
		}
		if (entity instanceof TNTPrimed) {
			return "TNT";
		}
		return entity.getClass().getSimpleName().substring(5);
	}

	public static void giveTool(Player player, int type) {
		final Inventory inv = player.getInventory();
		if (inv.contains(type)) {
			player.sendMessage(ChatColor.RED + "You have already a " + MaterialName.materialName(type));
		} else {
			final int free = inv.firstEmpty();
			if (free >= 0) {
				if ((player.getItemInHand() != null) && (player.getItemInHand().getTypeId() != 0)) {
					inv.setItem(free, player.getItemInHand());
				}
				player.setItemInHand(new ItemStack(type, 1));
				player.sendMessage(ChatColor.GREEN + "Here's your " + MaterialName.materialName(type));
			} else {
				player.sendMessage(ChatColor.RED + "You have no empty slot in your inventory");
			}
		}
	}

	public static short rawData(ItemStack item) {
		return item.getType() != null ? item.getData() != null ? item.getDurability() : 0 : 0;
	}

	public static int saveSpawnHeight(Location loc) {
		final World world = loc.getWorld();
		final Chunk chunk = world.getChunkAt(loc);
		if (!world.isChunkLoaded(chunk)) {
			world.loadChunk(chunk);
		}
		final int x = loc.getBlockX(), z = loc.getBlockZ();
		int y = loc.getBlockY();
		boolean lower = world.getBlockTypeIdAt(x, y, z) == 0, upper = world.getBlockTypeIdAt(x, y + 1, z) == 0;
		while ((!lower || !upper) && (y != 127)) {
			lower = upper;
			upper = world.getBlockTypeIdAt(x, ++y, z) == 0;
		}
		while ((world.getBlockTypeIdAt(x, y - 1, z) == 0) && (y != 0)) {
			y--;
		}
		return y;
	}

	public static int modifyContainer(BlockState b, ItemStack item) {
		if (b instanceof InventoryHolder) {
			final Inventory inv = ((InventoryHolder)b).getInventory();
			if (item.getAmount() < 0) {
				item.setAmount(-item.getAmount());
				final ItemStack tmp = inv.removeItem(item).get(0);
				return tmp != null ? tmp.getAmount() : 0;
			} else if (item.getAmount() > 0) {
				final ItemStack tmp = inv.addItem(item).get(0);
				return tmp != null ? tmp.getAmount() : 0;
			}
		}
		return 0;
	}

	public static boolean canFall(World world, int x, int y, int z) {
		final Material mat = world.getBlockAt(x, y, z).getType();

		// Air
		if (mat == Material.AIR) {
			return true;
		} else if ((mat == Material.WATER) || (mat == Material.STATIONARY_WATER) || (mat == Material.LAVA) || (mat == Material.STATIONARY_LAVA)) { // Fluids
			return true;
		} else if (BukkitUtils.getFallingEntityKillers().contains(mat) || (mat == Material.FIRE) || (mat == Material.VINE) || (mat == Material.LONG_GRASS) || (mat == Material.DEAD_BUSH)) { // Misc.
			return true;
		}
		return false;
	}

	public static class ItemStackComparator implements Comparator<ItemStack>
	{
		@Override
		public int compare(ItemStack a, ItemStack b) {
			final int aType = a.getTypeId(), bType = b.getTypeId();
			if (aType < bType) {
				return -1;
			}
			if (aType > bType) {
				return 1;
			}
			final short aData = BukkitUtils.rawData(a), bData = BukkitUtils.rawData(b);
			if (aData < bData) {
				return -1;
			}
			if (aData > bData) {
				return 1;
			}
			return 0;
		}
	}
}
