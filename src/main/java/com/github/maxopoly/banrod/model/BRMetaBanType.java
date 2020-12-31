package com.github.maxopoly.banrod.model;

public enum BRMetaBanType {

	COUNTRY("country"), REGION("region"), CITY("city"), POSTAL("postal"), DOMAIN("domain"),
	REGISTERED_AS("registered_as"), CONNECTION("connection");

	private String dbIdentifier;

	private BRMetaBanType(String dbIdentifier) {
		this.dbIdentifier = dbIdentifier;
	}

	public String getDBIdentifier() {
		return dbIdentifier;
	}

}
