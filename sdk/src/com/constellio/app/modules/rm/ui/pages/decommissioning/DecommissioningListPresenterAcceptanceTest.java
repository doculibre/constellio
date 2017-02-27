package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
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
        doNothing().when(presenter).folderPlacedInContainer(any(FolderDetailVO.class), any(ContainerVO.class));
        doReturn(null).when(view).getContainer(any(ContainerRecord.class));
        doReturn(null).when(view).getPackageableFolder(any(String.class));
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

    @Test
    public void givenAutofillButtonIsClickedThenContainersAreFilledCorrectly() throws RecordServicesException {
        buildAutoFillContainers();
        presenter.forRecordId(decommissioningList.getId());
        Map<String, Double> foldersWithSize = new HashMap<>();
        foldersWithSize.put(records.folder_A01, 40D);
        foldersWithSize.put(records.folder_A02, 50D);
        foldersWithSize.put(records.folder_A03, 70D);
        foldersWithSize.put(records.folder_A04, 30D);

        presenter.autoFillContainersRequested(foldersWithSize);

//        assertThatRecords(records.getFolder_A01(), records.getFolder_A02(), records.getFolder_A03(), records.getFolder_A04())
//                .extractingMetadatas(Folder.LINEAR_SIZE).containsOnly(tuple(10), tuple(10), tuple(10), tuple(10));

        assertThatRecords(records.getFolder_A01(), records.getFolder_A02(), records.getFolder_A03(), records.getFolder_A04())
                .extractingMetadatas(Schemas.IDENTIFIER.getLocalCode(), Folder.CONTAINER, Folder.LINEAR_SIZE)
                .containsOnly(
                        tuple(records.folder_A01, "container100", 40D),
                        tuple(records.folder_A02, "container150", 50D),
                        tuple(records.folder_A03, "container150", 70D),
                        tuple(records.folder_A04, "container100", 30D)
                );
    }

    private DecommissioningList buildDefaultDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setAdministrativeUnit(records.unitId_10).setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER)
                .addFolderDetailsFor(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04);
    }

    private void buildAutoFillContainers() throws RecordServicesException {
        recordServices.add(rm.newContainerRecordWithId("container100").setType(records.containerTypeId_boite22x22)
                .setTemporaryIdentifier("container100").setAdministrativeUnit(records.getUnit10()).setCapacity(100));
        recordServices.add(rm.newContainerRecordWithId("container150").setType(records.containerTypeId_boite22x22)
                .setTemporaryIdentifier("container150").setAdministrativeUnit(records.getUnit10()).setCapacity(150));
        recordServices.add(rm.newContainerRecordWithId("container25").setType(records.containerTypeId_boite22x22)
                .setTemporaryIdentifier("container25").setAdministrativeUnit(records.getUnit10()).setCapacity(25));
    }
}
