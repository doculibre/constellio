package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.ui.framework.components.OverridingMetadataFieldFactory.OverrideMode;
import com.constellio.model.entities.Language;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AddEditSchemaRecordPresenterAcceptTest extends ConstellioTest {

	public static final String FOLDER_TYPE_LINKED_SCHEMA = FolderType.DEFAULT_SCHEMA + "_" + FolderType.LINKED_SCHEMA;
	public static final String DOCUMENT_TYPE_LINKED_SCHEMA = DocumentType.DEFAULT_SCHEMA + "_" + DocumentType.LINKED_SCHEMA;
	public static final String CONTAINER_TYPE_LINKED_SCHEMA =
			ContainerRecordType.DEFAULT_SCHEMA + "_" + ContainerRecordType.LINKED_SCHEMA;
	public static final String STORAGE_SPACE_LINKED_SCHEMA =
			StorageSpaceType.DEFAULT_SCHEMA + "_" + StorageSpaceType.LINKED_SCHEMA;

	@Mock AddEditSchemaRecordView view;
	AddEditSchemaRecordPresenter presenter;

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));

		presenter = new AddEditSchemaRecordPresenter(view, null);
	}

	@Test
	public void givenFolderTypeThenReturnFolderCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(Folder.SCHEMA_TYPE);
		assertThat(presenter.getOverride(FOLDER_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(FOLDER_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(Folder.SCHEMA_TYPE + "_custom1", Folder.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(Folder.SCHEMA_TYPE + "_custom2", Folder.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenDocumentTypeThenReturnDocumentCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(Document.SCHEMA_TYPE);
		assertThat(presenter.getOverride(DOCUMENT_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(DOCUMENT_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(Document.SCHEMA_TYPE + "_custom1", Document.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(Document.SCHEMA_TYPE + "_custom2", Document.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenContainerRecordTypeThenReturnContainerRecordCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(ContainerRecord.SCHEMA_TYPE);
		assertThat(presenter.getOverride(CONTAINER_TYPE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(CONTAINER_TYPE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(ContainerRecord.SCHEMA_TYPE + "_custom1", ContainerRecord.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(ContainerRecord.SCHEMA_TYPE + "_custom2", ContainerRecord.SCHEMA_TYPE + " custom 2"));
	}

	@Test
	public void givenStorageSpaceTypeThenReturnStorageSpaceCustomSchemas()
			throws Exception {
		givenCustomSchemasFor(StorageSpace.SCHEMA_TYPE);
		assertThat(presenter.getOverride(STORAGE_SPACE_LINKED_SCHEMA))
				.isEqualTo(OverrideMode.DROPDOWN);
		assertThat(presenter.getChoices(STORAGE_SPACE_LINKED_SCHEMA)).extracting("value", "caption")
				.contains(
						Tuple.tuple(StorageSpace.SCHEMA_TYPE + "_custom1", StorageSpace.SCHEMA_TYPE + " custom 1"),
						Tuple.tuple(StorageSpace.SCHEMA_TYPE + "_custom2", StorageSpace.SCHEMA_TYPE + " custom 2"));
	}

	private void givenCustomSchemasFor(String schemaType)
			throws OptimisticLocking {
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = manager.modify(zeCollection);
		types.getSchemaType(schemaType).createCustomSchema("custom1").addLabel(Language.French, schemaType + " custom 1");
		types.getSchemaType(schemaType).createCustomSchema("custom2").addLabel(Language.French, schemaType + " custom 2");
		manager.saveUpdateSchemaTypes(types);
	}
}
