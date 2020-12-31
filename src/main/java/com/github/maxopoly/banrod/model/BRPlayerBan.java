package com.github.maxopoly.banrod.model;

import java.time.Instant;
import java.util.UUID;

public class BRPlayerBan extends BRBan {
	
	private UUID player;
	
	public BRPlayerBan(int bid, Instant startingTime, Instant endTime, String comment, String source, UUID player, BanEffect effect) {
		super(bid, startingTime, endTime, comment, source, effect);
		this.player = player;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	@Override
	public String getBanText() {
		return "Account Ban  -  " + getFormattedTimeSpan();
	}
}
