package com.constellio.app.ui.pages.trash;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2017-06-06.
 */
public class TrashPresenterAcceptanceTest extends ConstellioTest {
    @Mock
    TrashViewImpl view;
    MockedNavigation navigator;
    RMTestRecords records = new RMTestRecords(zeCollection);
    TrashPresenter presenter;
    SessionContext sessionContext;
    RMSchemasRecordsServices rmSchemasRecordsServices;
    MetadataSchemasManager metadataSchemasManager;
    RecordServices recordServices;

    @Before
    public void setup() {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withEvents()
        );

        sessionContext = FakeSessionContext.adminInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        recordServices = getModelLayerFactory().newRecordServices();

        when(view.getSessionContext()).thenReturn(sessionContext);
        when(view.getCollection()).thenReturn(zeCollection);
        when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
        navigator = new MockedNavigation();
        when(view.navigate()).thenReturn(navigator);

        presenter = new TrashPresenter(view);
    }

    @Test
    public void givenSecuredSchemaTypeThenFindCorrectRecords() {
        assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(0);

        recordServices.logicallyDelete(records.getFolder_A01().getWrappedRecord(), User.GOD);
        assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(1);
    }
}
