package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2017-01-10.
 */
public class DecommissioningBuilderPresenterAcceptanceTest extends ConstellioTest {
    @Mock
    DecommissioningBuilderView view;
    MockedNavigation navigator;
    RMTestRecords records = new RMTestRecords(zeCollection);
    DecommissioningBuilderPresenter presenter;
    SessionContext sessionContext;
    RMSchemasRecordsServices rm;
    MetadataSchemasManager metadataSchemasManager;
    RecordServices recordServices;
    DecommissioningList decommissioningList;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withEvents()
        );

        inCollection(zeCollection).setCollectionTitleTo("Collection de test");

        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

        recordServices = getModelLayerFactory().newRecordServices();

        sessionContext = FakeSessionContext.adminInCollection(zeCollection);
        sessionContext.setCurrentLocale(Locale.FRENCH);

        when(view.getSessionContext()).thenReturn(sessionContext);
        when(view.getCollection()).thenReturn(zeCollection);
        when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
        when(view.navigate()).thenReturn(navigator);

        decommissioningList = buildDefaultDecommissioningList();
        recordServices.add(decommissioningList.getWrappedRecord());
        presenter = spy(new DecommissioningBuilderPresenter(view));
    }

    @Test
    public void givenDecommissioningListThenCalculateGoodSearchType() {
        presenter.forParams("transfer/id/decomTest");
        presenter.forRequestParameters("transfer/id/decomTest");
        assertThat(presenter.getSearchType()).isEqualTo(SearchType.transfer);
        assertThat(presenter.getDecommissioningList().getId()).isEqualTo("decomTest");
        assertThat(presenter.adminUnitId).isEqualTo(records.unitId_10);
        assertThat(presenter.isAddMode()).isFalse();

        presenter = spy(new DecommissioningBuilderPresenter(view));
        presenter.forParams("transfer");
        presenter.forRequestParameters("transfer");
        assertThat(presenter.getSearchType()).isEqualTo(SearchType.transfer);
        assertThat(presenter.getDecommissioningList()).isNull();
        assertThat(presenter.adminUnitId).isNull();
        assertThat(presenter.isAddMode()).isTrue();
    }

    private DecommissioningList buildDefaultDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setAdministrativeUnit(records.unitId_10)
                .addFolderDetailsFor(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04);
    }
}

