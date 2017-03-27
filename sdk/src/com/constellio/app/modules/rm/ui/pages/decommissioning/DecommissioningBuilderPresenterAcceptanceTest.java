package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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
    RecordServices recordServices;
    DecommissioningList decommissioningList;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus().withEvents().withDocumentsHavingContent()
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

        decommissioningList = buildDefaultFolderDecommissioningList();
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

    @Test
    public void givenBuilderInEditModeThenAddRecordsCorrectly() throws RecordServicesException {
        presenter.forParams("transfer/id/decomTest");
        presenter.forRequestParameters("transfer/id/decomTest");
        presenter.addToListButtonClicked(asList(records.folder_A01, records.folder_A05));

        assertThat(rm.getDecommissioningList("decomTest").getFolders()).containsOnly(records.folder_A01,
                records.folder_A02, records.folder_A03, records.folder_A04, records.folder_A05);

        recordServices.add(buildDefaultDocumentDecommissioningList().getWrappedRecord());
        presenter.forParams("documentTransfer/id/decomTestDoc");
        presenter.forRequestParameters("documentTransfer/id/decomTestDoc");
        presenter.addToListButtonClicked(asList(records.document_A19, records.document_B33));

        assertThat(rm.getDecommissioningList("decomTestDoc").getDocuments()).containsOnly(records.document_A19, records.document_A49,
                records.document_A79, records.document_B30, records.document_B33);
    }

    private DecommissioningList buildDefaultFolderDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setAdministrativeUnit(records.unitId_10)
                .addFolderDetailsFor(rm.getFolders(asList(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04)).toArray(new Folder[0]));
    }

    private DecommissioningList buildDefaultDocumentDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTestDoc").setTitle("decomTestDoc").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER).setAdministrativeUnit(records.unitId_10)
                .setDocuments(asList(records.document_A19, records.document_A49, records.document_A79, records.document_B30));
    }
}

