package com.github.maxopoly.banrod.listener;

import java.util.List;
import java.util.UUID;

import com.github.maxopoly.banrod.model.BRBan;
import com.github.maxopoly.banrod.model.BRIPBan;
import com.github.maxopoly.banrod.model.BRPlayerBan;

import inet.ipaddr.IPAddress;

public class IPBanManager {
	
	public List<BRPlayerBan> getAccountBans(UUID uuid) {
		
	}
	
	public List<BRIPBan<?>> getIPBans(IPAddress address) {
		
	}
	
	
	public static <T extends BRBan> void filterActiveBans(List<T> bans) {
		bans.removeIf(b -> !b.isActive());
	}

}
