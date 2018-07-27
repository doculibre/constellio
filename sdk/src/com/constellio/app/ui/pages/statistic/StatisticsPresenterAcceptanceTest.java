package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import org.apache.commons.lang3.RandomUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.constellio.app.ui.pages.statistic.StatisticsPresenter.*;
import static org.assertj.core.api.Assertions.assertThat;

public class StatisticsPresenterAcceptanceTest extends StatisticsAcceptanceTest {

	@Test
	public void givenFixedNumberOfEventAddedThenSameNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		List<RecordVO> recordVOS = getPresenter().getStatisticsDataProvider().listRecordVOs(0, nb);

		assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents);
	}

	@Test
	public void givenSavedEventsWhenExcludedThenNotFound() {
		int nb = 10;
		List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		SearchEvent excluded = searchEvents.remove(RandomUtils.nextInt(0, searchEvents.size()));

		getPresenter().applyFilter(excluded.getQuery(), null, null, null, null, null);
		List<RecordVO> recordVOS = getPresenter().getStatisticsDataProvider().listRecordVOs(0, nb);

		assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
		assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents).doesNotContain(excluded);
	}

	@Test(expected = IllegalArgumentException.class)
	public void givenUnknownStatisticTypeThenException() {
		getPresenter().applyFilter(null, null, null, null, null, null);

		getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, 15);
	}

	@Test
	public void givenFixedNumberOfEventAddedThenSameFamousRequestFacetNumberFound() {
		int nb = 10;
		final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

		getPresenter().applyFilter(null, FAMOUS_REQUEST, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

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

		getPresenter().applyFilter(null, FAMOUS_REQUEST_WITH_RESULT, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

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

		getPresenter().applyFilter(null, FAMOUS_REQUEST_WITHOUT_RESULT, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

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

		getPresenter().applyFilter(null, FAMOUS_REQUEST_WITH_CLICK, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

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

		getPresenter().applyFilter(null, FAMOUS_REQUEST_WITHOUT_CLICK, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

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

		getPresenter().applyFilter(excludedRequest, FAMOUS_REQUEST_WITHOUT_CLICK, null, null, null, null);

		QueryResponse queryResponse = getPresenter().getStatisticsFacetsDataProvider().getQueryResponse(0, nb);

		NamedList<Object> namedList = queryResponse.getResponse();

		SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
		SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
		ArrayList<SimpleOrderedMap> buckets = (ArrayList<SimpleOrderedMap>) queryS.get("buckets");

		assertThat(buckets).isNotNull().hasSize(searchEvents.size() - 1);

		for(SimpleOrderedMap som : buckets) {
			assertThat(som.get("val")).isNotEqualTo(excludedRequest);
		}
	}
}
