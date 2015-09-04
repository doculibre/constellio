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
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
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

		givenNewFolderTypeLinkedToNewFolderScchema();

		presenter.forParams("");
		presenter.reloadFormAfterFolderTypeChange();
	}

	private void givenNewFolderTypeLinkedToNewFolderScchema()
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
			throws OptimistickLocking {
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = metadataSchemasManager.modify(zeCollection);
		typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRcustom1");
		metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
	}
}
