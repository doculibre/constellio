package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.utils.SortOrder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordHierarchyServicesAcceptTest extends ConstellioTest {

	RMTestRecords allTestRecords = new RMTestRecords(zeCollection);
	Users users = new Users();

	RecordHierarchyServices recordHierarchyServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(allTestRecords)
				.withAllTest(users).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		recordHierarchyServices = new RecordHierarchyServices(getModelLayerFactory());
	}

	@Test
	public void givenRecordInHierarchyWhenGetAllRecordsInHierarchySortOrderAscendingThenRecordsSortedAscending() {
		List<Record> orderedRecords =
				recordHierarchyServices.getAllRecordsInHierarchy(allTestRecords.getFolder_A02().getWrappedRecord(), SortOrder.ASCENDING);
		assertThat(orderedRecords.get(0)).isEqualTo(allTestRecords.getFolder_A02().getWrappedRecord());
	}

	@Test
	public void givenRecordInHierarchyWhenGetAllRecordsInHierarchySortOrderDescendingThenRecordsSortedDescending() {
		List<Record> orderedRecords =
				recordHierarchyServices.getAllRecordsInHierarchy(allTestRecords.getFolder_A02().getWrappedRecord(), SortOrder.DESCENDING);
		assertThat(orderedRecords.get(orderedRecords.size() - 1)).isEqualTo(allTestRecords.getFolder_A02().getWrappedRecord());
	}

}
