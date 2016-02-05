package com.constellio.sdk.tests;

import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public class SystemLoadSimulator {

	public enum SystemLoadLevel {
		OFF(0), LOW(1), HEAVY(3), STRESS(10);

		final int multiplicator;

		private SystemLoadLevel(int multiplicator) {
			this.multiplicator = multiplicator;
		}

		public int getMultiplicator() {
			return multiplicator;
		}

		public SystemLoadLevel toggle() {
			if (ordinal() + 1 == values().length) {
				return values()[0];
			} else {
				return values()[ordinal() + 1];
			}
		}
	}

	public static void simulateQuery(SolrParams query, long qtime, SystemLoadLevel level) {
		sleep(qtime * level.getMultiplicator());
	}

	public static void simulateUpdate(BigVaultServerTransaction transaction, long qtime, SystemLoadLevel level) {
		sleep(qtime * level.getMultiplicator());
	}

	private static void sleep(long delay) {
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
