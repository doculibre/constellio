package com.constellio.app.modules.rm.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class RMSchemasAcceptTest extends ConstellioTest {

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
	}

	@Test
	public void whenCallLogicallyThenPhysicallyDeletableCheckOnCategoriesThenGoodBehavior() {

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		assertThat(schemasDisplayManager.getReturnedFieldsForSearch(zeCollection)).containsOnly(
				"archivisticStatus_s", "title_s", "assigneeId_s", "code_s", "content_s", "modifiedOn_dt", "dueDate_da",
				"statusId_s", "description_s", "description_t", "mimetype_s", "migrationDataVersion_d", "deleted_s", "question_s"
		);
	}

	@Test
	public void givenASchemaTypeThenRetrieveSchemaTypesInItsHierarchy() {
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(zeCollection);

		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(Folder.SCHEMA_TYPE, schemaTypes)).containsOnly(
				Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE
		);
		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(Document.SCHEMA_TYPE, schemaTypes)).containsOnly(
				Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE
		);
		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(ContainerRecord.SCHEMA_TYPE, schemaTypes)).containsOnly(
				ContainerRecord.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE
		);
		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(AdministrativeUnit.SCHEMA_TYPE, schemaTypes)).containsOnly(
				AdministrativeUnit.SCHEMA_TYPE
		);

		schemaManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).create("myNewTaxonomy").setType(MetadataValueType.REFERENCE)
						.setTaxonomyRelationship(true).defineReferencesTo(types.getSchemaType(ContainerRecord.SCHEMA_TYPE));
			}
		});
		MetadataSchemaTypes newSchemaTypes = schemaManager.getSchemaTypes(zeCollection);
		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(Folder.SCHEMA_TYPE, newSchemaTypes)).containsOnly(
				Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE,
				StorageSpace.SCHEMA_TYPE
		);
	}

	@Test
	public void givenASchemaTypeThenAccurateunr() {
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(zeCollection);

		System.out.println(schemaTypes.getSchemaTypesSortedByDependency());
		//		for (MetadataSchemaType schemaType : schemaTypes.getSchemaTypes()) {
		//			for (Metadata metadata : schemaType.getDefaultSchema().getMetadatas().onlyWithType(MetadataValueType.REFERENCE)) {
		//				System.out.println(metadata.getCode() + " : " + metadata.isDependencyOfAutomaticMetadata());
		//			}
		//		}
	}

}
