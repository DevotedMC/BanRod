package com.github.maxopoly.banrod.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import inet.ipaddr.IPAddress;

public class BRSession {

	private static final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault());

	private int id;
	private UUID player;
	private Instant startingTime;
	private Instant endTime;
	private IPAddress ip;
	private boolean forgiven;

	public BRSession(int id, UUID player, Instant startingTime, Instant endTime, IPAddress ip, boolean forgiven) {
		this.id = id;
		this.player = player;
		this.startingTime = startingTime;
		this.endTime = endTime;
		this.ip = ip;
		this.forgiven = forgiven;
	}

	public boolean isForgiven() {
		return forgiven;
	}

	public int getID() {
		return id;
	}

	public UUID getPlayer() {
		return player;
	}

	public Instant getStartingTime() {
		return startingTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public IPAddress getIp() {
		return ip;
	}

	public String toString() {
		return String.format("Session %d, %s on %s from %s to %s%s", id, player, ip, formatter.format(startingTime),
				endTime != null ? formatter.format(endTime) : " - ", forgiven ? "  (forgiven)" : "");
	}

}
