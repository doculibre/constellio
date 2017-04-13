package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
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

    @Test
    public void givenBuilderThenShowOnlyNonBorrowedFolders() throws Exception {
        presenter.forParams("semiActiveToDeposit");
        presenter.forRequestParameters("semiActiveToDeposit");
        presenter.adminUnitId = records.unitId_10a;


        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        LogicalSearchCondition searchCondition = presenter.getSearchCondition();
        List<String> recordIds = searchServices.searchRecordIds(searchCondition);
        assertThat(recordIds).contains(records.folder_A48);


        RecordServices recordServices = getModelLayerFactory().newRecordServices();
        recordServices.physicallyDeleteNoMatterTheStatus(records.getList10().getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());
        recordServices.physicallyDeleteNoMatterTheStatus(records.getList17().getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());
        new BorrowingServices(zeCollection, getModelLayerFactory()).borrowFolder(records.folder_A48, LocalDate.now(),
                LocalDate.now().plusDays(1), records.getAdmin(), records.getAdmin(), BorrowingType.BORROW);

        recordIds = searchServices.searchRecordIds(searchCondition);
        assertThat(recordIds).doesNotContain(records.folder_A48);
    }

    private DecommissioningList buildDefaultFolderDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER).setAdministrativeUnit(records.unitId_10)
                .addFolderDetailsFor(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04);
    }

    private DecommissioningList buildDefaultDocumentDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTestDoc").setTitle("decomTestDoc").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER).setAdministrativeUnit(records.unitId_10)
                .setDocuments(asList(records.document_A19, records.document_A49, records.document_A79, records.document_B30));
    }
}

