package com.github.maxopoly.banrod.model;

import java.time.Instant;

import inet.ipaddr.ipv4.IPv4Address;

public class BRIPv4Ban extends BRIPBan<IPv4Address> {

	public BRIPv4Ban(int bid, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect, IPv4Address ip) {
		super(bid, startingTime, endTime, comment, source, ip, effect);
	}

}
