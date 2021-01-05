package com.github.maxopoly.banrod.manager;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

import inet.ipaddr.IPAddress;

/**
 * Tracks active sessions of online players and when they started
 *
 */
public class ActiveSessionTracker {

	private Map<UUID, Integer> sessionIds;
	private Logger logger;
	private BanRodDAO dao;

	public ActiveSessionTracker(Logger logger, BanRodDAO dao) {
		this.logger = logger;
		this.dao = dao;
		this.sessionIds = new HashMap<>();
	}

	/**
	 * Begins tracking a player session beginning now and inserts into the database without an ending time
	 * 
	 * @param uuid UUID of the player who logged in and started a session
	 * @param ip   IP the player logged in on
	 */
	public synchronized boolean registerPlayerLogin(UUID uuid, IPAddress ip, Instant time) {
		Preconditions.checkNotNull(uuid);
		if (sessionIds.containsKey(uuid)) {
			logger.error("Session was started for " + uuid + " but there was already an existing one?");
			//try to fix by logging off now, next login attempt should work
			registerPlayerLogoff(uuid, time);
			return false;
		}
		int id = dao.insertSession(uuid, time, null, ip, true);
		if (id == -1) {
			logger.error("Failed to insert session");
			return false;
		}
		sessionIds.put(uuid, id);
		return true;
	}

	/**
	 * Ends a running player session and updates the database with the current
	 * time as ending time of the session
	 * 
	 * @param uuid UUID of the player who logged off and whose session should be
	 *             ended
	 */
	public synchronized void registerPlayerLogoff(UUID uuid, Instant time) {
		Preconditions.checkNotNull(uuid);
		Integer sessionID = sessionIds.remove(uuid);
		if (sessionID == null) {
			logger.error("Failed to close session of " + uuid + ". No beginning existed");
			return;
		}
		dao.finishOpenSession(sessionID, time);
	}

}
