package de.diddiz.LogBlock;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.diddiz.LogBlock.config.Config;

public class Kill implements LookupCacheElement
{
	final long id, date;
	public final Location loc;
	final String killerName, victimName;
	final int weapon;

	public Kill(String killerName, String victimName, int weapon, Location loc) {
		this.id = 0;
		this.date = System.currentTimeMillis() / 1000;
		this.loc = loc;
		this.killerName = killerName;
		this.victimName = victimName;
		this.weapon = weapon;
	}

	public Kill(ResultSet rs, QueryParams p) throws SQLException {
		this.id = p.needId ? rs.getInt("id") : 0;
		this.date = p.needDate ? rs.getTimestamp("date").getTime() : 0;
		this.loc = p.needCoords ? new Location(p.world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")) : null;
		this.killerName = p.needKiller ? rs.getString("killer") : null;
		this.victimName = p.needVictim ? rs.getString("victim") : null;
		this.weapon = p.needWeapon ? rs.getInt("weapon") : 0;
	}

	@Override
	public String toString() {
		final StringBuilder msg = new StringBuilder();
		if (this.date > 0) {
			msg.append(Config.formatter.format(this.date)).append(" ");
		}
		msg.append(this.killerName).append(" killed ").append(this.victimName);
		if (this.loc != null) {
			msg.append(" at ").append(this.loc.getBlockX()).append(":").append(this.loc.getBlockY()).append(":").append(this.loc.getBlockZ());
		}
		final String weaponName = this.prettyItemName(new ItemStack(this.weapon));
		msg.append(" with " + weaponName); // + ("aeiou".contains(weaponName.substring(0, 1)) ? "an " : "a " )
		return msg.toString();
	}

	@Override
	public Location getLocation() {
		return this.loc;
	}

	@Override
	public String getMessage() {
		return this.toString();
	}

	public String prettyItemName(ItemStack i) {
		String item = i.getType().toString().replace('_', ' ' ).toLowerCase();
		if(item.equals("air")) {
			item = "fist";
		}
		return item;
	}
}
