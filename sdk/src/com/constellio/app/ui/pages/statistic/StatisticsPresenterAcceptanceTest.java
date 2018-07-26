package com.constellio.app.ui.pages.statistic;

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
import org.apache.commons.lang3.RandomUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static com.constellio.app.ui.pages.statistic.StatisticsPresenter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

	protected List<SearchEvent> addSomeSearchEventForTest(int nb, boolean withClick, boolean withResult) {
		List<SearchEvent> searchEvents = new ArrayList<>(nb);

		for (int i = 0; i < nb; i++) {
			SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();

			if (withClick) {
				searchEvent.setClickCount(i + 1);
			} else {
				searchEvent.setClickCount(0);
			}
			searchEvent.setPageNavigationCount(3 * i);
			searchEvent.setParams(Arrays.asList("params" + i));
			searchEvent.setQuery("query" + i);
			if (withResult) {
				searchEvent.setNumFound(i + 1);
			} else {
				searchEvent.setNumFound(0);
			}

			searchEvents.add(searchEvent);
		}

		saveSearhEvents(searchEvents);

		return searchEvents;
	}

	protected void saveSearhEvent(SearchEvent searchEvent) {
		saveSearhEvents(Arrays.asList(searchEvent));
	}

	protected void saveSearhEvents(List<SearchEvent> searchEvents) {
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
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		List<RecordVO> recordVOS = presenter.getStatisticsDataProvider().listRecordVOs(0, nb);

		assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents);
	}

	@Test
	public void givenSavedEventsWhenExcludedThenNotFound() {
		int nb = 10;
		List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		SearchEvent excluded = searchEvents.remove(RandomUtils.nextInt(0, searchEvents.size()));

		presenter.applyFilter(excluded.getQuery(), null, null, null, null, null);
		List<RecordVO> recordVOS = presenter.getStatisticsDataProvider().listRecordVOs(0, nb);

		assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents).doesNotContain(excluded);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenUnknownStatisticTypeThenException() {
		presenter.applyFilter(null, null, null, null, null, null);

		presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, 15);
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousRequestFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		presenter.applyFilter(null, FAMOUS_REQUEST, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousRequestWithResultFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, false, true);

		presenter.applyFilter(null, FAMOUS_REQUEST_WITH_RESULT, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousWithoutResultRequestFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, false);

		presenter.applyFilter(null, FAMOUS_REQUEST_WITHOUT_RESULT, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousRequestWithClickFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, false);

		presenter.applyFilter(null, FAMOUS_REQUEST_WITH_CLICK, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousRequestWithoutClickFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, false, false);

		presenter.applyFilter(null, FAMOUS_REQUEST_WITHOUT_CLICK, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size());
	}

	@Test
	public void givenFixedNumberOfEventAddedWhenRequestIsExcludedThenNotFound() {
		Random random = new Random();
		int nb = 10;
		String excludedRequest = "query" + random.nextInt(nb);
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, false, false);

		presenter.applyFilter(excludedRequest, FAMOUS_REQUEST_WITHOUT_CLICK, null, null, null, null);

		QueryResponse queryResponse = presenter.getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size() - 1);

		for(SimpleOrderedMap som : buckets) {
			assertThat(som.get("val")).isNotEqualTo(excludedRequest);
		}
	}

	public StatisticsPresenter getPresenter() {
		return presenter;
	}
}
