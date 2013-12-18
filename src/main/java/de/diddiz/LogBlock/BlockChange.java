package de.diddiz.LogBlock;

import static de.diddiz.util.MaterialName.materialName;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.diddiz.util.BukkitUtils;
import de.diddiz.util.MaterialName;

import org.bukkit.Location;

import de.diddiz.LogBlock.config.Config;
import org.bukkit.Material;

public class BlockChange implements LookupCacheElement
{
	public final long id, date;
	public final Location loc;
	public final String playerName;
	public final int replaced, type;
	public final byte data;
	public final String signtext;
	public final ChestAccess ca;

	public BlockChange(long date, Location loc, String playerName, int replaced, int type, byte data, String signtext, ChestAccess ca) {
		this.id = 0;
		this.date = date;
		this.loc = loc;
		this.playerName = playerName;
		this.replaced = replaced;
		this.type = type;
		this.data = data;
		this.signtext = signtext;
		this.ca = ca;
	}

	public BlockChange(ResultSet rs, QueryParams p) throws SQLException {
		this.id = p.needId ? rs.getInt("id") : 0;
		this.date = p.needDate ? rs.getTimestamp("date").getTime() : 0;
		this.loc = p.needCoords ? new Location(p.world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")) : null;
		this.playerName = p.needPlayer ? rs.getString("playername") : null;
		this.replaced = p.needType ? rs.getInt("replaced") : 0;
		this.type = p.needType ? rs.getInt("type") : 0;
		this.data = p.needData ? rs.getByte("data") : (byte)0;
		this.signtext = p.needSignText ? rs.getString("signtext") : null;
		this.ca = p.needChestAccess && (rs.getShort("itemtype") != 0) && (rs.getShort("itemamount") != 0) ? new ChestAccess(rs.getShort("itemtype"), rs.getShort("itemamount"), rs.getShort("itemdata")) : null;
	}

	@Override
	public String toString() {
		final StringBuilder msg = new StringBuilder();
		if (this.date > 0) {
			msg.append(Config.formatter.format(this.date)).append(" ");
		}
		if (this.playerName != null) {
			msg.append(this.playerName).append(" ");
		}
		if (this.signtext != null) {
			final String action = this.type == 0 ? "destroyed " : "created ";
			if (!this.signtext.contains("\0")) {
				msg.append(action).append(this.signtext);
			} else {
				msg.append(action).append(MaterialName.materialName(this.type != 0 ? this.type : this.replaced)).append(" [").append(this.signtext.replace("\0", "] [")).append("]");
			}
		} else if (this.type == this.replaced) {
			if (this.type == 0) {
				msg.append("did an unspecified action");
			} else if (this.ca != null) {
				if ((this.ca.itemType == 0) || (this.ca.itemAmount == 0)) {
					msg.append("looked inside ").append(MaterialName.materialName(this.type));
				} else if (this.ca.itemAmount < 0) {
					msg.append("took ").append(-this.ca.itemAmount).append("x ").append(MaterialName.materialName(this.ca.itemType, this.ca.itemData)).append(" from ").append(MaterialName.materialName(this.type));
				} else {
					msg.append("put ").append(this.ca.itemAmount).append("x ").append(MaterialName.materialName(this.ca.itemType, this.ca.itemData)).append(" into ").append(MaterialName.materialName(this.type));
				}
			} else if (BukkitUtils.getContainerBlocks().contains(Material.getMaterial(this.type))) {
				msg.append("opened ").append(MaterialName.materialName(this.type));
			} else if ((this.type == 64) || (this.type == 71)) {
				// This is a problem that will have to be addressed in LB 2,
				// there is no way to tell from the top half of the block if
				// the door is opened or closed.
				msg.append("moved ").append(MaterialName.materialName(this.type));
			} else if (this.type == 96) {
				msg.append(((this.data < 8) || (this.data > 11)) ? "opened" : "closed").append(" ").append(MaterialName.materialName(this.type));
			} else if (this.type == 107) {
				msg.append(this.data > 3 ? "opened" : "closed").append(" ").append(MaterialName.materialName(this.type));
			} else if (this.type == 69) {
				msg.append("switched ").append(MaterialName.materialName(this.type));
			} else if ((this.type == 77) || (this.type == 143)) {
				msg.append("pressed ").append(MaterialName.materialName(this.type));
			} else if (this.type == 92) {
				msg.append("ate a piece of ").append(MaterialName.materialName(this.type));
			} else if ((this.type == 25) || (this.type == 93) || (this.type == 94) || (this.type == 149) || (this.type == 150)) {
				msg.append("changed ").append(MaterialName.materialName(this.type));
			} else if ((this.type == 70) || (this.type == 72) || (this.type == 147) || (this.type == 148)) {
				msg.append("stepped on ").append(MaterialName.materialName(this.type));
			} else if (this.type == 132) {
				msg.append("ran into ").append(MaterialName.materialName(this.type));
			}
		} else if (this.type == 0) {
			msg.append("destroyed ").append(MaterialName.materialName(this.replaced, this.data));
		} else if (this.replaced == 0) {
			msg.append("created ").append(MaterialName.materialName(this.type, this.data));
		} else {
			msg.append("replaced ").append(MaterialName.materialName(this.replaced, (byte)0)).append(" with ").append(MaterialName.materialName(this.type, this.data));
		}
		if (this.loc != null) {
			msg.append(" at ").append(this.loc.getBlockX()).append(":").append(this.loc.getBlockY()).append(":").append(this.loc.getBlockZ());
		}
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
}
