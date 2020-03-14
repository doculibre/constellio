package com.constellio.model.services.records.cache;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.records.cache.PersistedIdsServices.RecordIdsIterator;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistedIdsServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void whenSavingThenLoadingSortValuesThenIdentical() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));

		PersistedIdsServices services = new PersistedIdsServices(getModelLayerFactory());

		RecordIdsIterator idsFromSolr = services.getRecordIds();
		assertThat(idsFromSolr.obtainedFromSolr).isTrue();
		RecordIdsIterator idsFromFile = services.getRecordIds();
		assertThat(idsFromFile.obtainedFromSolr).isFalse();
		assertThat(IteratorUtils.toList(idsFromFile.iterator)).isEqualTo(IteratorUtils.toList(idsFromSolr.iterator));

		assertThat(idsFromFile.timestamp).isEqualTo(idsFromSolr.timestamp).isNotNull();

	}
}
