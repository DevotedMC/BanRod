package com.github.maxopoly.banrod.model;

import java.time.Instant;

import inet.ipaddr.IPAddress;

public class BRIPBan <I extends IPAddress> extends BRBan {
	
	private I ip;

	public BRIPBan(int bid, Instant startingTime, Instant endTime, String comment, String source, I ip, BanEffect effect) {
		super(bid, startingTime, endTime, comment, source, effect);
		this.ip = ip;
	}
	
	public I getIP() {
		return ip;
	}

	@Override
	public String getBanText() {
		return "IP Ban  -  " + getFormattedTimeSpan();
	}

}

