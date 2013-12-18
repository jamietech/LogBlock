package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.util.Block;
import de.diddiz.util.BukkitUtils;
import de.diddiz.util.MaterialName;
import de.diddiz.util.Utils;
import de.diddiz.worldedit.RegionContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.diddiz.LogBlock.Session.getSession;
import static de.diddiz.LogBlock.config.Config.*;
import static de.diddiz.util.BukkitUtils.friendlyWorldname;
import static de.diddiz.util.BukkitUtils.getBlockEquivalents;
import static de.diddiz.util.MaterialName.materialName;
import static de.diddiz.util.Utils.*;

public final class QueryParams implements Cloneable
{
	private static final Set<Integer> keywords = new HashSet<Integer>(Arrays.asList("player".hashCode(), "area".hashCode(), "selection".hashCode(), "sel".hashCode(), "block".hashCode(), "type".hashCode(), "sum".hashCode(), "destroyed".hashCode(), "created".hashCode(), "chestaccess".hashCode(), "all".hashCode(), "time".hashCode(), "since".hashCode(), "before".hashCode(), "limit".hashCode(), "world".hashCode(), "asc".hashCode(), "desc".hashCode(), "last".hashCode(), "coords".hashCode(), "silent".hashCode(), "chat".hashCode(), "search".hashCode(), "match".hashCode(), "loc".hashCode(), "location".hashCode(), "kills".hashCode(), "killer".hashCode(), "victim".hashCode(), "both".hashCode()));
	public BlockChangeType bct = BlockChangeType.BOTH;
	public int limit = -1, before = 0, since = 0, radius = -1;
	public Location loc = null;
	public Order order = Order.DESC;
	public List<String> players = new ArrayList<String>();
	public List<String> killers = new ArrayList<String>();
	public List<String> victims = new ArrayList<String>();
	public boolean excludePlayersMode = false, excludeKillersMode = false, excludeVictimsMode = false, excludeBlocksMode = false, prepareToolQuery = false, silent = false;
	public RegionContainer sel = null;
	public SummarizationMode sum = SummarizationMode.NONE;
	public List<Block> types = new ArrayList<Block>();
	public World world = null;
	public String match = null;
	public boolean needCount = false, needId = false, needDate = false, needType = false, needData = false, needPlayer = false, needCoords = false, needSignText = false, needChestAccess = false, needMessage = false, needKiller = false, needVictim = false, needWeapon = false;
	private final LogBlock logblock;

	public QueryParams(LogBlock logblock) {
		this.logblock = logblock;
	}

	public QueryParams(LogBlock logblock, CommandSender sender, List<String> args) throws IllegalArgumentException {
		this.logblock = logblock;
		this.parseArgs(sender, args);
	}

	public static boolean isKeyWord(String param) {
		return QueryParams.keywords.contains(param.toLowerCase().hashCode());
	}

	public String getLimit() {
		return this.limit > 0 ? "LIMIT " + this.limit : "";
	}

