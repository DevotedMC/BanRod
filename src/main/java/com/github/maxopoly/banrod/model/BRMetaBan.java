package com.github.maxopoly.banrod.model;

import java.time.Instant;

public class BRMetaBan extends BRBan {

	private BRMetaBanType banType;
	private String condition;

	public BRMetaBan(int bid, Instant startingTime, Instant endTime, String comment, String source, BRMetaBanType banType,
			String condition, BanEffect effect) {
		super(bid, startingTime, endTime, comment, source, effect);
		this.banType = banType;
		this.condition = condition;
	}

	public BRMetaBanType getBanType() {
		return banType;
	}

	public String getCondition() {
		return condition;
	}
	
	@Override
	public String getBanText() {
		switch (banType) {
		case CITY:
			return String.format("City wide ban for %s  %s", condition, getFormattedTimeSpan());
		case CONNECTION:
			return String.format("Connection based ban for %s  %s", condition, getFormattedTimeSpan());
		case COUNTRY:
			return String.format("Country wide ban for %s  %s", condition, getFormattedTimeSpan());
		case DOMAIN:
			return String.format("Domain wide ban for %s  %s", condition, getFormattedTimeSpan());
		case POSTAL:
			return String.format("Postal code based ban for %s  %s", condition, getFormattedTimeSpan());
		case REGION:
			return String.format("Region wide ban for %s  %s", condition, getFormattedTimeSpan());
		case REGISTERED_AS:
			return String.format("Registrar based ban on %s  %s", condition, getFormattedTimeSpan());
		default:
			throw new IllegalStateException();
		}
	}

}
