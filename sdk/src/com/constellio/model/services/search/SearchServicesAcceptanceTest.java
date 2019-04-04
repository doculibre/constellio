package com.constellio.model.services.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
				.withDocumentsDecommissioningList());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void testQueryWithFacetPivots() {
		List<String> pivots = asList(rm.document.folder().getDataStoreCode());
		LogicalSearchQuery query = new LogicalSearchQuery()
				.setCondition(from(rm.documentSchemaType()).returnAll())
				.setNumberOfRows(0)
				.setFieldPivotFacets(pivots);

		SPEQueryResponse response = getModelLayerFactory().newSearchServices().query(query);

		assertThat(response.getFieldFacetPivotValues(pivots)).isNotEmpty();
	}

	@Test
	public void testQueryWithFacet() {
		SPEQueryResponse response = getModelLayerFactory().newSearchServices()
				.query(new LogicalSearchQuery()
						.setCondition(from(rm.folderSchemaType()).returnAll())
						.setNumberOfRows(0).addFieldFacet(rm.folder.parentFolder().getDataStoreCode()));

		assertThat(response.getFieldFacetValues()).isNotEmpty();
	}

}
