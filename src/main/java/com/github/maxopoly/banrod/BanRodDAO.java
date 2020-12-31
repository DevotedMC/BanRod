package com.github.maxopoly.banrod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.github.maxopoly.banrod.model.BRIPBan;
import com.github.maxopoly.banrod.model.BRIPv4Ban;
import com.github.maxopoly.banrod.model.BRIPv6Ban;
import com.github.maxopoly.banrod.model.BRMetaBan;
import com.github.maxopoly.banrod.model.BRMetaBanType;
import com.github.maxopoly.banrod.model.BRPlayerBan;
import com.github.maxopoly.zeus.plugin.ZeusPluginDatabase;

import inet.ipaddr.IPAddress;
import inet.ipaddr.ipv4.IPv4Address;
import inet.ipaddr.ipv6.IPv6Address;

public class BanRodDAO extends ZeusPluginDatabase {

	public BanRodDAO(Logger logger) {
		super("BanRod", logger);
		registerMigrations();
	}

	public void registerMigrations() {
		registerMigration(1,

				"CREATE TABLE IF NOT EXISTS br_ip_data (" + " ip INET PRIMARY KEY NOT NULL,"
						+ " creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," + " country VARCHAR(255),"
						+ " region VARCHAR(255)," + " city VARCHAR(255)," + " postal VARCHAR(255),"
						+ " lat DOUBLE PRECISION DEFAULT NULL," + " lon DOUBLE PRECISION DEFAULT NULL,"
						+ " domain TEXT," + " provider TEXT," + " registered_as TEXT," + " connection TEXT,"
						+ " proxy FLOAT," + " source TEXT," + " comment TEXT," + ");",

				"CREATE INDEX br_ip_data_country on br_ip_data (country)",
				"CREATE INDEX br_ip_data_region on br_ip_data (region)",
				"CREATE INDEX br_ip_data_city on br_ip_data (city)",
				"CREATE INDEX br_ip_data_postal on br_ip_data (postal)",
				"CREATE INDEX br_ip_data_lat on br_ip_data (lat)", "CREATE INDEX br_ip_data_lon on br_ip_data (lon)",
				"CREATE INDEX br_ip_data_domain on br_ip_data (domain)",
				"CREATE INDEX br_ip_data_provider on br_ip_data (provider)",
				"CREATE INDEX br_ip_data_registered_as on br_ip_data (registered_as)",
				"CREATE INDEX br_ip_data_connection on br_ip_data (connection)",
				"CREATE INDEX br_ip_data_proxy on br_ip_data (proxy)",
				"CREATE INDEX br_ip_data_source on br_ip_data (source)",

				"CREATE TABLE IF NOT EXISTS br_sessions (sid SERIAL PRIMARY KEY NOT NULL,"
						+ " player uuid NOT NULL, join_time TIMESTAMP NOT NULL DEFAULT NOW(),"
						+ " leave_time TIMESTAMP NOT NULL, ip INET NOT NULL, forgiven BOOLEAN NOT NULL DEFAULT FALSE);",

				"CREATE INDEX br_session_player on br_sessions (player, join_time)",
				"CREATE INDEX br_session_ip on br_sessions (ip, player)",

				"CREATE TABLE IF NOT EXISTS br_shares (shid SERIAL PRIMARY KEY NOT NULL, first_sid SERIAL NOT NULL,"
						+ "second_sid SERIAL NOT NULL, forgiven BOOLEAN NOT NULL DEFAULT FALSE"
						+ "constraint br_shares_fk_sid1 foreign key (first_sid) references br_sessions (sid) ON DELETE CASCADE,"
						+ "constraint br_shares_fk_sid2 foreign key (second_sid) references br_sessions (sid) ON DELETE CASCADE);",

				"CREATE TABLE IF NOT EXISTS br_ban_meta ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " creation_time TIMESTAMP NOT NULL DEFAULT NOW(), end_time TIMESTAMP, comment TEXT,"
						+ " source TEXT);",

				"CREATE TABLE IF NOT EXISTS br_ban_ip ( bid SERIAL PRIMARY KEY NOT NULL," + " ip cidr not null,"
						+ "constraint br_ban_ip_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE TABLE IF NOT EXISTS br_ban_player ( bid SERIAL PRIMARY KEY NOT NULL," + " player uuid not null,"
						+ "constraint br_ban_player_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				// thomasih table
				"CREATE TABLE IF NOT EXISTS br_ban_geoloc ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " lat DOUBLE PRECISION not null, lon DOUBLE PRECISION not null,"
						+ " lat_offset DOUBLE PRECISION not null, lon_offset DOUBLE PRECISION not null,"
						+ "constraint br_ban_geoloc_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE TYPE br_extra_ban_type AS ENUM ('country', 'region', "
						+ "'city', 'postal', 'domain', 'registered_as', 'connection')",

				"CREATE TABLE IF NOT EXISTS br_extra_bans ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " type br_extra_ban_type NOT NULL content text NOT NULL,"
						+ "constraint br_extra_bans_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE INDEX br_extra_bans_lookup on br_extra_bans (type, content)",

				"CREATE TABLE IF NOT EXISTS br_share_exclusions (seid SERIAL PRIMARY KEY NOT NULL,"
						+ " uuid first_player NOT NULL, uuid second_player NOT NULL)",

				"CREATE INDEX br_share_excl_first on br_share_exclusions (first_player)",
				"CREATE INDEX br_share_excl_second on br_share_exclusions (second_player)",

				"CREATE TABLE IF NOT EXISTS br_ban_exclusions (bid SERIAL NOT NULL,"
						+ " player UUID NOT NULL, PRIMARY KEY(bid, player)"
						+ "constraint br_ban_exclusions_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);");
	}

