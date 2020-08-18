package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class FolderCacheAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RMSchemasRecordsServices rm;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void test1()
			throws Exception {
		test();
	}

	@Test
	public void test2()
			throws Exception {
		test();
	}


	public void test()
			throws Exception {

		SearchServices searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();

		//Marche pas
		List<String> documentIds = searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.folder()).isIn(Arrays.asList(records.getFolder_A01().getId()))));

		// Marche
		//		List<String> documentIds = searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.document.schemaType())
		//				.where(rm.document.folder()).isEqualTo(records.getFolder_A01().getId())));

		assertThat(documentIds).hasSize(4);
	}

}
