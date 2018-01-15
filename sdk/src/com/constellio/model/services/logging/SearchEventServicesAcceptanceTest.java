package com.constellio.model.services.logging;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.background.ModelLayerBackgroundThreadsManager;
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
        assertThat(searchServices.getResultsCount(countSearchEventsQuery)).isEqualTo(5000);

    }

    @Test
    public void whenSavingPlethoraOfClicksThenReallyFast()
            throws Exception {

        long timeStampBefore = new Date().getTime();

        searchEventServices.save(schemas.newSearchEventWithId("search1").setUsername("bob").setQuery("banane"));
        searchEventServices.save(schemas.newSearchEventWithId("search2").setUsername("bob").setQuery("banane"));
        searchEventServices.save(schemas.newSearchEventWithId("search3").setUsername("bob").setQuery("banane"));

        for (int i = 0; i < 5000; i++) {
            searchEventServices.incrementClickCounter("search1");
        }
        for (int i = 0; i < 500; i++) {
            searchEventServices.incrementClickCounter("search2");
            searchEventServices.incrementPageNavigationCounter("search2");
        }
        for (int i = 0; i < 2500; i++) {
            searchEventServices.incrementClickCounter("search3");
            searchEventServices.incrementPageNavigationCounter("search3");
        }

        long timeStampAfter = new Date().getTime();

        getDataLayerFactory().newEventsDao().flush();

        assertThat(timeStampAfter - timeStampBefore).isLessThan(TWO_SECONDS);
        SearchEvent event1 = schemas.getSearchEvent("search1");
        SearchEvent event2 = schemas.getSearchEvent("search2");
        SearchEvent event3 = schemas.getSearchEvent("search3");

        assertThat(event1.getClickCount()).isEqualTo(5000);
        assertThat(event1.getPageNavigationCount()).isEqualTo(0);
        assertThat(event2.getClickCount()).isEqualTo(500);
        assertThat(event2.getPageNavigationCount()).isEqualTo(500);
        assertThat(event3.getClickCount()).isEqualTo(2500);
        assertThat(event3.getPageNavigationCount()).isEqualTo(2500);

    }
}