	public String getQuery() {
		if (this.bct == BlockChangeType.CHAT) {
			String select = "SELECT ";
			if (this.needCount) {
				select += "COUNT(*) AS count";
			} else {
				if (this.needId) {
					select += "id, ";
				}
				if (this.needDate) {
					select += "date, ";
				}
				if (this.needPlayer) {
					select += "playername, ";
				}
				if (this.needMessage) {
					select += "message, ";
				}
				select = select.substring(0, select.length() - 2);
			}
			String from = "FROM `lb-chat` ";

			if (this.needPlayer || (this.players.size() > 0)) {
				from += "INNER JOIN `lb-players` USING (playerid) ";
			}
			return select + " " + from + this.getWhere() + "ORDER BY date " + this.order + ", id " + this.order + " " + this.getLimit();
		}
		if (this.bct == BlockChangeType.KILLS) {
			if (this.sum == SummarizationMode.NONE) {
				String select = "SELECT ";
				if (this.needCount) {
					select += "COUNT(*) AS count";
				} else {
					if (this.needId) {
						select += "id, ";
					}
					if (this.needDate) {
						select += "date, ";
					}
					if (this.needPlayer || this.needKiller) {
						select += "killers.playername as killer, ";
					}
					if (this.needPlayer || this.needVictim) {
						select += "victims.playername as victim, ";
					}
					if (this.needWeapon) {
						select += "weapon, ";
					}
					if (this.needCoords) {
						select += "x, y, z, ";
					}
					select = select.substring(0, select.length() - 2);
				}
				String from = "FROM `" + this.getTable() + "-kills` ";

				if (this.needPlayer || this.needKiller || (this.killers.size() > 0)) {
					from += "INNER JOIN `lb-players` as killers ON (killer=killers.playerid) ";
				}

				if (this.needPlayer || this.needVictim || (this.victims.size() > 0)) {
					from += "INNER JOIN `lb-players` as victims ON (victim=victims.playerid) ";
				}

				return select + " " + from + this.getWhere() + "ORDER BY date " + this.order + ", id " + this.order + " " + this.getLimit();
			} else if (this.sum == SummarizationMode.PLAYERS) {
				return "SELECT playername, SUM(kills) AS kills, SUM(killed) AS killed FROM ((SELECT killer AS playerid, count(*) AS kills, 0 as killed FROM `" + this.getTable() + "-kills` INNER JOIN `lb-players` as killers ON (killer=killers.playerid) INNER JOIN `lb-players` as victims ON (victim=victims.playerid) " + this.getWhere(BlockChangeType.KILLS) + "GROUP BY killer) UNION (SELECT victim AS playerid, 0 as kills, count(*) AS killed FROM `" + this.getTable() + "-kills` INNER JOIN `lb-players` as killers ON (killer=killers.playerid) INNER JOIN `lb-players` as victims ON (victim=victims.playerid) " + this.getWhere(BlockChangeType.KILLS) + "GROUP BY victim)) AS t INNER JOIN `lb-players` USING (playerid) GROUP BY playerid ORDER BY SUM(kills) + SUM(killed) " + this.order + " " + this.getLimit();
			}
		}
		if (this.sum == SummarizationMode.NONE) {
			String select = "SELECT ";
			if (this.needCount) {
				select += "COUNT(*) AS count";
			} else {
				if (this.needId) {
					select += "`" + this.getTable() + "`.id, ";
				}
				if (this.needDate) {
					select += "date, ";
				}
				if (this.needType) {
					select += "replaced, type, ";
				}
				if (this.needData) {
					select += "data, ";
				}
				if (this.needPlayer) {
					select += "playername, ";
				}
				if (this.needCoords) {
					select += "x, y, z, ";
				}
				if (this.needSignText) {
					select += "signtext, ";
				}
				if (this.needChestAccess) {
					select += "itemtype, itemamount, itemdata, ";
				}
				select = select.substring(0, select.length() - 2);
			}
			String from = "FROM `" + this.getTable() + "` ";
			if (this.needPlayer || (this.players.size() > 0)) {
				from += "INNER JOIN `lb-players` USING (playerid) ";
			}
			if (this.needSignText) {
				from += "LEFT JOIN `" + this.getTable() + "-sign` USING (id) ";
			}
			if (this.needChestAccess) {
				// If BlockChangeType is CHESTACCESS, we can use more efficient query
				if (this.bct == BlockChangeType.CHESTACCESS) {
					from += "RIGHT JOIN `" + this.getTable() + "-chest` USING (id) ";
				} else {
					from += "LEFT JOIN `" + this.getTable() + "-chest` USING (id) ";
				}
			}
			return select + " " + from + this.getWhere() + "ORDER BY date " + this.order + ", id " + this.order + " " + this.getLimit();
		} else if (this.sum == SummarizationMode.TYPES) {
			return "SELECT type, SUM(created) AS created, SUM(destroyed) AS destroyed FROM ((SELECT type, count(*) AS created, 0 AS destroyed FROM `" + this.getTable() + "` INNER JOIN `lb-players` USING (playerid) " + this.getWhere(BlockChangeType.CREATED) + "GROUP BY type) UNION (SELECT replaced AS type, 0 AS created, count(*) AS destroyed FROM `" + this.getTable() + "` INNER JOIN `lb-players` USING (playerid) " + this.getWhere(BlockChangeType.DESTROYED) + "GROUP BY replaced)) AS t GROUP BY type ORDER BY SUM(created) + SUM(destroyed) " + this.order + " " + this.getLimit();
		} else {
			return "SELECT playername, SUM(created) AS created, SUM(destroyed) AS destroyed FROM ((SELECT playerid, count(*) AS created, 0 AS destroyed FROM `" + this.getTable() + "` " + this.getWhere(BlockChangeType.CREATED) + "GROUP BY playerid) UNION (SELECT playerid, 0 AS created, count(*) AS destroyed FROM `" + this.getTable() + "` " + this.getWhere(BlockChangeType.DESTROYED) + "GROUP BY playerid)) AS t INNER JOIN `lb-players` USING (playerid) GROUP BY playerid ORDER BY SUM(created) + SUM(destroyed) " + this.order + " " + this.getLimit();
		}
	}

	public String getTable() {
		return Config.getWorldConfig(this.world).table;
	}

