package com.github.maxopoly.banrod.model;

public class BRShare {
	
	private int shareID;
	private BRSession firstSession;
	private BRSession secondSession;
	private boolean forgiven;
	
	public BRShare(int shareID, BRSession firstSession, BRSession secondSession, boolean forgiven) {
		super();
		this.shareID = shareID;
		this.firstSession = firstSession;
		this.secondSession = secondSession;
		this.forgiven = forgiven;
	}

	public int getID() {
		return shareID;
	}

	public BRSession getFirstSession() {
		return firstSession;
	}

	public BRSession getSecondSession() {
		return secondSession;
	}

	public boolean isForgiven() {
		return forgiven;
	}

}
