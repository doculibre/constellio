package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SummaryColumnPresenterAcceptanceTest extends ConstellioTest {

    @Mock
    SummaryColumnView view;
    MockedNavigation navigator;
    @Mock
    SessionContext sessionContext;

    SummaryColumnPresenter summaryColumnPresenter;
    Users users;
    RMTestRecords records = new RMTestRecords(zeCollection);

    @Before
    public void setUp() {
        users = new Users();
        prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));

        navigator = new MockedNavigation();

        when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
        when(view.getCollection()).thenReturn(zeCollection);
        when(view.getSessionContext()).thenReturn(sessionContext);
        when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
        when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

        summaryColumnPresenter = new SummaryColumnPresenter(view, Folder.DEFAULT_SCHEMA);
    }

    @Test
    public void givenOneMetatSummaryThenMetadataHaveMetadataSummy() {

        MetadataToVOBuilder builder = new MetadataToVOBuilder();
        summaryColumnPresenter.getMetadatas();
        SummaryColumnParams summaryColumnParams = new SummaryColumnParams();
        summaryColumnParams.setPrefix("a");
        summaryColumnParams.setDisplayCondition(SummaryColumnParams.DisplayCondition.ALWAYS);
        summaryColumnParams.setMetadataVO(builder.build(Schemas.TITLE, FakeSessionContext.adminInCollection(zeCollection)));

        summaryColumnPresenter.addMetadaForSummary(summaryColumnParams);

        Metadata metadata = summaryColumnPresenter.getSummaryMetadata();

        List summaryComlomnList = (List) metadata.getCustomParameter().get(SummaryColumnPresenter.SUMMARY_COLOMN);

        assertThat(summaryComlomnList).isNotNull();
        assertThat(summaryComlomnList.size()).isEqualTo(1);
        assertThat(summaryColumnParams.getMetadataVO().getCode()).isEqualTo(Schemas.TITLE.getCode());
        assertThat(summaryColumnParams.getPrefix()).isEqualTo("a");
        assertThat(summaryColumnParams.getDisplayCondition()).isEqualTo(SummaryColumnParams.DisplayCondition.ALWAYS);
    }
}
