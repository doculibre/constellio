package com.constellio.model.services.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchServicesElevationAcceptTest extends ConstellioTest {
	protected SearchConfigurationsManager searchConfigurationsManager;
	protected RecordServices recordServices;
	protected SearchServices searchServices;

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
				.withFoldersAndContainersOfEveryStatus());

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = new SearchServices(getDataLayerFactory().newRecordDao(), getModelLayerFactory());

		searchConfigurationsManager = getModelLayerFactory().getSearchConfigurationsManager();
	}

	protected List loadRecords(String query) {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(query);

		return searchServices.search(logicalSearchQuery);
	}

	@Test
	public void givenElevatedIdThenTopOfTheResults() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		Record record = recordList.get(random.nextInt(recordList.size()));
		String idElevated = record.getId();

		searchConfigurationsManager.setElevated(zeCollection, query, idElevated);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(0);
		assertThat(record.getId()).isEqualTo(idElevated);
	}

	@Test
	public void givenElevatedIdWhenRemoveElevatedThenBackToInitialIndex() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		int index = random.nextInt(recordList.size());

		Record record = recordList.get(index);
		String idElevated = record.getId();

		searchConfigurationsManager.setElevated(zeCollection, query, idElevated);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(0);
		assertThat(record.getId()).isEqualTo(idElevated);

		searchConfigurationsManager.removeElevated(zeCollection, query, idElevated);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(index);
		assertThat(record.getId()).isEqualTo(idElevated);
	}

	@Test
	public void givenElevatedIdWhenRemoveQueryElevationThenBackToInitialIndex() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		int index = random.nextInt(recordList.size());

		Record record = recordList.get(index);
		String idElevated = record.getId();

		searchConfigurationsManager.setElevated(zeCollection, query, idElevated);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(0);
		assertThat(record.getId()).isEqualTo(idElevated);

		searchConfigurationsManager.removeQueryElevation(zeCollection, query);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(index);
		assertThat(record.getId()).isEqualTo(idElevated);
	}

	@Test
	public void givenElevatedIdWhenRemoveAllElevationThenBackToInitialIndex() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		int index = random.nextInt(recordList.size());

		Record record = recordList.get(index);
		String idElevated = record.getId();

		searchConfigurationsManager.setElevated(zeCollection, query, idElevated);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(0);
		assertThat(record.getId()).isEqualTo(idElevated);

		searchConfigurationsManager.removeAllElevation(zeCollection);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		record = recordList.get(index);
		assertThat(record.getId()).isEqualTo(idElevated);
	}

	@Test
	public void givenExcludedIdThenNotInResults() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		Record record = recordList.get(random.nextInt(recordList.size()));
		String idExcluded = record.getId();

		searchConfigurationsManager.setExcluded(zeCollection, idExcluded);
		System.out.println("Excluded : " + idExcluded);
		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty().extracting("id").doesNotContain(idExcluded);
	}

	@Test
	public void givenExcludedIdWhenRemoveExclusionThenInResults() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		Record record = recordList.get(random.nextInt(recordList.size()));
		String idExcluded = record.getId();

		searchConfigurationsManager.setExcluded(zeCollection, idExcluded);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty().extracting("id").doesNotContain(idExcluded);

		searchConfigurationsManager.removeExclusion(zeCollection, idExcluded);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty().extracting("id").contains(idExcluded);
	}

	@Test
	public void givenExcludedIdWhenRemoveAllExclusionThenInResults() {
		Random random = new Random();
		String query = "abeil";

		List<Record> recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty();

		Record record = recordList.get(random.nextInt(recordList.size()));
		String idExcluded = record.getId();

		searchConfigurationsManager.setExcluded(zeCollection, idExcluded);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty().extracting("id").doesNotContain(idExcluded);

		searchConfigurationsManager.removeAllExclusion(zeCollection);

		recordList = loadRecords(query);
		assertThat(recordList).isNotEmpty().extracting("id").contains(idExcluded);
	}
}
