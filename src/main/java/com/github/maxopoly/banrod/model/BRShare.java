package com.github.maxopoly.banrod.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

public class BRShare {
	
	private int shareID;
	private BRSession firstSession;
	private BRSession secondSession;
	private boolean forgiven;
	
	public BRShare(int shareID, BRSession firstSession, BRSession secondSession, boolean forgiven) {
		Preconditions.checkArgument(shareID >= 0);
		Preconditions.checkNotNull(firstSession);
		Preconditions.checkNotNull(secondSession);
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
	
	public boolean equals(Object o) {
		if (!(o instanceof BRShare)) {
			return false;
		}
		return ((BRShare) o).shareID == this.shareID;
	}
	
	public int hashCode() {
		return Objects.hash(shareID);
	}

}
