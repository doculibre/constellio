/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

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
	@Mock ContainerVO containerVO;
	@Mock DecomListFolderDetail folderDetail;
	@Mock ConstellioNavigator navigator;
	MockedFactories factories = new MockedFactories();

	DecommissioningListPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		when(factories.getAppLayerFactory().newPresenterService()).thenReturn(presenterService);
		when(presenterService.getCurrentUser(isA(SessionContext.class))).thenReturn(user);
		factories.getRecordServices();

		when(view.navigateTo()).thenReturn(navigator);

		when(rm.getDecommissioningList(ZE_LIST)).thenReturn(list);
		when(list.getWrappedRecord()).thenReturn(record);

		presenter = spy(new DecommissioningListPresenter(view).forRecordId(ZE_LIST));

		doReturn(service).when(presenter).decommissioningService();
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
		verify(navigator, times(1)).editDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenIsDeletableThenAskTheDecommissioningService() {
		when(service.isDeletable(list, user)).thenReturn(true);
		assertThat(presenter.isDeletable()).isTrue();
		verify(service, times(1)).isDeletable(list, user);
	}

	@Test
	public void givenDeleteButtonClickedThenDeleteTheListAndReturnToMainPage() {
		when(factories.getModelLayerFactory().newLoggingServices()).thenReturn(mock(LoggingServices.class));
		presenter.deleteButtonClicked();
		verify(factories.getRecordServices(), times(1)).logicallyDelete(record, user);
		verify(factories.getRecordServices(), times(1)).physicallyDelete(record, user);
		verify(navigator, times(1)).decommissioning();
	}

	@Test
	public void givenIsProcessableThenAskTheDecommissioningService() {
		when(service.isProcessable(list, user)).thenReturn(true);
		assertThat(presenter.isProcessable()).isTrue();
		verify(service, times(1)).isProcessable(list, user);
	}

	@Test
	public void givenProcessButtonClickedThenProcessTheListAndRefreshWithMessage() {
		presenter.processButtonClicked();
		verify(service, times(1)).decommission(list, user);
		verify(view, times(1)).showMessage(anyString());
		verify(navigator).displayDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenContainerCreationRequestedThenNavigateToContainerCreation() {
		presenter.containerCreationRequested();
		verify(navigator, times(1)).createContainerForDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenContainerSearchRequestedThenNavigateToContainerSearch() {
		presenter.containerSearchRequested();
		verify(navigator, times(1)).searchContainerForDecommissioningList(ZE_LIST);
	}

	@Test
	public void givenFolderPlacedInContainerThenUpdateDecommissioningListAndView()
			throws RecordServicesException {
		when(folderDetailVO.getFolderId()).thenReturn("zeFolder");
		when(containerVO.getId()).thenReturn("zeContainer");
		when(list.getFolderDetail("zeFolder")).thenReturn(folderDetail);
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
		givenTwoProcessableFoldersAndTwoUnprocessableFolders();
		assertThat(presenter.getPackageableFolders()).containsExactly(packageable1, packageable2);
	}

	@Test
	public void givenProcessableFoldersAskedThenReturnTheProcessableFolders() {
		givenTwoProcessableFoldersAndTwoUnprocessableFolders();
		assertThat(presenter.getProcessableFolders()).containsExactly(processable1, processable2);
	}

	@Test
	public void givenApprovedButtonClickedThenApproveTheListAndRefreshWithMessage() {
		presenter.approvalButtonClicked();
		verify(service, times(1)).approveList(list, user);
		verify(view, times(1)).showMessage(anyString());
		verify(navigator).displayDecommissioningList(ZE_LIST);
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
		when(builder.build(detail1)).thenReturn(processable1);
		when(builder.build(detail2)).thenReturn(packageable1);
		when(builder.build(detail3)).thenReturn(processable2);
		when(builder.build(detail4)).thenReturn(packageable2);

		when(list.getFolderDetailsWithType()).thenReturn(Arrays.asList(detail1, detail2, detail3, detail4));
		doReturn(builder).when(presenter).folderDetailToVOBuilder();
	}
}
