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
package com.constellio.app.ui.pages.management.schemaRecords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class AddEditSchemaRecordPresenterAcceptTest extends ConstellioTest {
	@Mock AddEditSchemaRecordView view;
	AddEditSchemaRecordPresenter presenter;

	RMTestRecords records;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));

		presenter = new AddEditSchemaRecordPresenter(view);
	}

	@Test
	public void givenFolderTypeThenReturnFolderCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOverride(AddEditSchemaRecordPresenter.FOLDER_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(AddEditSchemaRecordPresenter.FOLDER_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(Folder.SCHEMA_TYPE + "_custom1", Folder.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(Folder.SCHEMA_TYPE + "_custom2", Folder.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenDocumentTypeThenReturnDocumentCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(Document.SCHEMA_TYPE);
		assertThat(presenter.getOverride(AddEditSchemaRecordPresenter.DOCUMENT_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(AddEditSchemaRecordPresenter.DOCUMENT_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(Document.SCHEMA_TYPE + "_custom1", Document.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(Document.SCHEMA_TYPE + "_custom2", Document.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenContainerRecordTypeThenReturnContainerRecordCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(ContainerRecord.SCHEMA_TYPE);
		assertThat(presenter.getOverride(AddEditSchemaRecordPresenter.CONTAINER_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(AddEditSchemaRecordPresenter.CONTAINER_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(ContainerRecord.SCHEMA_TYPE + "_custom1", ContainerRecord.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(ContainerRecord.SCHEMA_TYPE + "_custom2", ContainerRecord.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenStorageSpaceTypeThenReturnStorageSpaceCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(StorageSpace.SCHEMA_TYPE);
		assertThat(presenter.getOverride(AddEditSchemaRecordPresenter.STORAGE_SPACE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(AddEditSchemaRecordPresenter.STORAGE_SPACE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(StorageSpace.SCHEMA_TYPE + "_custom1", StorageSpace.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(StorageSpace.SCHEMA_TYPE + "_custom2", StorageSpace.SCHEMA_TYPE + " custom 2"));
	}

	private void givenCustomSchemasFor(String schemaType)
			throws OptimistickLocking {
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = manager.modify(zeCollection);
		types.getSchemaType(schemaType).createCustomSchema("custom1").setLabel(schemaType + " custom 1");
		types.getSchemaType(schemaType).createCustomSchema("custom2").setLabel(schemaType + " custom 2");
		manager.saveUpdateSchemaTypes(types);
	}
}
