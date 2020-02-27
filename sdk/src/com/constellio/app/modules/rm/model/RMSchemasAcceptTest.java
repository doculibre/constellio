package com.constellio.app.modules.rm.model;

import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.AppLayerConfigurationAlteration;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMSchemasAcceptTest extends ConstellioTest {

	@Test
	public void whenCallLogicallyThenPhysicallyDeletableCheckOnCategoriesThenGoodBehavior() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		assertThat(schemasDisplayManager.getReturnedFieldsForSearch(zeCollection)).containsOnly(
				"archivisticStatus_s", "title_s", "assigneeId_s", "code_s", "content_s", "mediaType_s", "modifiedOn_dt", "dueDate_da",
				"statusId_s", "description_s", "description_t", "mimetype_s", "migrationDataVersion_d", "deleted_s", "question_s",
				"tokensHierarchy_ss", "documentsTokens_ss", "subFoldersTokens_ss"
		);
	}

	@Test
	public void givenASchemaTypeThenRetrieveSchemaTypesInItsHierarchy() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(zeCollection);

		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(Folder.SCHEMA_TYPE, schemaTypes)).containsOnly(
				Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE
		);
		assertThat(SchemaUtils.getSchemaTypesInHierarchyOf(Document.SCHEMA_TYPE, schemaTypes)).containsOnly(
				Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, Category.SCHEMA_TYPE
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

	private String[] EXPECTED_MULTILINGUAL_METADATAS = new String[]{
			"administrativeUnit_default_description", "administrativeUnit_default_title",
			"category_default_description", "category_default_keywords", "category_default_title",
			"ddvContainerRecordType_default_description", "ddvContainerRecordType_default_title",
			"ddvDocumentType_default_description", "ddvDocumentType_default_title",
			"ddvFolderType_default_description", "ddvFolderType_default_title",
			"ddvMediumType_default_description", "ddvMediumType_default_title",
			"ddvStorageSpaceType_default_description", "ddvStorageSpaceType_default_title",
			"ddvTaskStatus_default_description", "ddvTaskStatus_default_title",
			"ddvTaskType_default_description", "ddvTaskType_default_title",
			"ddvVariablePeriod_default_description", "ddvVariablePeriod_default_title",
			"ddvYearType_default_description", "ddvYearType_default_title",
			"facet_default_title",
			"retentionRule_default_title", "retentionRule_default_juridicReference", "retentionRule_default_generalComment",
			"retentionRule_default_keywords", "retentionRule_default_copyRulesComment", "retentionRule_default_description",
			"report_default_title",
			"uniformSubdivision_default_title",
			"printable_default_title",
			"ddvCapsuleLanguage_default_description",
			"ddvCapsuleLanguage_default_title",
			"ddvUserFunction_default_description",
			"ddvUserFunction_default_title"

	};

	@Test
	public void givenFastMigrationDisabledMultilingualCollectionThenSomeMetadatasAreMultilingual() {

		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		String collection = "collection" + anInteger();
		prepareSystem(
				withCollection(collection).withLanguages(asList("fr", "en")).withConstellioRMModule().withAllTestUsers()
		);
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(collection);
		assertThat(schemaTypes.hasMetadata("report_default_title")).isTrue();
		assertThat(getAutoCompleteMetadatasWhichAreNotMultilingual(collection)).isEmpty();
		assertThat(getMultilingualMetadatas(collection)).containsOnly(EXPECTED_MULTILINGUAL_METADATAS);
	}

	@Test
	public void givenFastMigrationEnabledMultilingualCollectionThenSomeMetadatasAreMultilingual() {

		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(true);
			}
		});

		String collection = "collection" + anInteger();
		prepareSystem(
				withCollection(collection).withLanguages(asList("fr", "en")).withConstellioRMModule().withAllTestUsers()
		);
		assertThat(getAutoCompleteMetadatasWhichAreNotMultilingual(collection)).isEmpty();
		assertThat(getMultilingualMetadatas(collection)).containsOnly(EXPECTED_MULTILINGUAL_METADATAS);
	}

	private List<String> getAutoCompleteMetadatasWhichAreNotMultilingual(String collection) {
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(collection);
		List<String> autoCompleteMetadatasWhichAreNotMultilingual = new ArrayList<>();
		for (MetadataSchemaType type : schemaTypes.getSchemaTypes()) {
			for (MetadataSchema schema : type.getAllSchemas()) {

				for (Metadata metadata : schema.getMetadatas().onlyWithoutInheritance()) {
					if (metadata.getLocalCode().equals("autocomplete")) {
						if (!metadata.isMultiLingual()) {
							autoCompleteMetadatasWhichAreNotMultilingual.add(metadata.getLocalCode());
						}
					}
				}

			}
		}
		return autoCompleteMetadatasWhichAreNotMultilingual;
	}

	private List<String> getMultilingualMetadatas(String collection) {
		MetadataSchemasManager schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = schemaManager.getSchemaTypes(collection);
		List<String> multilingualMetadatas = new ArrayList<>();
		for (MetadataSchemaType type : schemaTypes.getSchemaTypes()) {
			for (MetadataSchema schema : type.getAllSchemas()) {

				for (Metadata metadata : schema.getMetadatas().onlyWithoutInheritance()) {
					if (!metadata.getLocalCode().equals("autocomplete")) {
						if (metadata.isMultiLingual()) {
							multilingualMetadatas.add(metadata.getCode());
						}
					}
				}

			}
		}
		Collections.sort(multilingualMetadatas);
		return multilingualMetadatas;
	}

}
