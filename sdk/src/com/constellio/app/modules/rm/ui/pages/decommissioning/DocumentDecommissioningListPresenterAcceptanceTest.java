package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by Constellio on 2017-01-10.
 */
public class DocumentDecommissioningListPresenterAcceptanceTest extends ConstellioTest {
    @Mock
    DocumentDecommissioningListView view;
    MockedNavigation navigator;
    RMTestRecords records = new RMTestRecords(zeCollection);
    DocumentDecommissioningListPresenter presenter;
    SessionContext sessionContext;
    RMSchemasRecordsServices rm;
    MetadataSchemasManager metadataSchemasManager;
    RecordServices recordServices;
    DecommissioningList decommissioningList;

    @Before
    public void setUp()
            throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records).withDocumentsHavingContent()
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
        presenter = spy(new DocumentDecommissioningListPresenter(view));
        doReturn(decommissioningList).when(presenter).decommissioningList();
        doReturn(rm).when(presenter).rmRecordsServices();
        doNothing().when(presenter).refreshView();
    }

    @Test
    public void givenDecommissioningListThenCalculateGoodSearchType() {
        decommissioningList.setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.documentTransfer);

        decommissioningList.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_DEPOSIT);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.documentActiveToDeposit);

        decommissioningList.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_DESTROY);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.documentActiveToDestroy);

        decommissioningList.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
                .setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_DEPOSIT);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.documentSemiActiveToDeposit);

        decommissioningList.setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_DESTROY);
        assertThat(presenter.calculateSearchType()).isEqualTo(SearchType.documentSemiActiveToDestroy);
    }

    @Test
    public void whenRemoveDocumentsClickedThenRemoveGoodDocuments() {
        presenter.forRecordId(decommissioningList.getId());
        List<RecordVO> recordVOList = presenter.getDocuments().listRecordVOs(0,4);
        presenter.removeDocumentsButtonClicked(buildSelectedMap(false, true, false, false));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getDocuments()).containsOnly(records.document_A19, records.document_A79, records.document_B30);
        recordVOList = presenter.getDocuments().listRecordVOs(0,4);
        presenter.removeDocumentsButtonClicked(buildSelectedMap(true, true, false));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getDocuments()).containsOnly(records.document_B30);
        recordVOList = presenter.getDocuments().listRecordVOs(0,4);
        presenter.removeDocumentsButtonClicked(buildSelectedMap(true));
        assertThat(rm.getDecommissioningList(decommissioningList.getId()).getDocuments()).isEmpty();
    }

    private DecommissioningList buildDefaultDecommissioningList() {
        return rm.newDecommissioningListWithId("decomTest").setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
                .setDecommissioningListType(DecommissioningListType.DOCUMENTS_TO_TRANSFER)
                .addDocuments(records.document_A19, records.document_A49, records.document_A79, records.document_B30);
    }

    private HashMap<Integer, Boolean> buildSelectedMap(boolean... selected) {
        HashMap<Integer, Boolean> selectedMap = new HashMap<>();
        for(int i = 0; i < selected.length; i++) {
            selectedMap.put(i, selected[i]);
        }
        return selectedMap;
    }
}
