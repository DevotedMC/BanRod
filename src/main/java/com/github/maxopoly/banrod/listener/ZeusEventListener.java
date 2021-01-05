package com.github.maxopoly.banrod.listener;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.github.maxopoly.banrod.BanRodConfig;
import com.github.maxopoly.banrod.manager.AccountBanManager;
import com.github.maxopoly.banrod.manager.ActiveSessionTracker;
import com.github.maxopoly.banrod.manager.BanRodDAO;
import com.github.maxopoly.banrod.manager.IPBanManager;
import com.github.maxopoly.banrod.model.BRBan;
import com.github.maxopoly.banrod.model.BRPlayerCluster;
import com.github.maxopoly.banrod.model.BanEffect;
import com.github.maxopoly.zeus.plugin.event.ZEventHandler;
import com.github.maxopoly.zeus.plugin.event.ZeusListener;
import com.github.maxopoly.zeus.plugin.event.events.PlayerFinalDisconnectEvent;
import com.github.maxopoly.zeus.plugin.event.events.PlayerInitialLoginEvent;

import inet.ipaddr.IPAddress;

public class ZeusEventListener implements ZeusListener {

	private IPBanManager ipBanManager;
	private AccountBanManager accountBanManager;
	private BanRodDAO dao;
	private ActiveSessionTracker sessionTracker;
	private BanRodConfig config;
	private Logger logger;

	public ZeusEventListener(AccountBanManager accountBanManager, IPBanManager ipBanManager, BanRodDAO dao,
			BanRodConfig config, ActiveSessionTracker sessionTracker, Logger logger) {
		this.ipBanManager = ipBanManager;
		this.dao = dao;
		this.config = config;
		this.logger = logger;
		this.sessionTracker = sessionTracker;
		this.accountBanManager = accountBanManager;
	}

	@ZEventHandler(priority = 1000)
	public void onPlayerLoginAttempt(PlayerInitialLoginEvent event) {
		if (applyActiveBans(event, accountBanManager.getAccountBans(event.getPlayer()))) {
			logAttemptedSession(event.getPlayer(), event.getPlayerIP());
			return;
		}
		if (applyActiveBans(event, accountBanManager.getClusterBan(event.getPlayer()))) {
			logAttemptedSession(event.getPlayer(), event.getPlayerIP());
			return;
		}
		if (applyActiveBans(event, ipBanManager.getIPBans(IPAddress.from(event.getPlayerIP())))) {
			logAttemptedSession(event.getPlayer(), event.getPlayerIP());
			return;
		}

		// TODO ip lookup
		// TODO meta bans
		// TODO Location based bans
		
		//needs to happen before alts checks so shares are initialized
		Instant now = Instant.now();
		if (!sessionTracker.registerPlayerLogin(event.getPlayer(), IPAddress.from(event.getPlayerIP()), now) ) {
			event.setCancelled(true);
			event.setDenyMessage("Failed to create session entry, broken data, unlucky");
			return;
		}
		BRPlayerCluster cluster = accountBanManager.getCluster(event.getPlayer());
		int position = cluster.getRelativePosition(event.getPlayer());
		if (position >= config.getMaxAccountsPerShare()) {
			event.setCancelled(true);
			event.setDenyMessage(config.getTooManyAccountsMsg());
			logger.info("Denying login of " + event.getPlayerName() + " due to exceeding alt cluster size");
			sessionTracker.registerPlayerLogoff(event.getPlayer(), now);
			return;
		}
	}
	
	@ZEventHandler(priority = 0)
	public void onLogOff(PlayerFinalDisconnectEvent event) {
		sessionTracker.registerPlayerLogoff(event.getPlayer(), Instant.now());
	}

	private void logAttemptedSession(UUID player, InetAddress ipv) {
		IPAddress ip = IPAddress.from(ipv);
		Instant now = Instant.now();
		dao.insertSession(player, now, now, ip, config.createShareOnDeniedLogin());
	}

	private boolean applyActiveBans(PlayerInitialLoginEvent event, List<? extends BRBan> bans) {
		BRBan toApply = null;
		for (BRBan ban : bans) {
			if (!ban.isActive()) {
				continue;
			}
			BanEffect effect = ban.getEffect();
			logger.info(String.format("Applying ban %s to %s", ban.toString(), event.getPlayerName()));
			switch (effect) {
			case NOTHING:
				continue;
			case ACCOUNT_BAN:
				toApply = ban;
				break;
			case CREATE_ACCOUNT_BAN:
				accountBanManager.banAccount(event.getPlayer(), Instant.now(), null, "Autoban based on " + ban.getBid(),
						String.valueOf(ban.getBid()), effect);
				toApply = ban;
				break;
			case CLUSTER_BAN:
				toApply = ban;
				break;
			case CREATE_CLUSTER_BAN:
				toApply = ban;
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

			default:
				break;

			}
		}
		if (toApply != null) {
			event.setCancelled(true);
			event.setDenyMessage(toApply.getBanText());
			return true;
		}
		return false;
	}

}
