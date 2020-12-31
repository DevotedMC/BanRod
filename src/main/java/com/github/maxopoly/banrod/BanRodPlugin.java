package com.github.maxopoly.banrod;

import com.github.maxopoly.banrod.listener.IPBanManager;
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

	@Override
	public boolean onEnable() {
		instance = this;
		this.dao = new BanRodDAO(logger);
		this.ipBanManager = new IPBanManager();
		return true;
	}
	
	public BanRodDAO getDAO() {
		return dao;
	}
	
	public IPBanManager getIPBanManager() {
		return ipBanManager;
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