	public List<BRPlayerBan> getPlayerBans(UUID player) {
		try (Connection connection = db.getConnection();
				PreparedStatement getBans = connection
						.prepareStatement("select bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, "
								+ "from br_ban_player bbp inner join br_ban_meta bbm on bbp.bid = bbm.bid "
								+ "where bbp.player = ?")) {
			getBans.setObject(1, player);

			List<BRPlayerBan> result = new ArrayList<>();
			try (ResultSet rs = getBans.executeQuery()) {
				while (rs.next()) {
					int bid = rs.getInt(1);
					Instant creationTime = rs.getTimestamp(2).toInstant();
					Timestamp endTimeStamp = rs.getTimestamp(3);
					Instant endTime = endTimeStamp != null ? endTimeStamp.toInstant() : null;
					String comment = rs.getString(4);
					String source = rs.getString(5);
					result.add(new BRPlayerBan(bid, creationTime, endTime, comment, source, player));
				}
			}
			return result;
		} catch (SQLException e) {
			logger.error("Problem selecting player bans ", e);
			return Collections.emptyList();
		}
	}

	public List<BRIPBan<?>> getIPBans(IPAddress ip) {
		try (Connection connection = db.getConnection();
				PreparedStatement getBans = connection
						.prepareStatement("SELECT bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, "
								+ "FROM br_ban_ip bbi JOIN br_ban_meta bbm using (bid) " + "where ? <<= bbi.ip")) {
			List<BRIPBan<?>> result = new ArrayList<>();
			getBans.setString(1, ip.toNetworkPrefixLengthString());
			try (ResultSet rs = getBans.executeQuery()) {
				while (rs.next()) {
					result.add(createBRIP(ip, rs));
				}
			}
			return result;
		} catch (SQLException e) {
			logger.error("Problem selecting player bans ", e);
			return Collections.emptyList();
		}
	}

	private static BRIPBan<?> createBRIP(IPAddress ip, ResultSet rs) throws SQLException {
		int bid = rs.getInt(1);
		Instant creationTime = rs.getTimestamp(2).toInstant();
		Timestamp endTimeStamp = rs.getTimestamp(3);
		Instant endTime = endTimeStamp != null ? endTimeStamp.toInstant() : null;
		String comment = rs.getString(4);
		String source = rs.getString(5);
		if (ip instanceof IPv4Address) {
			return new BRIPv4Ban(bid, creationTime, endTime, comment, source, (IPv4Address) ip);
		} else {
			return new BRIPv6Ban(bid, creationTime, endTime, comment, source, (IPv6Address) ip);
		}
	}

	public List<BRMetaBan> getMetaBans(String content, BRMetaBanType type) {
		try (Connection connection = db.getConnection();
				PreparedStatement getBans = connection
						.prepareStatement("select bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, "
								+ "from br_extra_bans beb inner join br_ban_meta bbm on beb.bid = bbm.bid "
								+ "where beb.type = ?::br_extra_ban_type and beb.content = ?")) {
			getBans.setString(1, type.getDBIdentifier());
			getBans.setString(2, content);

			List<BRMetaBan> result = new ArrayList<>();
			try (ResultSet rs = getBans.executeQuery()) {
				while (rs.next()) {
					int bid = rs.getInt(1);
					Instant creationTime = rs.getTimestamp(2).toInstant();
					Timestamp endTimeStamp = rs.getTimestamp(3);
					Instant endTime = endTimeStamp != null ? endTimeStamp.toInstant() : null;
					String comment = rs.getString(4);
					String source = rs.getString(5);
					result.add(new BRMetaBan(bid, creationTime, endTime, comment, source, type, content));
				}
			}
			return result;
		} catch (SQLException e) {
			logger.error("Problem selecting meta bans ", e);
			return Collections.emptyList();
		}
	}

