package com.constellio.app.extensions.records;

import com.constellio.app.api.extensions.taxonomies.FolderDeletionEvent;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.FolderMenuItemActionBehaviors;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Locale;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Patrick on 2015-11-19.
 */
public class RecordAppExtensionAcceptTest extends ConstellioTest {

	RecordToVOBuilder voBuilder = new RecordToVOBuilder();
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
	Users users = new Users();
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

		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
	}

	@Test
	public void givenDescriptionCachedInSummaryThenAvailableInNiceTitles()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get(Folder.DESCRIPTION).setEssentialInSummary(true);
			}
		});

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList(Folder.SCHEMA_TYPE));

		Folder folder = records.getFolder_A01().setDescription("niceTitle");
		recordServices.update(folder);
		RecordVO recordVO = voBuilder.build(records.getFolder_A01().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A01().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder2/yellow_hybrid_folder_closed.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("yellow_hybrid_folder_closed");
		assertThat(recordVO.getNiceTitle()).isEqualTo("niceTitle");
	}

	@Test
	public void givenActiveFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		Folder folder = records.getFolder_A01().setDescription("niceTitle");
		recordServices.update(folder);
		RecordVO recordVO = voBuilder.build(records.getFolder_A01().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A01().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder2/yellow_hybrid_folder_closed.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getNiceTitle()).isNull();
	}

	@Test
	public void givenSemiActiveFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_C30().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_C30().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder2/orange_hybrid_folder_closed.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("orange_hybrid_folder_closed");
	}

	@Test
	public void givenDepositedFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_A79().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A79().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder2/purple_hybrid_folder_closed.png")
				.isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("purple_hybrid_folder_closed");
	}

	@Test
	public void givenDestroyedFolderToRecordVOWhenGetIconAndExtensionForRecordThenReturnGoodIcon()
			throws Exception {

		RecordVO recordVO = voBuilder.build(records.getFolder_A80().getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		String iconPath = getAppLayerFactory().getExtensions().forCollection(zeCollection)
				.getIconForRecord(new GetIconPathParams(records.getFolder_A80().getWrappedRecord(), false));

		assertThat(recordVO.getResourceKey()).isEqualTo("images/icons/folder2/grey_empty_folder_closed.png").isEqualTo(iconPath);
		assertThat(recordVO.getExtension()).isEqualTo("grey_empty_folder_closed");
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

	public static class FolderMenuItemActionBehaviorsOverrite extends FolderMenuItemActionBehaviors {

		public FolderMenuItemActionBehaviorsOverrite(String collection,
													 AppLayerFactory appLayerFactory) {
			super(collection, appLayerFactory);
		}

		@Override
		public void deleteFolder(Folder folder, String reason, MenuItemActionBehaviorParams params) {
			super.deleteFolder(folder, reason, params);
		}
	}

	@Test
	public void givenAFolderIsDeletedThenIsNotified() {
		MockedNavigation navigator = new MockedNavigation();

		when(displayFolderView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(displayFolderView.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		when(displayFolderView.getCollection()).thenReturn(zeCollection);
		when(displayFolderView.navigate()).thenReturn(navigator);
		Folder folder = rmSchemasRecordsServices.searchFolders(where(rmSchemasRecordsServices.folder.title()).isContainingText("Avocat")).get(0);

		getAppLayerFactory().getExtensions().forCollection(zeCollection).recordAppExtensions.add(recordAppExtension1);
		getAppLayerFactory().getExtensions().forCollection(zeCollection).recordAppExtensions.add(recordAppExtension2);

		FolderMenuItemActionBehaviorsOverrite folderMenuItemActionBehaviors = new FolderMenuItemActionBehaviorsOverrite(zeCollection, getAppLayerFactory());

		folderMenuItemActionBehaviors.deleteFolder(folder, "reason", new MenuItemActionBehaviorParams() {
			@Override
			public BaseView getView() {
				return displayFolderView;
			}

			@Override
			public Map<String, String> getFormParams() {
				return null;
			}

			@Override
			public User getUser() {
				return users.gandalfIn(zeCollection);
			}
		});

		verify(recordAppExtension1, times(1)).notifyFolderDeleted(Matchers.any(FolderDeletionEvent.class));
		verify(recordAppExtension2, times(1)).notifyFolderDeleted(Matchers.any(FolderDeletionEvent.class));
	}
}