	public String getTitle() {
		final StringBuilder title = new StringBuilder();
		if (this.bct == BlockChangeType.CHESTACCESS) {
			title.append("chest accesses ");
		} else if (this.bct == BlockChangeType.CHAT) {
			title.append("chat messages ");
		} else if (this.bct == BlockChangeType.KILLS) {
			title.append("kills ");
		} else {
			if (!this.types.isEmpty()) {
				if (this.excludeBlocksMode) {
					title.append("all blocks except ");
				}
				final String[] blocknames = new String[this.types.size()];
				for (int i = 0; i < this.types.size(); i++) {
					blocknames[i] = MaterialName.materialName(this.types.get(i).getBlock());
				}
				title.append(Utils.listing(blocknames, ", ", " and ")).append(" ");
			} else {
				title.append("block ");
			}
			if (this.bct == BlockChangeType.CREATED) {
				title.append("creations ");
			} else if (this.bct == BlockChangeType.DESTROYED) {
				title.append("destructions ");
			} else {
				title.append("changes ");
			}
		}
		if (this.killers.size() > 10) {
			title.append(this.excludeKillersMode ? "without" : "from").append(" many killers ");
		} else if (!this.killers.isEmpty()) {
			title.append(this.excludeKillersMode ? "without" : "from").append(" ").append(Utils.listing(this.killers.toArray(new String[this.killers.size()]), ", ", " and ")).append(" ");
		}
		if (this.victims.size() > 10) {
			title.append(this.excludeVictimsMode ? "without" : "of").append(" many victims ");
		} else if (!this.victims.isEmpty()) {
			title.append(this.excludeVictimsMode ? "without" : "of").append(" victim").append(this.victims.size() != 1 ? "s" : "").append(" ").append(Utils.listing(this.victims.toArray(new String[this.victims.size()]), ", ", " and ")).append(" ");
		}
		if (this.players.size() > 10) {
			title.append(this.excludePlayersMode ? "without" : "from").append(" many players ");
		} else if (!this.players.isEmpty()) {
			title.append(this.excludePlayersMode ? "without" : "from").append(" player").append(this.players.size() != 1 ? "s" : "").append(" ").append(Utils.listing(this.players.toArray(new String[this.players.size()]), ", ", " and ")).append(" ");
		}
		if ((this.match != null) && (this.match.length() > 0)) {
			title.append("matching '").append(this.match).append("' ");
		}
		if ((this.before > 0) && (this.since > 0)) {
			title.append("between ").append(this.since).append(" and ").append(this.before).append(" minutes ago ");
		} else if (this.since > 0) {
			title.append("in the last ").append(this.since).append(" minutes ");
		} else if (this.before > 0) {
			title.append("more than ").append(this.before * -1).append(" minutes ago ");
		}
		if (this.loc != null) {
			if (this.radius > 0) {
				title.append("within ").append(this.radius).append(" blocks of ").append(this.prepareToolQuery ? "clicked block" : "location").append(" ");
			} else if (this.radius == 0) {
				title.append("at ").append(this.loc.getBlockX()).append(":").append(this.loc.getBlockY()).append(":").append(this.loc.getBlockZ()).append(" ");
			}
		} else if (this.sel != null) {
			title.append(this.prepareToolQuery ? "at double chest " : "inside selection ");
		} else if (this.prepareToolQuery) {
			if (this.radius > 0) {
				title.append("within ").append(this.radius).append(" blocks of clicked block ");
			} else if (this.radius == 0) {
				title.append("at clicked block ");
			}
		}
		if ((this.world != null) && !((this.sel != null) && this.prepareToolQuery)) {
			title.append("in ").append(BukkitUtils.friendlyWorldname(this.world.getName())).append(" ");
		}
		if (this.sum != SummarizationMode.NONE) {
			title.append("summed up by ").append(this.sum == SummarizationMode.TYPES ? "blocks" : "players").append(" ");
		}
		title.deleteCharAt(title.length() - 1);
		title.setCharAt(0, String.valueOf(title.charAt(0)).toUpperCase().toCharArray()[0]);
		return title.toString();
	}

	public String getWhere() {
		return this.getWhere(this.bct);
	}

