package com.github.maxopoly.banrod.manager;

import java.time.Instant;
import java.util.List;

import com.github.maxopoly.banrod.model.BRIPBan;
import com.github.maxopoly.banrod.model.BanEffect;
import com.google.common.base.Preconditions;

import inet.ipaddr.IPAddress;

public class IPBanManager {
	
	private BanRodDAO dao;
	
	public IPBanManager(BanRodDAO dao) {
		this.dao = dao;
	}
	
	public BRIPBan<? extends IPAddress> banIP(IPAddress ip, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect) {
		Preconditions.checkNotNull(ip);
		Preconditions.checkNotNull(startingTime);
		Preconditions.checkNotNull(effect);
		return dao.insertIPBan(startingTime, endTime, comment, source, effect, ip);
	}
	
	
	public List<BRIPBan<?>> getIPBans(IPAddress address) {
		return dao.getIPBans(address);
	}

}
