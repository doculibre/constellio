package com.constellio.app.modules.rm.ui.pages.folder;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class AddEditFolderFolderPresenterAcceptTest extends ConstellioTest {

	@Mock AddEditFolderView view;
	@Mock ConstellioNavigator navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AddEditFolderPresenter presenter;
	SessionContext sessionContext;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		recordServices = getModelLayerFactory().newRecordServices();

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = spy(new AddEditFolderPresenter(view));

		doReturn("type1").when(presenter).getTypeFieldValue();
		doNothing().when(presenter).commitForm();
		doNothing().when(presenter).reloadForm();
	}

	@Test
	public void givenNewFolderTypeLinkedToNewFolderSchemaWhenReloadFormWithNewTypeThenDoNotSetValueInAutomaticMetadatas()
			throws Exception {

		givenNewFolderTypeLinkedToNewFolderSchema();

		presenter.forParams("");
		presenter.reloadFormAfterFolderTypeChange();
	}

	private void givenNewFolderTypeLinkedToNewFolderSchema()
			throws Exception {
		givenNewCustomFolderSchema();

		FolderType folderType1 = rmSchemasRecordsServices.newFolderTypeWithId("type1");
		folderType1.setCode("type1");
		folderType1.setDescription("type1");
		folderType1.setTitle("type1");
		folderType1.setLinkedSchema("folder_USRcustom1");
		recordServices.add(folderType1);
	}

	private void givenNewCustomFolderSchema()
			throws OptimisticLocking {
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = metadataSchemasManager.modify(zeCollection);
		typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRcustom1");
		metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
	}
}
