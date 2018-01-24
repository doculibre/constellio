package com.constellio.app.ui.pages.statistic;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.logging.SearchEventServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    private List<SearchEvent> addSomeSearchEventForTest(int nb) {
        List<SearchEvent> searchEvents = new ArrayList<>(nb);

        for(int i=0; i<nb; i++) {
            SearchEvent searchEvent = schemasRecordsServices.newSearchEvent();

            searchEvent.setClickCount(2*i);
            searchEvent.setPageNavigationCount(3*i);
            searchEvent.setParams(Arrays.asList("params"+i));
            searchEvent.setQuery("query"+i);

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

        for (RecordVO recordVO: recordVOS) {
            results.add(schemasRecordsServices.wrapSearchEvent(recordVO.getRecord()));
        }

        return results;
    }

    @Test
    public void givenFixedNumberOfEventAddedThenProvidedAtOnce() {
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

        presenter.applyFilter(excluded.getQuery(), null, null, null, null);
        List<RecordVO> recordVOS = presenter.getStatisticsDataProvider().listRecordVOs(0, nb);

        Assertions.assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());
        Assertions.assertThat(fromRecodVOs(recordVOS)).containsAll(searchEvents).doesNotContain(excluded);
    }
}
