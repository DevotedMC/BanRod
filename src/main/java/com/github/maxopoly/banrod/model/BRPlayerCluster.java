package com.github.maxopoly.banrod.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BRPlayerCluster {

	private Map<UUID, List<BRShare>> playersToShares;

	public BRPlayerCluster(Collection<BRShare> shares) {
		this.playersToShares = new HashMap<>();
		for (BRShare share : shares) {
			List<BRShare> exis = playersToShares.computeIfAbsent(share.getFirstSession().getPlayer(),
					u -> new ArrayList<>());
			exis.add(share);
			List<BRShare> exis2 = playersToShares.computeIfAbsent(share.getSecondSession().getPlayer(),
					u -> new ArrayList<>());
			exis2.add(share);
		}
	}

	public Map<UUID, List<BRShare>> getPlayerToShareMapping() {
		return Collections.unmodifiableMap(playersToShares);
	}

}
