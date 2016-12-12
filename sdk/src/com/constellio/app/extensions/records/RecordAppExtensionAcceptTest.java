package com.constellio.app.extensions.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Locale;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchPresenter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.folder.AddEditFolderView;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.mockito.Mockito;
import org.mockito.Spy;

/**
 * Created by Patrick on 2015-11-19.
 */
public class RecordAppExtensionAcceptTest extends ConstellioTest {

	RecordToVOBuilder voBuilder = new RecordToVOBuilder();
	@Mock AddEditFolderView view;
	@Mock CoreViews navigator;
	@Mock RecordAppExtension recordAppExtension1;
	@Mock RecordAppExtension recordAppExtension2;
	@Mock DisplayFolderView displayFolderView;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SessionContext sessionContext;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;
	ConnectorInstance connectorSmbInstance;
	ConnectorManager connectorManager;
	ESSchemasRecordsServices es;
	TasksSchemasRecordsServices tasksSchemas;
	ConnectorSmbDocument connectorSmbDocument;
	ConnectorHttpInstance connectorHttpInstance;
	ConnectorHttpDocument connectorHttpDocument;
	Task zeTask;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioESModule().withTasksModule().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		recordServices = getModelLayerFactory().newRecordServices();
		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		zeTask = tasksSchemas.newTask().setTitle("zeTask");
		recordServices.add(zeTask.getWrappedRecord());

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		connectorSmbInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setTitle("zeConnectorSMB")
				.setCode("zeConnectorSMB")
				.setEnabled(false)
				.setSeeds(asList("share")).setUsername("username").setPassword("password").setDomain("domain")
				.setTraversalCode("zeTraversal"));
		connectorSmbDocument = es.newConnectorSmbDocument(connectorSmbInstance);
		connectorHttpInstance = connectorManager.createConnector(es.newConnectorHttpInstance()
				.setTitle("zeConnectorHTTP")
				.setCode("zeConnectorHTTP")
				.setEnabled(false)
				.setSeeds("share").setUsername("username").setPassword("password").setDomain("domain")
				.setTraversalCode("zeTraversal"));
		connectorHttpDocument = es.newConnectorHttpDocument(connectorHttpInstance);
	}

	@Test
	public void givenActiveFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		Folder folder = records.getFolder_A01().setDescription("niceTitle");
		recordServices.update(folder);
		RecordVO recordVO = voBuilder.build(records.getFolder_A01().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A01().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder/folder.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("folder");
		assertThat(recordVO.getNiceTitle()).isEqualTo("niceTitle");
	}

	@Test
	public void givenSemiActiveFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_C30().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_C30().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder/folder_orange.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("folder_orange");
	}

	@Test
	public void givenDepositedFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_A79().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A79().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder/folder_purple.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("folder_purple");
	}

	@Test
	public void givenDestroyedFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_A80().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A80().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder/folder_grey.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("folder_grey");
	}

	@Test
	public void givenContainerToRecordVOWhenGetIconForRecordAndExtensionThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getContainerBac01().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getContainerBac01().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/container/box.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isNull();
	}

	@Test
	public void givenConnectorHttpInstanceToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(connectorSmbDocument.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(connectorSmbDocument.getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/connectors/connectorSmbDocument.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isNull();
	}

	@Test
	public void givenConnectorSmbInstanceToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(connectorHttpDocument.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(connectorHttpDocument.getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/connectors/connectorHttpDocument.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isNull();
	}

	@Test
	public void givenTaskToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(zeTask.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(zeTask.getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/task/task.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("task");
	}

	@Test
	public void givenAFolderIsDeletedThenIsNotified() {
		MockedNavigation navigator = new MockedNavigation();

		UIContext uiContext = mock(UIContext.class);
		when(uiContext.getAttribute(FolderDocumentBreadcrumbTrail.TAXONOMY_CODE)).thenReturn(null);
		when(displayFolderView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayFolderView.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		when(displayFolderView.getUIContext()).thenReturn(uiContext);
		when(displayFolderView.getCollection()).thenReturn(zeCollection);
		when(displayFolderView.navigate()).thenReturn(navigator);
		Folder folder = rmSchemasRecordsServices.searchFolders(where(rmSchemasRecordsServices.folder.title()).isContainingText("Avocat")).get(0);

		getAppLayerFactory().getExtensions().forCollection(zeCollection).recordAppExtensions.add(recordAppExtension1);
		getAppLayerFactory().getExtensions().forCollection(zeCollection).recordAppExtensions.add(recordAppExtension2);

		DisplayFolderPresenter displayFolderPresenter = new DisplayFolderPresenter(displayFolderView);
		displayFolderPresenter.forParams(folder.getId());

		displayFolderPresenter.deleteFolderButtonClicked("No reason");
		verify(recordAppExtension1, times(1)).notifyFolderDeleted(any(FolderDeletionEvent.class));
		verify(recordAppExtension2, times(1)).notifyFolderDeleted(any(FolderDeletionEvent.class));
	}
}
