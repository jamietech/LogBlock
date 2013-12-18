package de.diddiz.LogBlock;

import java.sql.ResultSet;
import java.sql.SQLException;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;
import de.diddiz.LogBlock.QueryParams.SummarizationMode;

public class LookupCacheElementFactory
{
	private final QueryParams params;
	private final float spaceFactor;

	public LookupCacheElementFactory(QueryParams params, float spaceFactor) {
		this.params = params;
		this.spaceFactor = spaceFactor;
	}

	public LookupCacheElement getLookupCacheElement(ResultSet rs) throws SQLException {
		if (this.params.bct == BlockChangeType.CHAT) {
			return new ChatMessage(rs, this.params);
		}
		if (this.params.bct == BlockChangeType.KILLS) {
			if (this.params.sum == SummarizationMode.NONE) {
				return new Kill(rs, this.params);
			} else if (this.params.sum == SummarizationMode.PLAYERS) {
				return new SummedKills(rs, this.params, this.spaceFactor);
			}
		}
		if (this.params.sum == SummarizationMode.NONE) {
			return new BlockChange(rs, this.params);
		}
		return new SummedBlockChanges(rs, this.params, this.spaceFactor);
	}
}
