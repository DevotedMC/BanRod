package com.github.maxopoly.banrod;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.github.maxopoly.zeus.model.yaml.ConfigSection;
import com.github.maxopoly.zeus.plugin.ZeusPluginConfig;

public class BanRodConfig {

	private ZeusPluginConfig pluginConfig;
	private ConfigSection config;
	
	private boolean createShareOnDeniedLogin;
	
	public BanRodConfig(ZeusPluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}
	
	public void parse() {
		pluginConfig.reloadConfig();
		this.config = pluginConfig.getConfig();
		this.createShareOnDeniedLogin = config.getBoolean("create_share_on_failed_login", false);
	}
	
	public boolean createShareOnDeniedLogin() {
		return createShareOnDeniedLogin;
	}

}
