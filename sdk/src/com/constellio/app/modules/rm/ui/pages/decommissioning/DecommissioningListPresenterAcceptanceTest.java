package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningEmailServiceException;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderComponent;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
		doReturn(null).when(view).getContainer(any(ContainerRecord.class));
		doReturn(null).when(view).getPackageableFolder(any(String.class));
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
		doReturn(null).when(view).getContainer(any(ContainerRecord.class));
		doReturn(null).when(view).getPackageableFolder(any(String.class));
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
	public void whenRefreshListThenCanAskStillAskForApproval() throws DecommissioningEmailServiceException {
		doReturn(null).when(view).getContainer(any(ContainerRecord.class));
		doReturn(null).when(view).getPackageableFolder(any(String.class));
		presenter.forRecordId(decommissioningList.getId());
		presenter.refreshList();
		presenter.getAvailableManagers();
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

		FolderDetailToVOBuilder folderBuilder = new FolderDetailToVOBuilder(rm, recordServices);
		FolderDetailVO folder1 = folderBuilder.build(decommissioningList.getFolderDetailWithType(records.folder_A01), FolderComponent.PACKAGEABLE_FOLDER_COMPONENT);
		folder1.setLinearSize(40D);
		FolderDetailVO folder2 = folderBuilder.build(decommissioningList.getFolderDetailWithType(records.folder_A02), FolderComponent.PACKAGEABLE_FOLDER_COMPONENT);
		folder2.setLinearSize(50D);
		FolderDetailVO folder3 = folderBuilder.build(decommissioningList.getFolderDetailWithType(records.folder_A03), FolderComponent.PACKAGEABLE_FOLDER_COMPONENT);
		folder3.setLinearSize(70D);
		FolderDetailVO folder4 = folderBuilder.build(decommissioningList.getFolderDetailWithType(records.folder_A04), FolderComponent.PACKAGEABLE_FOLDER_COMPONENT);
		folder4.setLinearSize(30D);
		doReturn(folder1).when(view).getPackageableFolder(records.folder_A01);
		doReturn(folder2).when(view).getPackageableFolder(records.folder_A02);
		doReturn(folder3).when(view).getPackageableFolder(records.folder_A03);
		doReturn(folder4).when(view).getPackageableFolder(records.folder_A04);
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				boolean container100 = decommissioningList.getContainerDetail("container100") == null;
				return container100 ?
					   new ContainerVO("container100", "container100", 100D, null) :
					   new ContainerVO("container100", "container100", decommissioningList.getContainerDetail("container100").getAvailableSize(), null);
			}
		}).when(view).getContainer(rm.getContainerRecord("container100"));
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				boolean container150 = decommissioningList.getContainerDetail("container150") == null;
				return container150 ?
					   new ContainerVO("container150", "container150", 150D, null) :
					   new ContainerVO("container150", "container150", decommissioningList.getContainerDetail("container150").getAvailableSize(), null);
			}
		}).when(view).getContainer(rm.getContainerRecord("container150"));

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				DecomListContainerDetail newContainerDetail100 = decommissioningList.getContainerDetail("container100");
				if (newContainerDetail100 == null) {
					decommissioningList.addContainerDetailsFrom(asList(rm.getContainerRecord("container100")));
					decommissioningList.getContainerDetail("container100").setAvailableSize(getSizeInContainer("container100"));
				}
				DecomListContainerDetail newContainerDetail150 = decommissioningList.getContainerDetail("container150");
				if (newContainerDetail150 == null) {
					decommissioningList.addContainerDetailsFrom(asList(rm.getContainerRecord("container150")));
					decommissioningList.getContainerDetail("container150").setAvailableSize(getSizeInContainer("container150"));
				}
				recordServices.update(decommissioningList);
				return null;
			}
		}).when(view).addUpdateContainer(any(ContainerVO.class), any(DecomListContainerDetail.class));

		presenter.autoFillContainersRequested(foldersWithSize);

		assertThat(asList(decommissioningList.getFolderDetail(records.folder_A01), decommissioningList.getFolderDetail(records.folder_A02),
				decommissioningList.getFolderDetail(records.folder_A03), decommissioningList.getFolderDetail(records.folder_A04)))
				.extracting("folderId", "containerRecordId", "folderLinearSize")
				.containsOnly(
						tuple(records.folder_A01, "container100", 40D),
						tuple(records.folder_A02, "container150", 50D),
						tuple(records.folder_A03, "container150", 70D),
						tuple(records.folder_A04, "container100", 30D)
				);

		assertThatRecords(records.getFolder_A01(), records.getFolder_A02(), records.getFolder_A03(), records.getFolder_A04())
				.extractingMetadatas(Schemas.IDENTIFIER.getLocalCode(), Folder.CONTAINER, Folder.LINEAR_SIZE)
				.containsOnly(
						tuple(records.folder_A01, null, null),
						tuple(records.folder_A02, null, null),
						tuple(records.folder_A03, null, null),
						tuple(records.folder_A04, null, null)
				);
	}

	private Double getSizeInContainer(String id) {
		Double size = rm.getContainerRecord(id).getCapacity();
		for (DecomListFolderDetail folder : rm.getDecommissioningList(decommissioningList.getId()).getFolderDetails()) {
			if (id.equals(folder.getContainerRecordId())) {
				size -= folder.getFolderLinearSize();
			}
		}
		return size;
	}

	private DecommissioningList buildDefaultDecommissioningList() {
		return rm.newDecommissioningListWithId("decomTest").setAdministrativeUnit(records.unitId_10).setTitle("decomTest").setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER)
				.addFolderDetailsFor(FolderDetailStatus.INCLUDED, rm.getFolders(asList(records.folder_A01, records.folder_A02, records.folder_A03, records.folder_A04)).toArray(new Folder[0]));
	}

	private void buildAutoFillContainers() throws RecordServicesException {
		recordServices.add(rm.newContainerRecordWithId("container100").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("container100").setAdministrativeUnit(records.getUnit10()).setCapacity(100).setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE));
		recordServices.add(rm.newContainerRecordWithId("container150").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("container150").setAdministrativeUnit(records.getUnit10()).setCapacity(150).setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE));
		recordServices.add(rm.newContainerRecordWithId("container25").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("container25").setAdministrativeUnit(records.getUnit10()).setCapacity(25).setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE));
	}
}
