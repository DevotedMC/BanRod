package com.github.maxopoly.banrod;

import com.github.maxopoly.zeus.model.yaml.ConfigSection;
import com.github.maxopoly.zeus.plugin.ZeusPluginConfig;

public class BanRodConfig {

	private ZeusPluginConfig pluginConfig;
	private ConfigSection config;
	
	
	private boolean createShareOnDeniedLogin;
	private int maxAccountsPerShare;
	private String tooManyAccountsMsg;
	
	public BanRodConfig(ZeusPluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
		parse();
	}
	
	public void parse() {
		pluginConfig.reloadConfig();
		this.config = pluginConfig.getConfig();
		this.createShareOnDeniedLogin = config.getBoolean("create_share_on_failed_login", false);
		this.maxAccountsPerShare = config.getInt("max_accounts_per_share", 1);
		this.tooManyAccountsMsg = config.getString("too_many_accounts_msg", "You already have too many accounts");
	}
	
	public boolean createShareOnDeniedLogin() {
		return createShareOnDeniedLogin;
	}
	
	public String getTooManyAccountsMsg() {
		return tooManyAccountsMsg;
	}
	
	public int getMaxAccountsPerShare() {
		return maxAccountsPerShare;
	}

}
