package com.dabi.habitv.provider.tf1plus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Registry of stable TF1+ catalogue hubs. Partner hubs may be added here without
 * hardcoding individual programmes.
 */
final class Tf1PlusHubRegistry {

	private static final List<Tf1PlusHubDescriptor> HUBS = Collections.unmodifiableList(Arrays.asList(
			Tf1PlusHubDescriptor.groupedTf1(),
			Tf1PlusHubDescriptor.flat("tmc", "TMC", "tmc", Tf1PlusConf.TMC_REPLAY_URL),
			Tf1PlusHubDescriptor.flat("tfx", "TFX", "tfx", Tf1PlusConf.TFX_REPLAY_URL),
			Tf1PlusHubDescriptor.flat("tf1-series-films", "TF1 Séries Films", "tf1-series-films",
					Tf1PlusConf.TF1_SERIES_FILMS_REPLAY_URL),
			Tf1PlusHubDescriptor.flat("lci", "LCI", "lci", Tf1PlusConf.LCI_REPLAY_URL),
			Tf1PlusHubDescriptor.flat("arte", "ARTE", "arte", Tf1PlusConf.ARTE_REPLAY_URL),
			Tf1PlusHubDescriptor.flatWithGraphqlSlug("public-senat", "LCP - Public Sénat", "public-senat",
					"public-senat", Tf1PlusConf.PUBLIC_SENAT_REPLAY_URL)));

	private Tf1PlusHubRegistry() {
	}

	static List<Tf1PlusHubDescriptor> enabledHubs() {
		return HUBS;
	}

	static Tf1PlusHubDescriptor findByHubId(final String hubId) {
		if (hubId == null) {
			return null;
		}
		for (final Tf1PlusHubDescriptor hub : HUBS) {
			if (hubId.equals(hub.getHubId())) {
				return hub;
			}
		}
		return null;
	}

}