	public String getWhere(BlockChangeType blockChangeType) {
		final StringBuilder where = new StringBuilder("WHERE ");
		if (blockChangeType == BlockChangeType.CHAT) {
			if ((this.match != null) && (this.match.length() > 0)) {
				final boolean unlike = this.match.startsWith("-");
				if (((this.match.length() > 3) && !unlike) || (this.match.length() > 4)) {
					where.append("MATCH (message) AGAINST ('").append(this.match).append("' IN BOOLEAN MODE) AND ");
				} else {
					where.append("message ").append(unlike ? "NOT " : "").append("LIKE '%").append(unlike ? this.match.substring(1) : this.match).append("%' AND ");
				}
			}
		} else if (blockChangeType == BlockChangeType.KILLS) {
			if (!this.players.isEmpty()) {
				if (!this.excludePlayersMode) {
					where.append('(');
					for (final String killerName : this.players) {
						where.append("killers.playername = '").append(killerName).append("' OR ");
					}
					for (final String victimName : this.players) {
						where.append("victims.playername = '").append(victimName).append("' OR ");
					}
					where.delete(where.length() - 4, where.length());
					where.append(") AND ");
				} else {
					for (final String killerName : this.players) {
						where.append("killers.playername != '").append(killerName).append("' AND ");
					}
					for (final String victimName : this.players) {
						where.append("victims.playername != '").append(victimName).append("' AND ");
					}
				}
			}

			if (!this.killers.isEmpty()) {
				if (!this.excludeKillersMode) {
					where.append('(');
					for (final String killerName : this.killers) {
						where.append("killers.playername = '").append(killerName).append("' OR ");
					}
					where.delete(where.length() - 4, where.length());
					where.append(") AND ");
				} else {
					for (final String killerName : this.killers) {
						where.append("killers.playername != '").append(killerName).append("' AND ");
					}
				}
			}

			if (!this.victims.isEmpty()) {
				if (!this.excludeVictimsMode) {
					where.append('(');
					for (final String victimName : this.victims) {
						where.append("victims.playername = '").append(victimName).append("' OR ");
					}
					where.delete(where.length() - 4, where.length());
					where.append(") AND ");
				} else {
					for (final String victimName : this.victims) {
						where.append("victims.playername != '").append(victimName).append("' AND ");
					}
				}
			}

            if (this.loc != null) {
                if (this.radius == 0) {
					this.compileLocationQuery(
                            where,
                            this.loc.getBlockX(), this.loc.getBlockX(),
                            this.loc.getBlockY(), this.loc.getBlockY(),
                            this.loc.getBlockZ(), this.loc.getBlockZ()
                            );
				} else if (this.radius > 0) {
					this.compileLocationQuery(
                            where,
                            (this.loc.getBlockX() - this.radius) + 1, (this.loc.getBlockX() + this.radius) - 1,
                            (this.loc.getBlockY() - this.radius) + 1, (this.loc.getBlockY() + this.radius) - 1,
                            (this.loc.getBlockZ() - this.radius) + 1, (this.loc.getBlockZ() + this.radius) - 1
                            );
				}

            } else if (this.sel != null) {
				this.compileLocationQuery(
                        where,
                        this.sel.getSelection().getMinimumPoint().getBlockX(), this.sel.getSelection().getMaximumPoint().getBlockX(),
                        this.sel.getSelection().getMinimumPoint().getBlockY(), this.sel.getSelection().getMaximumPoint().getBlockY(),
                        this.sel.getSelection().getMinimumPoint().getBlockZ(), this.sel.getSelection().getMaximumPoint().getBlockZ()
                        );
			}

		} else {
			switch (blockChangeType) {
				case ALL:
					if (!this.types.isEmpty()) {
						if (this.excludeBlocksMode) {
							where.append("NOT ");
						}
						where.append('(');
						for (final Block block : this.types) {
							where.append("((type = ").append(block.getBlock()).append(" OR replaced = ").append(block.getBlock());
							if (block.getData() != -1) {
								where.append(") AND data = ").append(block.getData());
							} else {
								where.append(")");
							}
							where.append(") OR ");
						}
						where.delete(where.length() - 4, where.length() - 1);
						where.append(") AND ");
					}
					break;
				case BOTH:
					if (!this.types.isEmpty()) {
						if (this.excludeBlocksMode) {
							where.append("NOT ");
						}
						where.append('(');
						for (final Block block : this.types) {
							where.append("((type = ").append(block.getBlock()).append(" OR replaced = ").append(block.getBlock());
							if (block.getData() != -1) {
								where.append(") AND data = ").append(block.getData());
							} else {
								where.append(")");
							}
							where.append(") OR ");
						}
						where.delete(where.length() - 4, where.length());
						where.append(") AND ");
					}
					where.append("type != replaced AND ");
					break;
				case CREATED:
					if (!this.types.isEmpty()) {
						if (this.excludeBlocksMode) {
							where.append("NOT ");
						}
						where.append('(');
						for (final Block block : this.types) {
							where.append("((type = ").append(block.getBlock());
							if (block.getData() != -1) {
								where.append(") AND data = ").append(block.getData());
							} else {
								where.append(")");
							}
							where.append(") OR ");
						}
						where.delete(where.length() - 4, where.length());
						where.append(") AND ");
					}
					where.append("type != 0 AND type != replaced AND ");
					break;
				case DESTROYED:
					if (!this.types.isEmpty()) {
						if (this.excludeBlocksMode) {
							where.append("NOT ");
						}
						where.append('(');
						for (final Block block : this.types) {
							where.append("((replaced = ").append(block.getBlock());
							if (block.getData() != -1) {
								where.append(") AND data = ").append(block.getData());
							} else {
								where.append(")");
							}
							where.append(") OR ");
						}
						where.delete(where.length() - 4, where.length());
						where.append(") AND ");
					}
					where.append("replaced != 0 AND type != replaced AND ");
					break;
				case CHESTACCESS:
					if (!this.types.isEmpty()) {
						if (this.excludeBlocksMode) {
							where.append("NOT ");
						}
						where.append('(');
						for (final Block block : this.types) {
							where.append("((itemtype = ").append(block.getBlock());
							if (block.getData() != -1) {
								where.append(") AND itemdata = ").append(block.getData());
							} else {
								where.append(")");
							}
							where.append(") OR ");
						}
						where.delete(where.length() - 4, where.length());
						where.append(") AND ");
					}
					break;
			}
            if (this.loc != null) {
                if (this.radius == 0) {
					this.compileLocationQuery(
                            where,
                            this.loc.getBlockX(), this.loc.getBlockX(),
                            this.loc.getBlockY(), this.loc.getBlockY(),
                            this.loc.getBlockZ(), this.loc.getBlockZ()
                            );
				} else if (this.radius > 0) {
					this.compileLocationQuery(
                            where,
                            (this.loc.getBlockX() - this.radius) + 1, (this.loc.getBlockX() + this.radius) - 1,
                            (this.loc.getBlockY() - this.radius) + 1, (this.loc.getBlockY() + this.radius) - 1,
                            (this.loc.getBlockZ() - this.radius) + 1, (this.loc.getBlockZ() + this.radius) - 1
                            );
				}

            } else if (this.sel != null) {
				this.compileLocationQuery(
                        where,
                        this.sel.getSelection().getMinimumPoint().getBlockX(), this.sel.getSelection().getMaximumPoint().getBlockX(),
                        this.sel.getSelection().getMinimumPoint().getBlockY(), this.sel.getSelection().getMaximumPoint().getBlockY(),
                        this.sel.getSelection().getMinimumPoint().getBlockZ(), this.sel.getSelection().getMaximumPoint().getBlockZ()
                        );
			}

		}
		if (!this.players.isEmpty() && (this.sum != SummarizationMode.PLAYERS) && (blockChangeType != BlockChangeType.KILLS)) {
			if (!this.excludePlayersMode) {
				where.append('(');
				for (final String playerName : this.players) {
					where.append("playername = '").append(playerName).append("' OR ");
				}
				where.delete(where.length() - 4, where.length());
				where.append(") AND ");
			} else {
				for (final String playerName : this.players) {
					where.append("playername != '").append(playerName).append("' AND ");
				}
			}
		}
		if (this.since > 0) {
			where.append("date > date_sub(now(), INTERVAL ").append(this.since).append(" MINUTE) AND ");
		}
		if (this.before > 0) {
			where.append("date < date_sub(now(), INTERVAL ").append(this.before).append(" MINUTE) AND ");
		}
		if (where.length() > 6) {
			where.delete(where.length() - 4, where.length());
		} else {
			where.delete(0, where.length());
		}
		return where.toString();
	}
	
