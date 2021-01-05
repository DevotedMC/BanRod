package com.github.maxopoly.banrod.manager;

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
import com.github.maxopoly.banrod.model.BRSession;
import com.github.maxopoly.banrod.model.BRShare;
import com.github.maxopoly.banrod.model.BanEffect;
import com.github.maxopoly.zeus.plugin.ZeusPluginDatabase;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
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
						+ " proxy FLOAT," + " source TEXT," + " comment TEXT" + ");",

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
						+ " leave_time TIMESTAMP, ip INET NOT NULL, forgiven BOOLEAN NOT NULL DEFAULT FALSE);",

				"CREATE INDEX br_session_player on br_sessions (player, join_time)",
				"CREATE INDEX br_session_ip on br_sessions (ip, player)",

				"CREATE TABLE IF NOT EXISTS br_shares (shid SERIAL PRIMARY KEY NOT NULL, first_sid SERIAL NOT NULL,"
						+ "second_sid SERIAL NOT NULL, forgiven BOOLEAN NOT NULL DEFAULT FALSE, "
						+ "constraint br_shares_fk_sid1 foreign key (first_sid) references br_sessions (sid) ON DELETE CASCADE,"
						+ "constraint br_shares_fk_sid2 foreign key (second_sid) references br_sessions (sid) ON DELETE CASCADE);",

				"CREATE VIEW extended_shares AS SELECT brsh.shid, brsh.forgiven, brsh.first_sid, brse1.player as player1, brse1.join_time as join_time1, "
						+ "brse1.leave_time as leave_time1, brse1.ip as ip1, brse1.forgiven as forgiven1, brsh.second_sid, brse2.player as player2, "
						+ "brse2.join_time as join_time2, brse2.leave_time as leave_time2, "
						+ "brse2.ip as ip2, brse2.forgiven as forgiven2 FROM br_shares brsh INNER JOIN br_sessions brse1 ON brsh.first_sid = brse1.sid "
						+ "INNER JOIN br_sessions brse2 ON brsh.second_sid = brse2.sid ",

				"CREATE TYPE IF NOT EXISTS br_ban_effect AS ENUM ('NOTHING', 'DENY_LOGIN', "
						+ "'BAN_ACCOUNT', 'BAN_CLUSTER', 'CUSTOM_1', 'CUSTOM_2', 'CUSTOM_3', 'CUSTOM_4', 'CUSTOM_5')",

				"CREATE TABLE IF NOT EXISTS br_ban_meta ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " creation_time TIMESTAMP NOT NULL DEFAULT NOW(), end_time TIMESTAMP, effect br_ban_effect not null, "
						+ "comment TEXT, source TEXT);",

				"CREATE TABLE IF NOT EXISTS br_ban_ip ( bid SERIAL PRIMARY KEY NOT NULL," + " ip CIDR NOT NULL,"
						+ "constraint br_ban_ip_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE TABLE IF NOT EXISTS br_ban_player ( bid SERIAL PRIMARY KEY NOT NULL," + " player uuid not null,"
						+ "constraint br_ban_player_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",
						
				"CREATE TABLE IF NOT EXISTS br_ban_cluster ( bid SERIAL PRIMARY KEY NOT NULL, player uuid not null,"
						+ "constraint br_ban_player_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				// thomasih table
				"CREATE TABLE IF NOT EXISTS br_ban_geoloc ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " lat DOUBLE PRECISION not null, lon DOUBLE PRECISION not null,"
						+ " lat_offset DOUBLE PRECISION not null, lon_offset DOUBLE PRECISION not null,"
						+ "constraint br_ban_geoloc_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE TYPE IF NOT EXISTS br_extra_ban_type AS ENUM ('COUNTRY', 'REGION', "
						+ "'CITY', 'POSTAL', 'DOMAIN', 'REGISTERED_AS', 'CONNECTION')",

				"CREATE TABLE IF NOT EXISTS br_extra_bans ( bid SERIAL PRIMARY KEY NOT NULL,"
						+ " type br_extra_ban_type NOT NULL, content text NOT NULL,"
						+ "constraint br_extra_bans_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE);",

				"CREATE INDEX br_extra_bans_lookup on br_extra_bans (type, content)",

				"CREATE TABLE IF NOT EXISTS br_share_exclusions (seid SERIAL PRIMARY KEY NOT NULL,"
						+ " first_player UUID NOT NULL, second_player UUID NOT NULL)",

				"CREATE INDEX br_share_excl_first on br_share_exclusions (first_player)",
				"CREATE INDEX br_share_excl_second on br_share_exclusions (second_player)",

				"CREATE TABLE IF NOT EXISTS br_ban_exclusions (bid SERIAL NOT NULL,"
						+ " player UUID NOT NULL, PRIMARY KEY(bid, player),"
						+ "constraint br_ban_exclusions_fk_bid foreign key (bid) references br_ban_meta (bid) ON DELETE CASCADE)");
	}

	public BRMetaBan insertMetaBan(int bid, Instant startingTime, Instant endTime, String comment, String source,
			BanEffect effect, BRMetaBanType banType, String condition) {
		int id = createBanMetaEntry(startingTime, endTime, comment, source, effect);
		if (id == -1) {
			return null;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement putIPBan = connection
						.prepareStatement("insert into br_extra_bans (bid, type, content) values(?,?,?)")) {
			putIPBan.setInt(1, id);
			putIPBan.setString(2, banType.toString());
			putIPBan.setString(3, condition);
			putIPBan.execute();
			return new BRMetaBan(bid, startingTime, endTime, comment, source, banType, condition, effect);
		} catch (SQLException e) {
			logger.error("Problem creating meta ban ", e);
			return null;
		}
	}

	public Set<BRShare> getAllSharesFor(UUID uuid) {
		try (Connection connection = db.getConnection();
				PreparedStatement getShares = connection
						.prepareStatement("WITH RECURSIVE alts AS (SELECT es.shid,es.forgiven,es.first_sid,es.player1,es.join_time1,"
								+ "es.leave_time1,es.ip1,es.forgiven1,es.second_sid,es.player2,es.join_time2,es.leave_time2,es.ip2,"
								+ "es.forgiven2 FROM extended_shares es "
								+ "WHERE es.player1 = ? OR es.player2 = ? UNION SELECT es_in.shid,es_in.forgiven,es_in.first_sid,es_in.player1,"
								+ "es_in.join_time1,es_in.leave_time1,es_in.ip1,es_in.forgiven1,es_in.second_sid,es_in.player2,es_in.join_time2,"
								+ "es_in.leave_time2,es_in.ip2,es_in.forgiven2 FROM extended_shares es_in "
								+ "INNER JOIN alts a ON a.player1 = es_in.player1 OR a.player2 = es_in.player2 "
								+ "OR a.player1 = es_in.player2 OR a.player2 = es_in.player1) SELECT * FROM alts")) {
			getShares.setObject(1, uuid);
			getShares.setObject(2, uuid);
			try (ResultSet rs = getShares.executeQuery()) {
				Set<BRShare> result = new HashSet<>();
				while (rs.next()) {
					int shareID = rs.getInt(1);
					boolean forgiven = rs.getBoolean(2);
					BRSession firstSession = parseSession(rs, 3);
					BRSession secondSession = parseSession(rs, 9);
					BRShare share = new BRShare(shareID, firstSession, secondSession, forgiven);
					result.add(share);
				}
				return result;
			}
		} catch (SQLException e) {
			logger.error("Problem selecting alts", e);
			return Collections.emptySet();
		}
	}

	private BRSession parseSession(ResultSet rs, int offset) throws SQLException {
		int id = rs.getInt(offset);
		UUID player = (UUID) rs.getObject(offset + 1);
		Instant start = rs.getTimestamp(offset + 2).toInstant();
		Timestamp endTimeStamp = rs.getTimestamp(offset + 3);
		Instant end = endTimeStamp != null ? endTimeStamp.toInstant() : null;
		IPAddress ip = new IPAddressString(rs.getString(offset + 4)).getAddress();
		boolean forgiven = rs.getBoolean(offset + 5);
		return new BRSession(id, player, start, end, ip, forgiven);
	}

	public BRIPBan<? extends IPAddress> insertIPBan(Instant startingTime, Instant endTime, String comment,
			String source, BanEffect effect, IPAddress ip) {
		int id = createBanMetaEntry(startingTime, endTime, comment, source, effect);
		if (id == -1) {
			return null;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement putIPBan = connection
						.prepareStatement("insert into br_ban_ip (bid, ip) values(?,?::INET)")) {
			putIPBan.setInt(1, id);
			putIPBan.setString(2, ip.toNetworkPrefixLengthString());
			putIPBan.execute();
			if (ip instanceof IPv4Address) {
				return new BRIPv4Ban(id, startingTime, endTime, comment, source, effect, (IPv4Address) ip);
			} else {
				return new BRIPv6Ban(id, startingTime, endTime, comment, source, effect, (IPv6Address) ip);
			}
		} catch (SQLException e) {
			logger.error("Problem creating ip ban ", e);
			return null;
		}
	}

	public BRPlayerBan insertPlayerBan(UUID player, Instant startingTime, Instant endTime, String comment,
			String source, BanEffect effect) {
		int id = createBanMetaEntry(startingTime, endTime, comment, source, effect);
		if (id == -1) {
			return null;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement putPlayerBan = connection
						.prepareStatement("insert into br_ban_player (bid, player) values(?,?)")) {
			putPlayerBan.setInt(1, id);
			putPlayerBan.setObject(2, player);
			putPlayerBan.execute();
			return new BRPlayerBan(id, startingTime, endTime, comment, source, player, effect);
		} catch (SQLException e) {
			logger.error("Problem creating player ban ", e);
			return null;
		}
	}

	private int createBanMetaEntry(Instant startingTime, Instant endTime, String comment, String source,
			BanEffect effect) {
		try (Connection connection = db.getConnection();
				PreparedStatement putBanMeta = connection.prepareStatement(
						"insert into br_ban_meta (creation_time, end_time, effect, comment, source) values (?,?,?,?,?);",
						Statement.RETURN_GENERATED_KEYS)) {
			putBanMeta.setTimestamp(1, Timestamp.from(startingTime));
			putBanMeta.setTimestamp(2, endTime != null ? Timestamp.from(endTime) : null);
			putBanMeta.setString(3, effect.toString());
			putBanMeta.setString(4, comment);
			putBanMeta.setString(5, source);
			putBanMeta.executeUpdate();
			try (ResultSet rs = putBanMeta.getGeneratedKeys()) {
				rs.next();
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("Problem creating player ban meta", e);
			return -1;
		}
	}

	public List<BRPlayerBan> getPlayerBans(UUID player) {
		try (Connection connection = db.getConnection();
				PreparedStatement getBans = connection.prepareStatement(
						"select bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, bbm.effect "
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
					BanEffect effect = BanEffect.valueOf(rs.getString(6));
					result.add(new BRPlayerBan(bid, creationTime, endTime, comment, source, player, effect));
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
				PreparedStatement getBans = connection.prepareStatement(
						"SELECT bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, bbm.effect "
								+ "FROM br_ban_ip bbi JOIN br_ban_meta bbm using (bid) where ?::INET <<= bbi.ip")) {
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
		BanEffect effect = BanEffect.valueOf(rs.getString(6));
		if (ip instanceof IPv4Address) {
			return new BRIPv4Ban(bid, creationTime, endTime, comment, source, effect, (IPv4Address) ip);
		} else {
			return new BRIPv6Ban(bid, creationTime, endTime, comment, source, effect, (IPv6Address) ip);
		}
	}

	public List<BRMetaBan> getMetaBans(String content, BRMetaBanType type) {
		try (Connection connection = db.getConnection();
				PreparedStatement getBans = connection.prepareStatement(
						"select bbm.bid, bbm.creation_time, bbm.end_time, bbm.comment, bbm.source, bbm.effect "
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
					BanEffect effect = BanEffect.valueOf(rs.getString(6));
					result.add(new BRMetaBan(bid, creationTime, endTime, comment, source, type, content, effect));
				}
			}
			return result;
		} catch (SQLException e) {
			logger.error("Problem selecting meta bans ", e);
			return Collections.emptyList();
		}
	}
	
	public void finishOpenSession(int sessionID, Instant endingTime) {
		try (Connection connection = db.getConnection();
				PreparedStatement endSession = connection.prepareStatement(
						"update br_sessions set leave_time = ? where sid = ?")) {
			endSession.setTimestamp(1, Timestamp.from(endingTime));
			endSession.setInt(2, sessionID);
			endSession.execute();
		} catch (SQLException e) {
			logger.error("Problem closing open session", e);
		}
	}

	public int insertSession(UUID player, Instant startingTime, Instant endTime, IPAddress ip, boolean createShare) {
		// load preexisting sessions to update shares
		Map<UUID, Integer> playersInThisShare = new HashMap<>();
		if (createShare) {
			try (Connection connection = db.getConnection();
					PreparedStatement insSession = connection.prepareStatement(
							"SELECT sid, player FROM br_sessions WHERE ip = ?::INET AND forgiven = FALSE ORDER BY join_time ASC")) {
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
				return -1;
			}
		}
		int sessionID = insertSessionEntry(player, startingTime, endTime, ip, !createShare);
		if (sessionID == -1 || !createShare) {
			return sessionID;
		}
		if (playersInThisShare.isEmpty()) {
			return sessionID; // new player on new ip
		}
		if (playersInThisShare.containsKey(player)) {
			return sessionID; // player already has shares with all players on this ip as he played on this IP
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
		return sessionID;
	}

	private int insertSessionEntry(UUID player, Instant startingTime, Instant endTime, IPAddress ip, boolean forgiven) {
		try (Connection connection = db.getConnection();
				PreparedStatement insSession = connection.prepareStatement(
						"INSERT INTO br_sessions(player, join_time, leave_time, ip, forgiven) values(?,?,?,?::INET,?)",
						Statement.RETURN_GENERATED_KEYS)) {
			insSession.setObject(1, player);
			insSession.setTimestamp(2, Timestamp.from(startingTime));
			if (endTime == null) {
				insSession.setTimestamp(3, null);
			} else {
				insSession.setTimestamp(3, Timestamp.from(endTime));
			}
			insSession.setString(4, ip.toCanonicalString());
			insSession.setBoolean(5, forgiven);
			insSession.executeUpdate();
			try (ResultSet rs = insSession.getGeneratedKeys()) {
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
