package com.constellio.model.services.logging;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.background.ModelLayerBackgroundThreadsManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class SearchEventServicesAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	SearchEventServices searchEventServices;
	SchemasRecordsServices schemas;
	SearchServices searchServices;

	private static int TWO_SECONDS = 2000;

	@Before
	public void setUp()
			throws Exception {

		ModelLayerBackgroundThreadsManager.FLUSH_EVENTS_EVERY_DURATION = Duration.standardSeconds(2);
		givenBackgroundThreadsEnabled();
		prepareSystem(withZeCollection().withAllTest(users));
		searchEventServices = new SearchEventServices(zeCollection, getModelLayerFactory());
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test(expected = java.lang.NumberFormatException.class)
	public void whenCreatingASearchEventThenIdIsUUID()
			throws Exception {
		String id = schemas.newSearchEvent().getId();
		Double.parseDouble(id);
	}

	@Test
	public void whenSavingPlethoraOfSearchEventsThenReallyFast()
			throws Exception {

		long timeStampBefore = new Date().getTime();

		for (int i = 0; i < 5000; i++) {
			searchEventServices.save(schemas.newSearchEvent().setUsername("bob").setQuery("banane"));
		}

		long timeStampAfter = new Date().getTime();

		LogicalSearchQuery countSearchEventsQuery = new LogicalSearchQuery(from(schemas.searchEvent.schemaType()).returnAll());

		for (int i = 0; i < 2500 && searchServices.getResultsCount(countSearchEventsQuery) < 5000; i += 10) {
			Thread.sleep(10);
		}

		assertThat(timeStampAfter - timeStampBefore).isLessThan(TWO_SECONDS);
		getDataLayerFactory().newEventsDao().flush();
		assertThat(searchServices.getResultsCount(countSearchEventsQuery)).isEqualTo(5000);

	}

	@Test
	public void whenSavingPlethoraOfClicksThenReallyFast()
			throws Exception {

		long timeStampBefore = new Date().getTime();

		searchEventServices.save(schemas.newSearchEventWithId("search1").setUsername("bob").setQuery("banane").setNumFound(3000)
				.setQTime(100));
		searchEventServices.save(schemas.newSearchEventWithId("search2").setUsername("bob").setQuery("banane"));
		searchEventServices.save(schemas.newSearchEventWithId("search3").setUsername("bob").setQuery("banane"));

		for (int i = 0; i < 5000; i++) {
			searchEventServices.incrementClickCounter("search1");
			searchEventServices.setLastPageNavigation("search1", 5000 / 2);
		}
		for (int i = 0; i < 500; i++) {
			searchEventServices.incrementClickCounter("search2");
			searchEventServices.incrementPageNavigationCounter("search2");
			searchEventServices.setLastPageNavigation("search2", 505 - i);
		}
		for (int i = 0; i < 2500; i++) {
			searchEventServices.incrementClickCounter("search3");
			searchEventServices.incrementPageNavigationCounter("search3");
			searchEventServices.setLastPageNavigation("search3", 42);
		}

		long timeStampAfter = new Date().getTime();

		getDataLayerFactory().newEventsDao().flush();

		assertThat(timeStampAfter - timeStampBefore).isLessThan(TWO_SECONDS);
		SearchEvent event1 = schemas.getSearchEvent("search1");
		SearchEvent event2 = schemas.getSearchEvent("search2");
		SearchEvent event3 = schemas.getSearchEvent("search3");

		assertThat(event1.getClickCount()).isEqualTo(5000);
		assertThat(event1.getPageNavigationCount()).isEqualTo(0);
		assertThat(event1.getNumFound()).isEqualTo(3000);
		assertThat(event1.getQTime()).isEqualTo(100);
		assertThat(event1.getLastPageNavigation()).isEqualTo(2500);
		assertThat(event2.getClickCount()).isEqualTo(500);
		assertThat(event2.getPageNavigationCount()).isEqualTo(500);
		assertThat(event2.getLastPageNavigation()).isEqualTo(6);
		assertThat(event3.getClickCount()).isEqualTo(2500);
		assertThat(event3.getPageNavigationCount()).isEqualTo(2500);
		assertThat(event3.getLastPageNavigation()).isEqualTo(42);

	}

	@Test
	public void whenGetMostPopularQueriesAutocompleteThenReturnPreviouslyConcluentRequestsWithoutExclusionsInOrder()
			throws RecordServicesException {

		// concluding searches

		Map<String, Integer> searchValuesWithOccurences = new HashMap<>();
		searchValuesWithOccurences.put("avengers", new Integer(10));
		searchValuesWithOccurences.put("avion", new Integer(5));
		searchValuesWithOccurences.put("aviation", new Integer(4));

		searchValuesWithOccurences.put("aviron", new Integer(3));
		searchValuesWithOccurences.put("avionique", new Integer(2));

		searchValuesWithOccurences.put("avaleur", new Integer(1));

		// not concluding searches
		Set<String> searchTermsWithoutResultsThusExcluded = new HashSet<>();
		searchTermsWithoutResultsThusExcluded.add("avaleur");

		// prepares transaction
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(
				OptimisticLockingResolution.EXCEPTION); // changes transatction limit from 1 000 to 100 000

		// makes transaction
		for (Map.Entry searchValueWithOccurence : searchValuesWithOccurences.entrySet()) {

			String searchValue = (String) searchValueWithOccurence.getKey();
			Integer searchOccurence = (Integer) searchValueWithOccurence.getValue();

			// determines if should be concluent search
			int numFound = 1;
			if (searchTermsWithoutResultsThusExcluded.contains(searchValue)) {
				numFound = 0;
			}

			// searches for number of times indicated
			for (int i = 0; i < searchOccurence; i++) {
				SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();
				searchEvent.setQuery(StringUtils.stripAccents(searchValue.toLowerCase())).setNumFound(numFound);
				transaction.add(searchEvent);
			}
		}
		recordServices.execute(transaction);

		String[] emptyCustomRequestExclusion = {};
		assertThat(searchEventServices.getMostPopularQueriesAutocomplete("av", 5, emptyCustomRequestExclusion))
				.containsExactly("avengers", "avion", "aviation", "aviron", "avionique");
		assertThat(searchEventServices.getMostPopularQueriesAutocomplete("Av", 5, emptyCustomRequestExclusion))
				.containsExactly("avengers", "avion", "aviation", "aviron", "avionique");

		String[] singleCustomRequestExclusion = { "aviron" };
		assertThat(searchEventServices.getMostPopularQueriesAutocomplete("av", 5, singleCustomRequestExclusion))
				.containsExactly("avengers", "avion", "aviation", "avionique");

		String[] multipleCustomRequestExclusions = { "aviron", "avionique" };
		assertThat(searchEventServices.getMostPopularQueriesAutocomplete("av", 5, multipleCustomRequestExclusions))
				.containsExactly("avengers", "avion", "aviation");

	}
}
