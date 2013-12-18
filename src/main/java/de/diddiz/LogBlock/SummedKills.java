package de.diddiz.LogBlock;

import static de.diddiz.util.Utils.spaces;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Location;
import de.diddiz.util.Utils;

public class SummedKills implements LookupCacheElement
{
	private final String playerName;
	private final int kills, killed;
	private final float spaceFactor;

	public SummedKills(ResultSet rs, QueryParams p, float spaceFactor) throws SQLException {
		this.playerName = rs.getString(1);
		this.kills = rs.getInt(2);
		this.killed = rs.getInt(3);
		this.spaceFactor = spaceFactor;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getMessage() {
		return this.kills + Utils.spaces((int)((6 - String.valueOf(this.kills).length()) / this.spaceFactor)) + this.killed + Utils.spaces((int)((7 - String.valueOf(this.killed).length()) / this.spaceFactor)) + this.playerName;
	}
}
