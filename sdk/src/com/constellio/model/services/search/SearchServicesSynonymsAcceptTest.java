package com.constellio.model.services.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchServicesSynonymsAcceptTest extends ConstellioTest {
	protected SynonymsConfigurationsManager synonymsConfigurationsManager;
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

		synonymsConfigurationsManager = getModelLayerFactory().getSynonymsConfigurationsManager();
	}

	protected List<Record> loadRecords(String query) {
		LogicalSearchCondition logicalSearchCondition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(logicalSearchCondition);
		logicalSearchQuery.setFreeTextQuery(query);

		return searchServices.search(logicalSearchQuery);
	}

	@Test
	public void givenSynonymsThenResultsTitlesContainAnyOfThem() {
		Random random = new Random();
		final String[] synonyms = {"chat", "lion", "abeille"};
		String query = synonyms[random.nextInt(synonyms.length)];

		synonymsConfigurationsManager.setSynonyms(zeCollection, Arrays.asList(StringUtils.join(synonyms, ", ")));

		List<Record> records = loadRecords(query);
		assertThat(records).isNotEmpty();

		assertThat(records).extracting("title").has(new Condition<List<Object>>() {
			@Override
			public boolean matches(List<Object> values) {
				for (Object title : values) {
					String synonym = null;

					for (int i = 0; i < synonyms.length && synonym == null; i++) {
						if (StringUtils.containsIgnoreCase((String) title, synonyms[i])) {
							synonym = synonyms[i];
						}
					}

					if (synonym == null) {
						return false;
					}
				}

				return true;
			}
		});
	}

	@Test
	public void givenSynonymsWhenResetThenResultsTitlesContainOnlyTheQuery() {
		Random random = new Random();
		final String[] synonyms = {"chat", "lion", "abeille"};
		final String query = synonyms[random.nextInt(synonyms.length)];

		synonymsConfigurationsManager.setSynonyms(zeCollection, Arrays.asList(StringUtils.join(synonyms, ", ")));

		List<Record> records = loadRecords(query);
		assertThat(records).isNotEmpty();

		assertThat(records).extracting("title").has(new Condition<List<Object>>() {
			@Override
			public boolean matches(List<Object> values) {
				for (Object title : values) {
					String synonym = null;

					for (int i = 0; i < synonyms.length && synonym == null; i++) {
						if (StringUtils.containsIgnoreCase((String) title, synonyms[i])) {
							synonym = synonyms[i];
						}
					}

					if (synonym == null) {
						return false;
					}
				}

				return true;
			}
		});

		synonymsConfigurationsManager.setSynonyms(zeCollection, Collections.<String>emptyList());

		records = loadRecords(query);
		assertThat(records).isNotEmpty();

		assertThat(records).extracting("title").has(new Condition<List<Object>>() {
			@Override
			public boolean matches(List<Object> values) {
				for (Object title : values) {
					if (!StringUtils.containsIgnoreCase((String) title, query)) {
						return false;
					}
				}

				return true;
			}
		});
	}
}
