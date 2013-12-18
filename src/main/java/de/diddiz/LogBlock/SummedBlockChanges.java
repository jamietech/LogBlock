package de.diddiz.LogBlock;

import static de.diddiz.util.MaterialName.materialName;
import static de.diddiz.util.Utils.spaces;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Location;
import de.diddiz.LogBlock.QueryParams.SummarizationMode;
import de.diddiz.util.MaterialName;
import de.diddiz.util.Utils;

public class SummedBlockChanges implements LookupCacheElement
{
	private final String group;
	private final int created, destroyed;
	private final float spaceFactor;

	public SummedBlockChanges(ResultSet rs, QueryParams p, float spaceFactor) throws SQLException {
		this.group = p.sum == SummarizationMode.PLAYERS ? rs.getString(1) : MaterialName.materialName(rs.getInt(1));
		this.created = rs.getInt(2);
		this.destroyed = rs.getInt(3);
		this.spaceFactor = spaceFactor;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getMessage() {
		return this.created + Utils.spaces((int)((10 - String.valueOf(this.created).length()) / this.spaceFactor)) + this.destroyed + Utils.spaces((int)((10 - String.valueOf(this.destroyed).length()) / this.spaceFactor)) + this.group;
	}
}
