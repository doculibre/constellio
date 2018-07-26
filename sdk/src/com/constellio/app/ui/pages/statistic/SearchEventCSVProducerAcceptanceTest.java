package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.containers.SearchEventVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchEventVODataProvider;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.vaadin.ui.Table;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SearchEventCSVProducerAcceptanceTest extends StatisticsPresenterAcceptanceTest {
    @Mock
    private Table table;

    private SearchEventCSVProducer searchEventCSVProducer;

    @Before
    public void init() {
        SearchEventVODataProvider statisticsDataProvider = getPresenter().getStatisticsDataProvider();
        MetadataSchemaVO schema = statisticsDataProvider.getSchema();
        List<String> properties = SearchEventVOLazyContainer.getProperties(schema);

        when(table.getVisibleColumns()).thenReturn(properties.toArray(new Object[0]));

        for (String prop: properties) {
            when(table.getColumnHeader(prop)).thenReturn(prop);
        }
    }

    @Test
    public void givenSearchEventThenCSVFileProduced() throws IOException {
        int nb = 10;
        final List<SearchEvent> searchEvents = addSomeSearchEventForTest(nb, true, true);

        List<RecordVO> recordVOS = getPresenter().getStatisticsDataProvider().listRecordVOs(0, nb);

        assertThat(recordVOS).isNotNull().hasSize(searchEvents.size());

        SearchEventVODataProvider statisticsDataProvider = getPresenter().getStatisticsDataProvider();
        searchEventCSVProducer = new SearchEventCSVProducer(table, (long) nb, statisticsDataProvider);

        File file = null;
        try {
            file = searchEventCSVProducer.produceCSVFile();

            assertThat(file).isNotNull().exists().canRead();

            List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
            assertThat(lines).isNotEmpty().hasSize(nb + 1);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
