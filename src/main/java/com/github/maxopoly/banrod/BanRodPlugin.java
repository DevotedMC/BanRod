package com.github.maxopoly.banrod;

import com.github.maxopoly.banrod.listener.ZeusEventListener;
import com.github.maxopoly.banrod.manager.AccountBanManager;
import com.github.maxopoly.banrod.manager.ActiveSessionTracker;
import com.github.maxopoly.banrod.manager.BanRodDAO;
import com.github.maxopoly.banrod.manager.IPBanManager;
import com.github.maxopoly.zeus.plugin.ZeusLoad;
import com.github.maxopoly.zeus.plugin.ZeusPlugin;

@ZeusLoad(name = "BanRod", version = "1.0", description = "Advanced ban handling")
public class BanRodPlugin extends ZeusPlugin {
	
	private static BanRodPlugin instance;

	public static BanRodPlugin getInstance() {
		return instance;
	}
	
	private BanRodDAO dao;
	private IPBanManager ipBanManager;
	private AccountBanManager accBanManager;
	private ActiveSessionTracker sessionTracker;
	private BanRodConfig config;

	@Override
	public boolean onEnable() {
		instance = this;
		this.config = new BanRodConfig(getConfig());
		this.dao = new BanRodDAO(logger);
		if (!dao.updateDatabase()) {
			return false;
		}
		this.accBanManager = new AccountBanManager(dao);
		this.ipBanManager = new IPBanManager(dao);
		this.sessionTracker = new ActiveSessionTracker(logger, dao);
		registerPluginlistener(new ZeusEventListener(accBanManager, ipBanManager, dao, config, sessionTracker, logger));
		return true;
	}
	
	public BanRodDAO getDAO() {
		return dao;
	}
	
	public BanRodConfig getBanRodConfig() {
		return config;
	}
	
	public AccountBanManager getAccountBanManager() {
		return accBanManager;
	}
	
	public IPBanManager getIPBanManager() {
		return ipBanManager;
	}

	@Override
	public void onDisable() {
		
	}

}