	public void insertSession(UUID player, Instant startingTime, Instant endTime, IPAddress ip, boolean createShare) {
		// load preexisting sessions to update shares
		Map<UUID, Integer> playersInThisShare = new HashMap<>();
		if (createShare) {
			try (Connection connection = db.getConnection();
					PreparedStatement insSession = connection.prepareStatement(
							"SELECT sid, player FROM br_sessions WHERE ip = ? AND forgiven = FALSE ORDER BY creation_time ASC")) {
				insSession.setString(1, ip.toCanonicalString());
				try (ResultSet rs = insSession.executeQuery()) {
					while (rs.next()) {
						int id = rs.getInt(1);
						UUID uuid = (UUID) rs.getObject(2);
						// order is ascending by time and we only insert if absent, so the id in the map
						// is the first one the player has for this ip, always
						playersInThisShare.putIfAbsent(uuid, id);
					}
				}
			} catch (SQLException e) {
				logger.error("Problem getting existing sessions ", e);
				return;
			}
		}
		int sessionID = insertSessionEntry(player, startingTime, endTime, ip, !createShare);
		if (sessionID == -1 || !createShare) {
			return;
		}
		if (playersInThisShare.isEmpty()) {
			return; // new player on new ip
		}
		if (playersInThisShare.containsKey(player)) {
			return; // player already has shares with all players on this ip as he played on this IP
					// before
		}
		// create shares between all players who played on this IP and the player
		// first load any exclusions
		// TODO optimize it by combining with some join on the first query?
		Set<UUID> exclusions = getShareExclusionsFor(player);
		// then insert any non-excluded new shares
		for (Entry<UUID, Integer> shareEntry : playersInThisShare.entrySet()) {
			if (exclusions.contains(shareEntry.getKey())) {
				continue;
			}
			insertShare(sessionID, shareEntry.getValue());
		}
	}

	private int insertSessionEntry(UUID player, Instant startingTime, Instant endTime, IPAddress ip, boolean forgiven) {
		try (Connection connection = db.getConnection();
				PreparedStatement insSession = connection.prepareStatement(
						"INSERT INTO br_sessions(player, join_time, leave_time, ip, forgiven) values(?,?,?,?,?)",
						Statement.RETURN_GENERATED_KEYS)) {
			insSession.setObject(1, player);
			insSession.setTimestamp(2, Timestamp.from(startingTime));
			insSession.setTimestamp(3, Timestamp.from(endTime));
			insSession.setString(4, ip.toCanonicalString());
			insSession.setBoolean(5, forgiven);
			try (ResultSet rs = insSession.executeQuery()) {
				rs.next();
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Problem inserting session ", e);
			return -1;
		}
	}

	public Set<UUID> getShareExclusionsFor(UUID player) {
		try (Connection connection = db.getConnection();
				PreparedStatement insSession = connection.prepareStatement(
						"SELECT first_player, second_player from br_share_exclusions where first_player = ? OR second_player = ?")) {
			insSession.setObject(1, player);
			insSession.setObject(2, player);
			Set<UUID> result = new HashSet<>();
			try (ResultSet rs = insSession.executeQuery()) {
				while (rs.next()) {
					UUID first = (UUID) rs.getObject(1);
					UUID second = (UUID) rs.getObject(2);
					if (first.equals(player)) {
						result.add(second);
					} else {
						result.add(first);
					}
				}
			}
			return result;
		} catch (SQLException e) {
			logger.error("Problem getting existing exclusions ", e);
			return Collections.emptySet();
		}
	}

	private void insertShare(int firstSessionID, int secondSessionID) {
		if (firstSessionID == secondSessionID) {
			throw new IllegalArgumentException();
		}
		// always lower one first
		if (secondSessionID < firstSessionID) {
			int temp = secondSessionID;
			secondSessionID = firstSessionID;
			firstSessionID = temp;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement insertShare = connection
						.prepareStatement("INSERT INTO br_shares (first_sid, second_sid) values(?,?)")) {
			insertShare.setInt(1, firstSessionID);
			insertShare.setInt(2, secondSessionID);
			insertShare.execute();

		} catch (SQLException e) {
			logger.error("Problem inserting share", e);
		}
	}
}
