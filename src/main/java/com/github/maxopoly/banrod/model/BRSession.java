package com.github.maxopoly.banrod.model;

import java.time.Instant;
import java.util.UUID;

import inet.ipaddr.IPAddress;

public class BRSession {
	
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

}
