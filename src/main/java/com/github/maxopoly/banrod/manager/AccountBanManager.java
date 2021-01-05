package com.github.maxopoly.banrod.manager;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.maxopoly.banrod.model.BRClusterBan;
import com.github.maxopoly.banrod.model.BRPlayerBan;
import com.github.maxopoly.banrod.model.BRPlayerCluster;
import com.github.maxopoly.banrod.model.BanEffect;
import com.google.common.base.Preconditions;

public class AccountBanManager {
	
	private BanRodDAO dao;
	
	public AccountBanManager(BanRodDAO dao) {
		this.dao = dao;
	}

	public List<BRPlayerBan> getAccountBans(UUID player) {
		return dao.getPlayerBans(player);
	}
	
	public List<BRClusterBan> getClusterBan(UUID player) {
		return Collections.emptyList(); //TODO
	}
	
	public BRClusterBan banCluster(UUID player, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect) {
		
		return null; //TODO
	}

	public BRPlayerBan banAccount(UUID player, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(startingTime);
		Preconditions.checkNotNull(effect);
		return dao.insertPlayerBan(player, startingTime, endTime, comment, source, effect);
	}
	
	public BRPlayerCluster getCluster(UUID player) {
		return new BRPlayerCluster(dao.getAllSharesFor(player));
	}

}
