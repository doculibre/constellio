package com.constellio.app.ui.pages.statistic;

import static com.constellio.app.ui.pages.statistic.StatisticsPresenter.FAMOUS_REQUEST;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.RandomUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class StatisticsPresenterAcceptanceTest extends ConstellioTest {
	private SchemasRecordsServices schemasRecordsServices;
	@Mock
	private UIContext uiContext;
	@Mock
	private StatisticsViewImpl statisticsView;

	private StatisticsPresenter presenter;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		SessionContext sessionContext = FakeSessionContext.noUserInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(statisticsView.getSessionContext()).thenReturn(sessionContext);
		when(statisticsView.getCollection()).thenReturn(zeCollection);
		when(statisticsView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(statisticsView.getUIContext()).thenReturn(uiContext);

		presenter = new StatisticsPresenter(statisticsView);
	}

	private List<SearchEvent> addSomeSearchEventForTest(int nb) {
		List<SearchEvent> searchEvents = new ArrayList<>(nb);

		for (int i = 0; i < nb; i++) {
			SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();

			searchEvent.setClickCount(2 * i);
			searchEvent.setPageNavigationCount(3 * i);
			searchEvent.setParams(Arrays.asList("params" + i));
			searchEvent.setQuery("query" + i);

			searchEvents.add(searchEvent);
		}

		saveSearhEvents(searchEvents);

		return searchEvents;
	}

	private void saveSearhEvent(SearchEvent searchEvent) {
		saveSearhEvents(Arrays.asList(searchEvent));
	}

	private void saveSearhEvents(List<SearchEvent> searchEvents) {
		Transaction tx = new Transaction();
		tx.addAll(searchEvents);
		tx.setRecordFlushing(RecordsFlushing.NOW());
		try {
			getModelLayerFactory().newRecordServices().execute(tx);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private List<SearchEvent> fromRecodVOs(List<RecordVO> recordVOS) {
		List<SearchEvent> results = new ArrayList<>();

		for (RecordVO recordVO : recordVOS) {
			results.add(schemasRecordsServices.wrapSearchEvent(recordVO.getRecord()));
		}

		return results;
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb);

		List<RecordVO> recordVOS = presenter.getStatisticsDataProvider().listRecordVOs(0, nb);

		Assertions.assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		Assertions.assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents);
	}

	@Test
	public void givenSavedEventsWhenExcludedThenNotFound() {
		int nb = 10;
		List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb);

		SearchEvent excluded = searchEvents.remove(RandomUtils.nextInt(0, searchEvents.size()));

		presenter.applyFilter(excluded.getQuery(), null, null, null, null, null);
		List<RecordVO> recordVOS = presenter.getStatisticsDataProvider().listRecordVOs(0, nb);

		Assertions.assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		Assertions.assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents).doesNotContain(excluded);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenUnknownStatisticTypeThenException() {
		presenter.applyFilter(null, null, null, null, null, null);

		presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, 15);
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb);

		presenter.applyFilter(null, FAMOUS_REQUEST, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		Assertions.assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}
}
