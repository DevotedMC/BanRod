package com.github.maxopoly.banrod.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class BRPlayerCluster {

	private Set<BRShare> shares;

	public BRPlayerCluster(Collection<BRShare> shares) {
		this.shares = new HashSet<>(shares);
	}

	/**
	 * Calculates how many accounts in the cluster are older than the given player,
	 * meaning they joined on any shared ip before the player
	 * 
	 * @param player Player to check for
	 * @return 0 if the player is the oldest account, otherwise the amount of
	 *         accounts older than the given player
	 */
	public int getRelativePosition(UUID player) {
		if (shares.isEmpty()) {
			return 0;
		}
		Set<UUID> older = new HashSet<>();
		for (BRShare share : shares) {
			BRSession first = share.getFirstSession();
			BRSession second = share.getSecondSession();
			if (first.getPlayer().equals(player)) {
				if (second.getStartingTime().isBefore(first.getStartingTime())) {
					older.add(second.getPlayer());
				}
				continue;
			}
			if (second.getPlayer().equals(player)) {
				if (first.getStartingTime().isBefore(second.getStartingTime())) {
					older.add(first.getPlayer());
				}
			}
		}
		return older.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Share cluster containing ");
		sb.append(shares.size());
		sb.append(" total shares:\n");
		for (BRShare share : shares) {
			BRSession first = share.getFirstSession();
			BRSession second = share.getSecondSession();
			sb.append("Share ");
			sb.append(share.getID());
			if (share.isForgiven()) {
				sb.append(" (Forgiven)");
			}
			sb.append("  ");
			sb.append(first.toString());
			sb.append(" <----> ");
			sb.append(second.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

}
