package com.github.maxopoly.banrod.listener;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.github.maxopoly.banrod.BanRodConfig;
import com.github.maxopoly.banrod.BanRodDAO;
import com.github.maxopoly.banrod.model.BRBan;
import com.github.maxopoly.banrod.model.BanEffect;
import com.github.maxopoly.zeus.plugin.event.ZEventHandler;
import com.github.maxopoly.zeus.plugin.event.ZeusListener;
import com.github.maxopoly.zeus.plugin.event.events.PlayerInitialLoginEvent;

import inet.ipaddr.IPAddress;

public class ZeusEventListener implements ZeusListener {
	
	private IPBanManager ipBanManager;
	private BanRodDAO dao;
	private BanRodConfig config;
	
	public ZeusEventListener(IPBanManager ipBanManager, BanRodDAO dao, BanRodConfig config) {
		this.ipBanManager = ipBanManager;
		this.dao = dao;
		this.config = config;
	}
	
	@ZEventHandler
	public void onPlayerLoginAttempt(PlayerInitialLoginEvent event) {
		if (applyActiveBans(event, ipBanManager.getAccountBans(event.getPlayer()))) {
			logAttemptedSession(event.getPlayer(), event.getPlayerIP());
			return;
		}
		if (applyActiveBans(event, ipBanManager.getIPBans(IPAddress.from(event.getPlayerIP())))) {
			logAttemptedSession(event.getPlayer(), event.getPlayerIP());
			return;
		}
		
		
	}
	
	private void logAttemptedSession(UUID player, InetAddress ipv) {
		IPAddress ip = IPAddress.from(ipv);
		Instant now = Instant.now();
		dao.insertSession(player, now, now, ip, config.createShareOnDeniedLogin());
	}
	
	private boolean applyActiveBans(PlayerInitialLoginEvent event, List<? extends BRBan> bans) {
		IPBanManager.filterActiveBans(bans);
		for(BRBan ban : bans) {
			BanEffect effect = ban.getEffect();
			switch (effect) {
			case NOTHING:
				break;
			case BAN_ACCOUNT:
				break;
			case BAN_CLUSTER:
				break;
			case CUSTOM_1:
				break;
			case CUSTOM_2:
				break;
			case CUSTOM_3:
				break;
			case CUSTOM_4:
				break;
			case CUSTOM_5:
				break;
			case DENY_LOGIN:
				break;
			default:
				break;
			
			}
		}
		if (!bans.isEmpty()) {
			event.setCancelled(true);
			event.setDenyMessage(bans.get(0).getBanText());
			return true;
		}
		return false;
	}
	



}
