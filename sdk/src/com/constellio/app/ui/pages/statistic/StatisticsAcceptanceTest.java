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

public class StatisticsAcceptanceTest extends ConstellioTest {
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

	protected List<SearchEvent> fromRecodVOs(List<RecordVO> recordVOS) {
		List<SearchEvent> results = new ArrayList<>();

		for (RecordVO recordVO : recordVOS) {
			results.add(schemasRecordsServices.wrapSearchEvent(recordVO.getRecord()));
		}

		return results;
	}

	public StatisticsPresenter getPresenter() {
		return presenter;
	}
}
