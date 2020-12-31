package com.github.maxopoly.banrod.model;

import java.time.Instant;

import inet.ipaddr.ipv4.IPv4Address;
import inet.ipaddr.ipv6.IPv6Address;

public class BRIPv6Ban extends BRIPBan<IPv6Address> {

	public BRIPv6Ban(int bid, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect, IPv6Address ip) {
		super(bid, startingTime, endTime, comment, source, ip, effect);
	}

}
