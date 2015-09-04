/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
