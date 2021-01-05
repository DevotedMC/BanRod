package com.github.maxopoly.banrod.model;

import java.time.Instant;
import java.util.UUID;

public class BRClusterBan extends BRPlayerBan {

	public BRClusterBan(int bid, Instant startingTime, Instant endTime, String comment, String source, UUID player,
			BanEffect effect) {
		super(bid, startingTime, endTime, comment, source, player, effect);
	}

	@Override
	public String getBanText() {
		return "Cluster ban originating from " + getPlayer();
	}

}
