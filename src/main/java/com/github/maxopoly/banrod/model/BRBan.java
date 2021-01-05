package com.github.maxopoly.banrod.model;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public abstract class BRBan {

	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

	private int bid;
	private Instant startingTime;
	private Instant endTime;
	private String comment;
	private String source;
	private BanEffect effect;

	protected BRBan(int bid, Instant startingTime, Instant endTime, String comment, String source, BanEffect effect) {
		this.bid = bid;
		this.startingTime = startingTime;
		this.endTime = endTime;
		this.comment = comment;
		this.source = source;
		this.effect = effect;
	}

	protected String getFormattedTimeSpan() {
		String startingTimeFormatted = timeFormatter.format(startingTime);
		String endTimeFormatted;
		if (endTime == null) {
			endTimeFormatted = "never";
		} else {
			endTimeFormatted = timeFormatter.format(endTime);
		}
		return String.format("Issued: %s  -  Ends: %s", startingTimeFormatted, endTimeFormatted);
	}

	public abstract String getBanText();

	public boolean isActive() {
		Instant now = Instant.now();
		return (endTime == null || endTime.isAfter(now)) && startingTime.isBefore(now);
	}

	public int getBid() {
		return bid;
	}

	public Instant getStartingTime() {
		return startingTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public String getComment() {
		return comment;
	}

	public String getSource() {
		return source;
	}
	
	public BanEffect getEffect() {
		return effect;
	}
	
	public String toString() {
		return String.format("Start: %s, End: %s, Effect: %s, Comment: %s, Source: %s", startingTime, endTime, effect, comment, source);
	}

}