	private void compileLocationQuery(StringBuilder where, int blockX, int blockX2, int blockY, int blockY2, int blockZ, int blockZ2) {
	    this.compileLocationQueryPart(where, "x", blockX, blockX2);
	    where.append(" AND ");
        this.compileLocationQueryPart(where, "y", blockY, blockY2);
        where.append(" AND ");
        this.compileLocationQueryPart(where, "z", blockZ, blockZ2);
        where.append(" AND ");
    }

    private void compileLocationQueryPart(StringBuilder where, String locValue, int loc, int loc2) {
        final int min = Math.min(loc, loc2);
        final int max = Math.max(loc2, loc);
        
        if (min == max) {
			where.append(locValue).append(" = ").append(min);
		} else if ((max - min) > 50) {
			where.append(locValue).append(" >= ").append(min).append(" AND ").append(locValue).append(" <= ").append(max);
		} else {
            where.append(locValue).append(" in (");
            for (int c = min; c < max; c++) {
                where.append(c).append(",");
            }
            where.append(max);
            where.append(")");
        }
    }

	public void parseArgs(CommandSender sender, List<String> args) throws IllegalArgumentException {
		if ((args == null) || args.isEmpty()) {
			throw new IllegalArgumentException("No parameters specified.");
		}
		final Player player = sender instanceof Player ? (Player)sender : null;
		final Session session = this.prepareToolQuery ? null : Session.getSession(sender);
		if ((player != null) && (this.world == null)) {
			this.world = player.getWorld();
		}
		for (int i = 0; i < args.size(); i++) {
			final String param = args.get(i).toLowerCase();
			final String[] values = QueryParams.getValues(args, i + 1);
			if (param.equals("last")) {
				if (session.lastQuery == null) {
					throw new IllegalArgumentException("This is your first command, you can't use last.");
				}
				this.merge(session.lastQuery);
			} else if (param.equals("player")) {
				if (values.length < 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				for (final String playerName : values) {
					if (playerName.length() > 0) {
						if (playerName.contains("!")) {
							this.excludePlayersMode = true;
						}
						if (playerName.contains("\"")) {
							this.players.add(playerName.replaceAll("[^a-zA-Z0-9_]", ""));
						} else {
							final List<Player> matches = this.logblock.getServer().matchPlayer(playerName);
							if (matches.size() > 1) {
								throw new IllegalArgumentException("Ambiguous playername '" + param + "'");
							}
							this.players.add(matches.size() == 1 ? matches.get(0).getName() : playerName.replaceAll("[^a-zA-Z0-9_]", ""));
						}
					}
				}
				this.needPlayer = true;
			} else if (param.equals("killer")) {
				if (values.length < 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				for (final String killerName : values) {
					if (killerName.length() > 0) {
						if (killerName.contains("!")) {
							this.excludeVictimsMode = true;
						}
						if (killerName.contains("\"")) {
							this.killers.add(killerName.replaceAll("[^a-zA-Z0-9_]", ""));
						} else {
							final List<Player> matches = this.logblock.getServer().matchPlayer(killerName);
							if (matches.size() > 1) {
								throw new IllegalArgumentException("Ambiguous victimname '" + param + "'");
							}
							this.killers.add(matches.size() == 1 ? matches.get(0).getName() : killerName.replaceAll("[^a-zA-Z0-9_]", ""));
						}
					}
				}
				this.needKiller = true;
			} else if (param.equals("victim")) {
				if (values.length < 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				for (final String victimName : values) {
					if (victimName.length() > 0) {
						if (victimName.contains("!")) {
							this.excludeVictimsMode = true;
						}
						if (victimName.contains("\"")) {
							this.victims.add(victimName.replaceAll("[^a-zA-Z0-9_]", ""));
						} else {
							final List<Player> matches = this.logblock.getServer().matchPlayer(victimName);
							if (matches.size() > 1) {
								throw new IllegalArgumentException("Ambiguous victimname '" + param + "'");
							}
							this.victims.add(matches.size() == 1 ? matches.get(0).getName() : victimName.replaceAll("[^a-zA-Z0-9_]", ""));
						}
					}
				}
				this.needVictim = true;
			} else if (param.equals("weapon")) {
				if (values.length < 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				for (final String weaponName : values) {
					Material mat = Material.matchMaterial(weaponName);
					if (mat == null) {
						try {
							mat = Material.getMaterial(Integer.parseInt(weaponName));
						} catch (final NumberFormatException e) {
							throw new IllegalArgumentException("Data type not a valid number: '" + weaponName + "'");
						}
					}
					if (mat == null) {
						throw new IllegalArgumentException("No material matching: '" + weaponName + "'");
					}
					this.types.add(new Block(mat.getId(), -1));
				}
				this.needWeapon = true;
			} else if (param.equals("block") || param.equals("type")) {
				if (values.length < 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				for (String blockName : values) {
					if (blockName.startsWith("!")) {
						this.excludeBlocksMode = true;
						blockName = blockName.substring(1);
					}
					if (blockName.contains(":")) {
						final String[] blockNameSplit = blockName.split(":");
						if (blockNameSplit.length > 2) {
							throw new IllegalArgumentException("No material matching: '" + blockName + "'");
						}
						final int data;
						try {
							data = Integer.parseInt(blockNameSplit[1]);
						} catch (final NumberFormatException e) {
							throw new IllegalArgumentException("Data type not a valid number: '" + blockNameSplit[1] + "'");
						}
						if ((data > 255) || (data < 0)) {
							throw new IllegalArgumentException("Data type out of range (0-255): '" + data + "'");
						}
						final Material mat = Material.matchMaterial(blockNameSplit[0]);
						if (mat == null) {
							throw new IllegalArgumentException("No material matching: '" + blockName + "'");
						}
						this.types.add(new Block(mat.getId(), data));
					} else {
						final Material mat = Material.matchMaterial(blockName);
						if (mat == null) {
							throw new IllegalArgumentException("No material matching: '" + blockName + "'");
						}
						this.types.add(new Block(mat.getId(), -1));
					}
				}
			} else if (param.equals("area")) {
				if ((player == null) && !this.prepareToolQuery && (this.loc == null)) {
					throw new IllegalArgumentException("You have to be a player to use area, or specify a location first");
				}
				if (values.length == 0) {
					this.radius = Config.defaultDist;
					if (!this.prepareToolQuery && (this.loc == null)) {
						this.loc = player.getLocation();
					}
				} else {
					if (!Utils.isInt(values[0])) {
						throw new IllegalArgumentException("Not a number: '" + values[0] + "'");
					}
					this.radius = Integer.parseInt(values[0]);
					if (!this.prepareToolQuery && (this.loc == null)) {
						this.loc = player.getLocation();
					}
				}
			} else if (param.equals("selection") || param.equals("sel")) {
				if (player == null) {
					throw new IllegalArgumentException("You have to ba a player to use selection");
				}
				final Plugin we = player.getServer().getPluginManager().getPlugin("WorldEdit");
				if (we != null) {
					this.setSelection(RegionContainer.fromPlayerSelection(player, we));
				} else {
					throw new IllegalArgumentException("WorldEdit not found!");
				}
			} else if (param.equals("time") || param.equals("since")) {
				this.since = values.length > 0 ? Utils.parseTimeSpec(values) : Config.defaultTime;
				if (this.since == -1) {
					throw new IllegalArgumentException("Failed to parse time spec for '" + param + "'");
				}
			} else if (param.equals("before")) {
				this.before = values.length > 0 ? Utils.parseTimeSpec(values) : Config.defaultTime;
				if (this.before == -1) {
					throw new IllegalArgumentException("Faile to parse time spec for '" + param + "'");
				}
			} else if (param.equals("sum")) {
				if (values.length != 1) {
					throw new IllegalArgumentException("No or wrong count of arguments for '" + param + "'");
				}
				if (values[0].startsWith("p")) {
					this.sum = SummarizationMode.PLAYERS;
				} else if (values[0].startsWith("b")) {
					this.sum = SummarizationMode.TYPES;
				} else if (values[0].startsWith("n")) {
					this.sum = SummarizationMode.NONE;
				} else {
					throw new IllegalArgumentException("Wrong summarization mode");
				}
			} else if (param.equals("created")) {
				this.bct = BlockChangeType.CREATED;
			} else if (param.equals("destroyed")) {
				this.bct = BlockChangeType.DESTROYED;
			} else if (param.equals("both")) {
				this.bct = BlockChangeType.BOTH;
			} else if (param.equals("chestaccess")) {
				this.bct = BlockChangeType.CHESTACCESS;
			} else if (param.equals("chat")) {
				this.bct = BlockChangeType.CHAT;
			} else if (param.equals("kills")) {
				this.bct = BlockChangeType.KILLS;
			}
			else if (param.equals("all")) {
				this.bct = BlockChangeType.ALL;
			} else if (param.equals("limit")) {
				if (values.length != 1) {
					throw new IllegalArgumentException("Wrong count of arguments for '" + param + "'");
				}
				if (!Utils.isInt(values[0])) {
					throw new IllegalArgumentException("Not a number: '" + values[0] + "'");
				}
				this.limit = Integer.parseInt(values[0]);
			} else if (param.equals("world")) {
				if (values.length != 1) {
					throw new IllegalArgumentException("Wrong count of arguments for '" + param + "'");
				}
				final World w = sender.getServer().getWorld(values[0].replace("\"", ""));
				if (w == null) {
					throw new IllegalArgumentException("There is no world called '" + values[0] + "'");
				}
				this.world = w;
			} else if (param.equals("asc")) {
				this.order = Order.ASC;
			} else if (param.equals("desc")) {
				this.order = Order.DESC;
			} else if (param.equals("coords")) {
				this.needCoords = true;
			} else if (param.equals("silent")) {
				this.silent = true;
			} else if (param.equals("search") || param.equals("match")) {
				if (values.length == 0) {
					throw new IllegalArgumentException("No arguments for '" + param + "'");
				}
				this.match = Utils.join(values, " ").replace("\\", "\\\\").replace("'", "\\'");
			} else if (param.equals("loc") || param.equals("location")) {
				final String[] vectors = values.length == 1 ? values[0].split(":") : values;
				if (vectors.length != 3) {
					throw new IllegalArgumentException("Wrong count arguments for '" + param + "'");
				}
				for (final String vec : vectors) {
					if (!Utils.isInt(vec)) {
						throw new IllegalArgumentException("Not a number: '" + vec + "'");
					}
				}
				this.loc = new Location(null, Integer.valueOf(vectors[0]), Integer.valueOf(vectors[1]), Integer.valueOf(vectors[2]));
				this.radius = 0;
			} else {
				throw new IllegalArgumentException("Not a valid argument: '" + param + "'");
			}
			i += values.length;
		}
		if (this.bct == BlockChangeType.KILLS) {
			if (this.world == null) {
				throw new IllegalArgumentException("No world specified");
			}
			if (!Config.getWorldConfig(this.world).isLogging(Logging.KILL)) {
				throw new IllegalArgumentException("Kill logging not enabled for world '" + this.world.getName() + "'");
			}
		}
		if (this.types.size() > 0) {
			for (final Set<Integer> equivalent : BukkitUtils.getBlockEquivalents()) {
				boolean found = false;
				for (final Block block : this.types) {
					if (equivalent.contains(block.getBlock())) {
						found = true;
						break;
					}
				}
				if (found) {
					for (final Integer type : equivalent) {
						if (!Block.inList(this.types, type)) {
							this.types.add(new Block(type, -1));
						}
					}
				}
			}
		}
		if (!this.prepareToolQuery && (this.bct != BlockChangeType.CHAT)) {
			if (this.world == null) {
				throw new IllegalArgumentException("No world specified");
			}
			if (!Config.isLogged(this.world)) {
				throw new IllegalArgumentException("This world ('" + this.world.getName() + "') isn't logged");
			}
		}
		if (session != null) {
			session.lastQuery = this.clone();
		}
	}

	public void setLocation(Location loc) {
		this.loc = loc;
		this.world = loc.getWorld();
	}

	public void setSelection(RegionContainer container) {
		this.sel = container;
		this.world = this.sel.getSelection().getWorld();
	}

	public void setPlayer(String playerName) {
		this.players.clear();
		this.players.add(playerName);
	}

	@Override
	protected QueryParams clone() {
		try {
			final QueryParams params = (QueryParams)super.clone();
			params.players = new ArrayList<String>(this.players);
			params.types = new ArrayList<Block>(this.types);
			return params;
		} catch (final CloneNotSupportedException ex) {
		}
		return null;
	}

	private static String[] getValues(List<String> args, int offset) {
		// The variable i will store the last value's index
		int i;
		// Iterate over the all the values from the offset up till the end
		for (i = offset; i < args.size(); i++) {
			// We found a keyword, break here since anything after this isn't a value.
			if (QueryParams.isKeyWord(args.get(i))) {
				break;
			}
		}
		// If the offset equals to the last value index, return an empty string array
		if (i == offset) {
			return new String[0];
		}
		// Instantiate a new string array with the total indexes required
		final List<String> values = new ArrayList<String>();
		// Buffer for the value
		String value = "";
		// Iterate over the offset up till the last index value
		for (int j = offset; j < i; j++) {
			// If the value starts with a double quote or we're already dealing with a quoted value
			if (args.get(j).startsWith("\"") || !value.equals("")) {
				// If the value doesn't end with a double quote
				if (!args.get(j).endsWith("\"")) {
					// Add the argument to the value buffer after stripping out the initial quote
					
					// If the argument starts with a quote we wanna strip that out otherwise add it normally
					if (args.get(j).startsWith("\"")) {
						value += args.get(j).substring(1) + " ";
					} else {
						value += args.get(j) + " ";
					}
					
				} else {
					// The value ends with a double quote
					
					// If the argument starts with a double quote we wanna strip that out too along with the end quote
					if (args.get(j).startsWith("\"")) {
						value += args.get(j).substring(0, args.get(j).length() - 1).substring(1);
					} else {
					// Looks like its just the end quote here, just need to strip that out
						value += args.get(j).substring(0, args.get(j).length() - 1);
					}
					// Add the value to the main values list
					values.add(value);
					// Reset the buffer
					value = "";
				}
			} else {
				// Set the value in the array to be returned to the one from the main arguments list
				values.add(args.get(j));
			}
		}
		// Return the values array
		return values.toArray(new String[values.size()]);
	}

	public void merge(QueryParams p) {
		this.players = p.players;
		this.excludePlayersMode = p.excludePlayersMode;
		this.types = p.types;
		this.loc = p.loc;
		this.radius = p.radius;
		this.sel = p.sel;
		if ((p.since != 0) || (this.since != Config.defaultTime)) {
			this.since = p.since;
		}
		this.before = p.before;
		this.sum = p.sum;
		this.bct = p.bct;
		this.limit = p.limit;
		this.world = p.world;
		this.order = p.order;
		this.match = p.match;
	}

	public static enum BlockChangeType
	{
		ALL, BOTH, CHESTACCESS, CREATED, DESTROYED, CHAT, KILLS
	}

	public static enum Order
	{
		ASC, DESC
	}

	public static enum SummarizationMode
	{
		NONE, PLAYERS, TYPES
	}
}
