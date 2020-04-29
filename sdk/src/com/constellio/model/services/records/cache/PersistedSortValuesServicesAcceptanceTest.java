package com.constellio.model.services.records.cache;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.records.cache.PersistedSortValuesServices.SortValueList;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistedSortValuesServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void whenSavingThenLoadingSortValuesThenIdentical() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));

		PersistedSortValuesServices services = new PersistedSortValuesServices(getModelLayerFactory());

		givenTimeIs(LocalDate.now().plusDays(4));

		SortValueList sortValueListFromSolr = services.readSortValues();
		assertThat(sortValueListFromSolr.obtainedFromSolr).isTrue();
		SortValueList sortValueListFromFile = services.readSortValues();
		assertThat(sortValueListFromFile.obtainedFromSolr).isFalse();
		assertThat(sortValueListFromFile.sortValues).isEqualTo(sortValueListFromSolr.sortValues);
		assertThat(sortValueListFromFile.timestamp).isEqualTo(sortValueListFromSolr.timestamp).isNotNull();

	}
}
