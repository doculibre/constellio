package com.constellio.model.services.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchConfigurationsManagerAcceptanceTest extends ConstellioTest {

	public SynonymsConfigurationsManager synonymsConfigurationsManager;
	public SearchConfigurationsManager searchConfigurationsManager;
	public static final String SYNONYM_1 = "car";
	public static final String TWO_SYNONYMS = SYNONYM_1 + ", ppppppppppppppppp";
	public static final String THREE_SYNONYMS = "auto, cccccccccccccc, dada";

	Users users = new Users();
	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	public static final String ELEVATED_KEY_1 = "abeil";

	@Before
	public void setUp() {
		assumeLocalSolr();
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users)
										.withFoldersAndContainersOfEveryStatus());
		//syncSolrConfigurationFiles(getDataLayerFactory());
		synonymsConfigurationsManager = getModelLayerFactory().getSynonymsConfigurationsManager();
		searchConfigurationsManager = getModelLayerFactory().getSearchConfigurationsManager();
		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();

		Elevations elevations = searchConfigurationsManager.getCollection(zeCollection);

		for (Iterator<Elevations.QueryElevation> iterator = elevations.getQueryElevations().iterator(); iterator.hasNext(); ) {
			Elevations.QueryElevation queryElevation = iterator.next();

			for (Iterator<Elevations.QueryElevation.DocElevation> queryElevationIterator =
				 queryElevation.getDocElevations().iterator(); queryElevationIterator.hasNext(); ) {
				Elevations.QueryElevation.DocElevation docElevation = queryElevationIterator.next();
				searchConfigurationsManager.removeElevated(zeCollection, queryElevation.getQuery(), docElevation.getId());
			}
		}

	}

	@Test
	public void setElevationAndExlusionAndThenRemoveQuery() {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(ELEVATED_KEY_1);

		List<Record> recordList = searchServices.search(logicalSearchQuery);

		Record record0 = recordList.get(3);
		Record excludedRecord1 = recordList.get(2);
		Record excludedRecord2 = recordList.get(0);

		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record0);
		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, excludedRecord1);
		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, excludedRecord2);
	}

	@Test
	public void setElevatedAndExclutionThenRemoveAllExclusion() {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(ELEVATED_KEY_1);

		List<Record> recordList = searchServices.search(logicalSearchQuery);

		Record record0 = recordList.get(3);
		Record excludedRecord1 = recordList.get(2);
		Record excludedRecord2 = recordList.get(0);

		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record0);
		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, excludedRecord1);
		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, excludedRecord2);

		searchConfigurationsManager.removeQueryElevation(zeCollection, ELEVATED_KEY_1);

		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, excludedRecord1)).isFalse();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, excludedRecord2)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludedRecord1)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludedRecord2)).isFalse();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record0)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record0)).isFalse();
	}

	@Test
	public void setElevatedAndExclusionThenRemoveAllElevation() {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(ELEVATED_KEY_1);

		List<Record> recordList = searchServices.search(logicalSearchQuery);

		Record record0 = recordList.get(3);
		Record record1 = recordList.get(2);
		Record excludeRecord = recordList.get(0);

		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record0);
		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record1);
		searchConfigurationsManager.setExcluded(zeCollection, excludeRecord);

		searchConfigurationsManager.removeQueryElevation(zeCollection, ELEVATED_KEY_1);

		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record0)).isFalse();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record1)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record0)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record1)).isFalse();

		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludeRecord)).isTrue();
	}

	@Test
	public void elevationsSetElevatedSetExclutionGetDefinedSearchWithParametersThenVerifyOrder() {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(ELEVATED_KEY_1);

		List<Record> recordList = searchServices.search(logicalSearchQuery);

		Record record0 = recordList.get(3);
		Record excludeRecord = recordList.get(0);
		String excludedRecordId = excludeRecord.getId();
		String elevatedId = record0.getId();

		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record0);
		searchConfigurationsManager.setExcluded(zeCollection, excludeRecord);

		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record0)).isTrue();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record0)).isFalse();

		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludeRecord)).isTrue();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, excludeRecord)).isFalse();

		List<Record> recordsListAfterElevation = searchServices.search(logicalSearchQuery);
		assertThat(recordsListAfterElevation.get(0).getId()).isEqualTo(elevatedId);

		for (Record currentRecord : recordsListAfterElevation) {
			assertThat(currentRecord.getId()).isNotEqualTo(excludedRecordId);
		}
	}

	@Test
	public void setElevationAndExclusionRemoveElevationThanVerifyRemoval() {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(ELEVATED_KEY_1);

		List<Record> recordList = searchServices.search(logicalSearchQuery);

		Record record0 = recordList.get(3);
		Record excludeRecord = recordList.get(0);

		searchConfigurationsManager.setElevated(zeCollection, ELEVATED_KEY_1, record0);
		searchConfigurationsManager.setExcluded(zeCollection, excludeRecord);

		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record0)).isTrue();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record0)).isFalse();

		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludeRecord)).isTrue();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, excludeRecord)).isFalse();

		searchConfigurationsManager.removeElevated(zeCollection, ELEVATED_KEY_1, record0.getId());
		searchConfigurationsManager.removeExclusion(zeCollection, excludeRecord.getId());

		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, record0)).isFalse();
		assertThat(searchConfigurationsManager.isExcluded(zeCollection, record0)).isFalse();

		assertThat(searchConfigurationsManager.isExcluded(zeCollection, excludeRecord)).isFalse();
		assertThat(searchConfigurationsManager.isElevated(zeCollection, ELEVATED_KEY_1, excludeRecord)).isFalse();
	}

	@Test
	public void getSetGetSynonymsOnServerThenOk() {
		synonymsConfigurationsManager.setSynonyms(zeCollection, new ArrayList<String>());
		assertThat(synonymsConfigurationsManager.getSynonyms(zeCollection).size()).isEqualTo(0);
		synonymsConfigurationsManager.setSynonyms(zeCollection, Arrays.asList(TWO_SYNONYMS, THREE_SYNONYMS));
		assertThat(synonymsConfigurationsManager.getSynonyms(zeCollection).get(0)).isEqualTo(TWO_SYNONYMS);
		assertThat(synonymsConfigurationsManager.getSynonyms(zeCollection).get(1)).isEqualTo(THREE_SYNONYMS);
	}
}
