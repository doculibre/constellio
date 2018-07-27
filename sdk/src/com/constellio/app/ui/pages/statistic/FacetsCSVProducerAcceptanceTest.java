package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.framework.data.FacetsDataProvider;
import com.vaadin.ui.Table;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.constellio.app.ui.pages.statistic.StatisticsPresenter.*;
import static com.constellio.app.ui.pages.statistic.StatisticsViewImpl.initStatisticsColumnsHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FacetsCSVProducerAcceptanceTest extends StatisticsAcceptanceTest {
    @Mock
    private Table table;

    @Test
    public void givenFacetsFamousRequestThenCSVFileProduced() throws IOException {
        givenSearchEventWithStatisticsTypeThenCSVFileProduced(FAMOUS_REQUEST);
    }

    @Test
    public void givenFacetsFamousRequestWithClickThenCSVFileProduced() throws IOException {
        givenSearchEventWithStatisticsTypeThenCSVFileProduced(FAMOUS_REQUEST_WITH_CLICK);
    }

    @Test
    public void givenFacetsFamousRequestWithoutClickThenCSVFileProduced() throws IOException {
        givenSearchEventWithStatisticsTypeThenCSVFileProduced(FAMOUS_REQUEST_WITHOUT_CLICK);
    }

    @Test
    public void givenFacetsFamousRequestWithResultThenCSVFileProduced() throws IOException {
        givenSearchEventWithStatisticsTypeThenCSVFileProduced(FAMOUS_REQUEST_WITH_RESULT);
    }

    @Test
    public void givenFacetsFamousRequestWithoutResultThenCSVFileProduced() throws IOException {
        givenSearchEventWithStatisticsTypeThenCSVFileProduced(FAMOUS_REQUEST_WITHOUT_RESULT);
    }

    public void givenSearchEventWithStatisticsTypeThenCSVFileProduced(String statisticsType) throws IOException {
        int nb = 10;
        List<String> properties = initStatisticsColumnsHeader(statisticsType);

        when(table.getVisibleColumns()).thenReturn(properties.toArray(new Object[0]));

        for (String prop: properties) {
            when(table.getColumnHeader(prop)).thenReturn(prop);
        }

        if(FAMOUS_REQUEST.equals(statisticsType)) {
            addSomeSearchEventForTest(nb, true, true);
        } else if(FAMOUS_REQUEST_WITH_CLICK.equals(statisticsType)) {
            addSomeSearchEventForTest(nb, true, false);
        } else if(FAMOUS_REQUEST_WITHOUT_CLICK.equals(statisticsType)) {
            addSomeSearchEventForTest(nb, false, false);
        } else if(FAMOUS_REQUEST_WITH_RESULT.equals(statisticsType)) {
            addSomeSearchEventForTest(nb, false, true);
        } else if(FAMOUS_REQUEST_WITHOUT_RESULT.equals(statisticsType)) {
            addSomeSearchEventForTest(nb, false, false);
        }

        getPresenter().applyFilter(null, statisticsType, null, null, null, null);

        FacetsDataProvider statisticsFacetsDataProvider = getPresenter().getStatisticsFacetsDataProvider();

        FacetsCSVProducer facetsCSVProducer = new FacetsCSVProducer(table, (long) nb, statisticsFacetsDataProvider, properties);

        File file = null;
        try {
            file = facetsCSVProducer.produceCSVFile();

            assertThat(file).isNotNull().exists().canRead();

            List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
            assertThat(lines).isNotEmpty().hasSize(nb + 1);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
