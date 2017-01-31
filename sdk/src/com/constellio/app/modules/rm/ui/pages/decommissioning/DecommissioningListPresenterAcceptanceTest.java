package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by Constellio on 2017-01-10.
 */
public class DecommissioningListPresenterAcceptanceTest extends ConstellioTest {
    @Mock
    DecommissioningListView view;
    MockedNavigation navigator;
    RMTestRecords records = new RMTestRecords(zeCollection);
    DecommissioningListPresenter presenter;
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
        presenter = spy(new DecommissioningListPresenter(view));
        doReturn(decommissioningList).when(presenter).decommissioningList();
        doReturn(rm).when(presenter).rmRecordsServices();
        doNothing().when(presenter).refreshView();
    }

    @Test
    public void givenDecommissioningListThenCalculateGoodSearchType() {
        decommissioningList.setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.transfer);

        decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DEPOSIT);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.activeToDeposit);

        decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DESTROY);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.activeToDestroy);

        decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DEPOSIT);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.semiActiveToDeposit);

        decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DESTROY);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.semiActiveToDestroy);
    }

    @Test
    public void whenRemoveFoldersClickedThenRemoveGoodFolders() {
        presenter.forRecordId(decommissioningList.getId());
        FolderDetailVO detail1 = new FolderDetailVO();
        FolderDetailVO detail2 = new FolderDetailVO();
        detail1.setFolderId(records.folder_A02);
        presenter.removeFoldersButtonClicked(asList(detail1));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getFolders()).containsOnly(records.folder_A01, records.folder_A03, records.folder_A04);
        detail1.setFolderId(records.folder_A01);
        detail2.setFolderId(records.folder_A03);
        presenter.removeFoldersButtonClicked(asList(detail1, detail2));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getFolders()).containsOnly(records.folder_A04);
        detail1.setFolderId(records.folder_A04);
        presenter.removeFoldersButtonClicked(asList(detail1));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getFolders()).isEmpty();
    }

    private DecommissioningList buildDefaultDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER)
                .addFolderDetailsFor(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04);
    }
}
