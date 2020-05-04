package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderComponent;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.RecordsCaches2Impl;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DecommissioningListPresenterTest extends ConstellioTest {
	public static final String ZE_LIST = "zeList";

	@Mock DecommissioningListView view;
	@Mock DecommissioningService service;
	@Mock RMSchemasRecordsServices rm;
	@Mock PresenterService presenterService;
	@Mock DecommissioningList list;
	@Mock Record record;
	@Mock User user;
	@Mock FolderDetailVO folderDetailVO;
	@Mock FolderDetailVO processable1;
	@Mock FolderDetailVO processable2;
	@Mock FolderDetailVO packageable1;
	@Mock FolderDetailVO packageable2;
	@Mock FolderDetailVO packageable1Detail;
	@Mock FolderDetailVO packageable2Detail;
	@Mock ContainerVO containerVO;
	@Mock DecomListFolderDetail folderDetail;
	MockedNavigation navigator;
	MockedFactories factories = new MockedFactories();

	DecommissioningListPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		when(factories.getAppLayerFactory().newPresenterService()).thenReturn(presenterService);
		when(factories.getModelLayerFactory().getRecordsCaches())
				.thenReturn(mock(RecordsCaches2Impl.class));
		when(presenterService.getCurrentUser(isA(SessionContext.class))).thenReturn(user);
		factories.getRecordServices();

		navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		when(rm.getDecommissioningList(ZE_LIST)).thenReturn(list);
		when(list.getWrappedRecord()).thenReturn(record);

		presenter = spy(new DecommissioningListPresenter(view).forRecordId(ZE_LIST));

		doReturn(service).when(presenter).decommissioningService();
		doReturn(false).when(presenter).isFolderPlacedInContainer(any(FolderDetailWithType.class));
		doReturn(rm).when(presenter).rmRecordsServices();

		UI.setCurrent(new UI() {
			@Override
			protected void init(VaadinRequest request) {

			}

			@Override
			public Locale getLocale() {
				return Locale.CANADA;
			}
		});
	}

	@After
	public void tearDown() {
		UI.setCurrent(null);
	}

	@Test
	public void givenIsEditableThenAskTheDecommissioningService() {
		when(service.isEditable(list, user)).thenReturn(true);
		assertThat(presenter.isEditable()).isTrue();
		verify(service, times(1)).isEditable(list, user);
	}

	@Test
	public void givenEditButtonClickedThenNavigateToEditWindow() {
		presenter.editButtonClicked();
		verify(navigator.to(RMViews.class), times(1)).editDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenIsDeletableThenAskTheDecommissioningService() {
		when(service.isDeletable(list, user)).thenReturn(true);
		assertThat(presenter.isDeletable()).isTrue();
		verify(service, times(1)).isDeletable(list, user);
	}

	@Test
	public void givenDeleteButtonClickedThenDeleteTheListAndReturnToMainPage() {
		RecordServices recordServices = factories.getRecordServices();
		when(factories.getModelLayerFactory().newLoggingServices()).thenReturn(mock(LoggingServices.class));
		when(recordServices.validateLogicallyThenPhysicallyDeletable(record, user)).thenReturn(new ValidationErrors());

		presenter.deleteButtonClicked();
		verify(factories.getRecordServices(), times(1))
				.logicallyDelete(eq(record), eq(user), any(RecordLogicalDeleteOptions.class));
		verify(factories.getRecordServices(), times(1))
				.physicallyDeleteNoMatterTheStatus(eq(record), eq(user), any(RecordPhysicalDeleteOptions.class));
	}

	@Test
	public void givenIsProcessableThenAskTheDecommissioningService() {
		when(service.isProcessable(list, user)).thenReturn(true);
		assertThat(presenter.isProcessable()).isTrue();
		verify(service, times(1)).isProcessable(list, user);
	}

	@Test
	public void givenProcessButtonClickedThenProcessTheListAndRefreshWithMessage() throws Exception {
		doReturn(true).when(presenter).isListReadyToBeProcessed();
		presenter.processButtonClicked();
		verify(service, times(1)).decommission(list, user);
		verify(view, times(1)).showMessage(anyString());
	}

	@Test
	public void givenContainerCreationRequestedThenNavigateToContainerCreation() {
		presenter.containerCreationRequested();
		verify(navigator.to(RMViews.class), times(1)).createContainerForDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenContainerSearchRequestedThenNavigateToContainerSearch() {
		presenter.containerSearchRequested();
		verify(navigator.to(RMViews.class), times(1)).searchContainerForDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenFolderPlacedInContainerThenUpdateDecommissioningListAndView()
			throws Exception {
		when(folderDetailVO.getFolderId()).thenReturn("zeFolder");
		when(containerVO.getId()).thenReturn("zeContainer");
		when(list.getFolderDetail("zeFolder")).thenReturn(folderDetail);
		when(folderDetail.setFolderLinearSize(any(Double.class))).thenReturn(folderDetail);
		when(folderDetail.setContainerRecordId(any(String.class))).thenReturn(folderDetail);
		when(service.isProcessable(list, user)).thenReturn(true);

		presenter.folderPlacedInContainer(folderDetailVO, containerVO);
		verify(folderDetail, times(1)).setContainerRecordId("zeContainer");
		verify(factories.getRecordServices()).executeHandlingImpactsAsync(isA(Transaction.class));
		verify(view, times(1)).setProcessable(folderDetailVO);
		verify(view, times(1)).updateProcessButtonState(true);
	}

	@Test
	public void givenFolderSortedWhenFolderBecomesProcessableThenUpdateDecommissioningListAndView()
			throws RecordServicesException {
		when(folderDetailVO.getFolderId()).thenReturn("zeFolder");
		FolderDetailWithType detailWithType = mock(FolderDetailWithType.class, "FolderDetailWithType");
		when(list.getFolderDetailWithType("zeFolder")).thenReturn(detailWithType);
		when(detailWithType.getDetail()).thenReturn(folderDetail);
		when(service.isFolderProcessable(list, detailWithType)).thenReturn(true);

		presenter.folderSorted(folderDetailVO, true);
		verify(folderDetail, times(1)).setReversedSort(true);
		verify(factories.getRecordServices()).executeHandlingImpactsAsync(isA(Transaction.class));
		verify(folderDetailVO, times(1)).setPackageable(false);
		verify(view, times(1)).setProcessable(folderDetailVO);
	}

	@Test
	public void givenFolderSortedWhenFolderBecomesNotProcessableThenUpdateDecommissioningListAndView()
			throws RecordServicesException {
		when(folderDetailVO.getFolderId()).thenReturn("zeFolder");
		FolderDetailWithType detailWithType = mock(FolderDetailWithType.class, "FolderDetailWithType");
		when(list.getFolderDetailWithType("zeFolder")).thenReturn(detailWithType);
		when(detailWithType.getDetail()).thenReturn(folderDetail);
		when(service.isFolderProcessable(list, detailWithType)).thenReturn(false);

		presenter.folderSorted(folderDetailVO, true);
		verify(folderDetail, times(1)).setReversedSort(true);
		verify(factories.getRecordServices()).executeHandlingImpactsAsync(isA(Transaction.class));
		verify(folderDetailVO, times(1)).setPackageable(true);
		verify(view, times(1)).setPackageable(folderDetailVO);
	}

	@Test
	public void givenContainerStatusChangedThenUpdateTheDecommissioningList()
			throws RecordServicesException {
		DecomListContainerDetail detail = mock(DecomListContainerDetail.class, "DecomListContainerDetail");
		presenter.containerStatusChanged(detail, true);
		verify(detail, times(1)).setFull(true);
		verify(factories.getRecordServices()).executeHandlingImpactsAsync(isA(Transaction.class));
	}

	@Test
	public void givenPackageableFoldersAskedThenReturnThePackageableFolders() {
		doReturn(null).when(presenter).getFolderDetailTableExtension();
		givenTwoProcessableFoldersAndTwoUnprocessableFolders();
		assertThat(presenter.getPackageableFolders()).containsExactly(packageable1, packageable2);
	}

	@Test
	public void givenProcessableFoldersAskedThenReturnTheProcessableFolders() {
		givenTwoProcessableFoldersAndTwoUnprocessableFolders();
		assertThat(presenter.getProcessableFolders()).containsExactly(processable1, processable2);
	}

	@Test
	public void whenShouldAllowContainerEditingThenAskTheDecommissioningService() {
		when(service.canEditContainers(list, user)).thenReturn(true);
		assertThat(presenter.shouldAllowContainerEditing()).isTrue();
		verify(service, times(1)).canEditContainers(list, user);
	}

	@Test
	public void whenShouldDisplaySortThenAskTheDecommissioningService() {
		when(service.isSortable(list)).thenReturn(true);
		assertThat(presenter.shouldDisplaySort()).isTrue();
		verify(service, times(1)).isSortable(list);
	}

	private void givenTwoProcessableFoldersAndTwoUnprocessableFolders() {
		FolderDetailWithType detail1 = mock(FolderDetailWithType.class);
		FolderDetailWithType detail2 = mock(FolderDetailWithType.class);
		FolderDetailWithType detail3 = mock(FolderDetailWithType.class);
		FolderDetailWithType detail4 = mock(FolderDetailWithType.class);

		when(detail1.isIncluded()).thenReturn(true);
		when(detail2.isIncluded()).thenReturn(true);
		when(detail3.isIncluded()).thenReturn(true);
		when(detail4.isIncluded()).thenReturn(true);

		when(service.isFolderProcessable(list, detail1)).thenReturn(true);
		when(service.isFolderProcessable(list, detail2)).thenReturn(false);
		when(service.isFolderProcessable(list, detail3)).thenReturn(true);
		when(service.isFolderProcessable(list, detail4)).thenReturn(false);

		FolderDetailToVOBuilder builder = mock(FolderDetailToVOBuilder.class, "FolderDetailToVOBuilder");
		when(builder.build(detail1, FolderComponent.PROCESSABLE_FOLDER_COMPONENT)).thenReturn(processable1);
		when(builder.build(detail2, FolderComponent.PACKAGEABLE_FOLDER_COMPONENT)).thenReturn(packageable1);
		when(builder.build(detail3, FolderComponent.PROCESSABLE_FOLDER_COMPONENT)).thenReturn(processable2);
		when(builder.build(detail4, FolderComponent.PACKAGEABLE_FOLDER_COMPONENT)).thenReturn(packageable2);

		when(list.getFolderDetailsWithType()).thenReturn(Arrays.asList(detail1, detail2, detail3, detail4));
		doReturn(builder).when(presenter).folderDetailToVOBuilder();
	}
}
