package com.github.maxopoly.banrod.model;

import java.net.InetAddress;

public class BRIP {

	private InetAddress ip;
	private long creationTime;
	private String continent;
	private String country;
	private String region;
	private String city;
	private String postal;
	private Double lat;
	private Double lon;
	private String domain;
	private String provider;
	private String registeredAs;
	private String connection;
	private float proxy;
	private String source;
	private String comment;

	public BRIP(InetAddress ip, long creationTime, String continent, String country, String region, String city,
			String postal, Double lat, Double lon, String domain, String provider, String registeredAs,
			String connection, float proxy, String source, String comment) {
		this.ip = ip;
		this.creationTime = creationTime;
		this.continent = continent;
		this.country = country;
		this.region = region;
		this.city = city;
		this.postal = postal;
		this.lat = lat;
		this.lon = lon;
		this.domain = domain;
		this.provider = provider;
		this.registeredAs = registeredAs;
		this.connection = connection;
		this.proxy = proxy;
		this.source = source;
		this.comment = comment;
	}

	public InetAddress getIp() {
		return ip;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getContinent() {
		return continent;
	}

	public String getCountry() {
		return country;
	}

	public String getRegion() {
		return region;
	}

	public String getCity() {
		return city;
	}

	public String getPostal() {
		return postal;
	}

	public Double getLat() {
		return lat;
	}

	public Double getLon() {
		return lon;
	}

	public String getDomain() {
		return domain;
	}

	public String getProvider() {
		return provider;
	}

	public String getRegisteredAs() {
		return registeredAs;
	}

	public String getConnection() {
		return connection;
	}

	public float getProxy() {
		return proxy;
	}

	public String getSource() {
		return source;
	}

	public String getComment() {
		return comment;
	}

}
