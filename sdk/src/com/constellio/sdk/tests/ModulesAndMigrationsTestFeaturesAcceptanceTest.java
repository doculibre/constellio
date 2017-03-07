package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class ModulesAndMigrationsTestFeaturesAcceptanceTest extends ConstellioTest {
	ConstellioPluginManager pluginManager;

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void givenKeyI18nWhenGetLabelInI18nThenOk()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();

		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaTypes().size()).isNotZero();
	}

	@Test
	public void testToCurrentVersion()
			throws Exception {

		prepareSystem(
				withZeCollection()
		);
		pluginManager = getAppLayerFactory().getPluginManager();

		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaTypes().size()).isNotZero();
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType("user").getCode()).isEqualTo("user");
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType(UserDocument.SCHEMA_TYPE)).isNotNull();
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType("group").getCode()).isEqualTo("group");
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType("collection").getCode()).isEqualTo("collection");
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType("task").getCode()).isEqualTo("task");
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaType("event").getCode()).isEqualTo("event");

	}

	@Test
	public void whenGetFoldersFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		int i = 0;
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.TYPE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.PARENT_FOLDER);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.UNIFORM_SUBDIVISION_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.RETENTION_RULE_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.MAIN_COPY_RULE_ID_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.ENTERED_CLOSING_DATE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.MEDIUM_TYPES);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.KEYWORDS);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.DESCRIPTION);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.CONTAINER);

		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.ACTUAL_TRANSFER_DATE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.ACTUAL_DEPOSIT_DATE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Folder.DEFAULT_SCHEMA + "_" + Folder.ACTUAL_DESTRUCTION_DATE);

		assertThereIsNotSystemReservedIn(metadataCodes);

	}

	@Test
	public void whenGetDocumentsFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, Document.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();

		int i = 0;
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER);
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Document.TYPE);
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Document.MAIN_COPY_RULE_ID_ENTERED);
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Document.KEYWORDS);
		assertThat(metadataCodes.get(i++)).isEqualTo(Document.DEFAULT_SCHEMA + "_" + Document.CONTENT);

		assertThereIsNotSystemReservedIn(metadataCodes);
	}

	@Test
	public void whenGetAdministrativeUnitsFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, AdministrativeUnit.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		assertThereIsNotSystemReservedIn(metadataCodes);
	}

	@Test
	public void whenGetCategorysFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, Category.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		assertThat(metadataCodes.get(0)).isEqualTo(Category.DEFAULT_SCHEMA + "_" + Category.CODE);
		assertThat(metadataCodes.get(1)).isEqualTo(Category.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(2)).isEqualTo(Category.DEFAULT_SCHEMA + "_" + Category.DESCRIPTION);
		assertThat(metadataCodes.get(3)).isEqualTo(Category.DEFAULT_SCHEMA + "_" + Category.KEYWORDS);
		assertThat(metadataCodes.get(4)).isEqualTo(Category.DEFAULT_SCHEMA + "_" + Category.PARENT);

		assertThereIsNotSystemReservedIn(metadataCodes);
	}

	@Test
	public void whenGetDecommissioningListsFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, DecommissioningList.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		int i = 0;
		assertThat(metadataCodes.get(i++)).isEqualTo(
				DecommissioningList.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE);
		assertThat(metadataCodes.get(i++))
				.isEqualTo(DecommissioningList.DEFAULT_SCHEMA + "_" + DecommissioningList.DESCRIPTION);

		assertThereIsNotSystemReservedIn(metadataCodes);
	}

	@Test
	public void whenGetFilingSpacesFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, FilingSpace.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		assertThat(metadataCodes.get(0)).isEqualTo(FilingSpace.DEFAULT_SCHEMA + "_" + FilingSpace.CODE);
		assertThat(metadataCodes.get(1)).isEqualTo(FilingSpace.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(2)).isEqualTo(FilingSpace.DEFAULT_SCHEMA + "_" + FilingSpace.ADMINISTRATORS);
		assertThat(metadataCodes.get(3)).isEqualTo(FilingSpace.DEFAULT_SCHEMA + "_" + FilingSpace.USERS);

		assertThereIsNotSystemReservedIn(metadataCodes);

	}

	private void assertThereIsNotSystemReservedIn(List<String> metadataCodes) {

		MetadataSchemaTypes schemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		List<String> metadataCodesSystemReserved = new ArrayList<>();
		for (String metadataCode : metadataCodes) {
			if (schemaTypes.getMetadata(metadataCode).isSystemReserved()) {
				metadataCodesSystemReserved.add(metadataCode);
			}
		}
		assertThat(metadataCodesSystemReserved.isEmpty());
	}

	@Test
	public void whenGetStorageSpacesFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, StorageSpace.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		assertThat(metadataCodes.get(0)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.TYPE);
		assertThat(metadataCodes.get(1)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.CODE);
		assertThat(metadataCodes.get(2)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(3)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.DESCRIPTION);
		assertThat(metadataCodes.get(4)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.CAPACITY);
		assertThat(metadataCodes.get(5)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.DECOMMISSIONING_TYPE);
		assertThat(metadataCodes.get(6)).isEqualTo(StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.PARENT_STORAGE_SPACE);
	}

	@Test
	public void whenGetUniformSubdivionsFormMetadataCodesThenItIsInOrder()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> metadataCodes = schemasDisplayManager.getSchema(zeCollection, UniformSubdivision.DEFAULT_SCHEMA)
				.getFormMetadataCodes();

		assertThat(metadataCodes.size()).isNotZero();
		assertThat(metadataCodes.get(0)).isEqualTo(UniformSubdivision.DEFAULT_SCHEMA + "_" + UniformSubdivision.CODE);
		assertThat(metadataCodes.get(1)).isEqualTo(UniformSubdivision.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
		assertThat(metadataCodes.get(2)).isEqualTo(UniformSubdivision.DEFAULT_SCHEMA + "_" + UniformSubdivision.RETENTION_RULE);
		assertThat(metadataCodes.get(3)).isEqualTo(UniformSubdivision.DEFAULT_SCHEMA + "_" + UniformSubdivision.DESCRIPTION);
	}

	@Test
	public void testWithRMModule()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		assertThat(manager.getSchemaTypes("zeCollection").getSchemaTypes()).extracting("code").containsOnly("document",
				"ddvTaskType", "cart", "ddvStorageSpaceType", "ddvContainerRecordType", "savedSearch", "userDocument",
				"ddvVariablePeriod", "storageSpace", "decommissioningList", "emailToSend", "event", "group",
				"workflowInstance", "ddvMediumType", "filingSpace", "workflow", "ddvFolderType", "collection",
				"userTask", "uniformSubdivision", "authorizationDetails", "administrativeUnit", "ddvDocumentType",
				"folder", "task", "ddvTaskStatus", "containerRecord", "report", "category", "facet", "retentionRule",
				"user", "printable");
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		Taxonomy administrativeUnitsTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode("zeCollection",
				RMTaxonomies.ADMINISTRATIVE_UNITS);
		Taxonomy classificationPlanTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode("zeCollection",
				RMTaxonomies.CLASSIFICATION_PLAN);
		Taxonomy storagesAndContainersTaxonomy = taxonomiesManager.getEnabledTaxonomyWithCode("zeCollection",
				RMTaxonomies.STORAGES);
		assertThat(administrativeUnitsTaxonomy).isNotNull();
		assertThat(administrativeUnitsTaxonomy.getSchemaTypes()).containsOnly(AdministrativeUnit.SCHEMA_TYPE);
		assertThat(classificationPlanTaxonomy).isNotNull();
		assertThat(classificationPlanTaxonomy.getSchemaTypes()).containsOnly(Category.SCHEMA_TYPE);
		assertThat(storagesAndContainersTaxonomy).isNotNull();
		assertThat(storagesAndContainersTaxonomy.getSchemaTypes())
				.containsOnly(StorageSpace.SCHEMA_TYPE);

		RolesManager rolesManager = getModelLayerFactory().getRolesManager();
		assertThat(rolesManager.getRole("zeCollection", RMRoles.USER)).isNotNull();
		assertThat(rolesManager.getRole("zeCollection", RMRoles.MANAGER)).isNotNull();
		assertThat(rolesManager.getRole("zeCollection", RMRoles.RGD)).isNotNull();

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		assertThat(schemaTypesOf(schemasDisplayManager.getSimpleSearchSchemaTypeConfigs(zeCollection)))
				.contains(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)
				.doesNotContain(User.SCHEMA_TYPE, Group.SCHEMA_TYPE);

		assertThat(schemaTypesOf(schemasDisplayManager.getAdvancedSearchSchemaTypeConfigs(zeCollection)))
				.contains(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)
				.doesNotContain(User.SCHEMA_TYPE, Group.SCHEMA_TYPE);

		assertThat(metadataCodesOf(schemasDisplayManager.getAdvancedSearchMetadatas(zeCollection, Document.SCHEMA_TYPE)))
				.contains(Document.FOLDER, Schemas.TITLE.getLocalCode(), Document.KEYWORDS)
				.doesNotContain(Document.CONTENT, Schemas.ALL_AUTHORIZATIONS.getLocalCode());

	}

	// ---------------------------

	private List<String> schemaTypesOf(List<SchemaTypeDisplayConfig> configs) {
		List<String> codes = new ArrayList<>();
		for (SchemaTypeDisplayConfig type : configs) {
			codes.add(type.getSchemaType());
		}
		return codes;
	}

	private List<String> metadataCodesOf(List<MetadataDisplayConfig> configs) {
		List<String> codes = new ArrayList<>();
		for (MetadataDisplayConfig metadata : configs) {
			codes.add(new SchemaUtils().toLocalMetadataCode(metadata.getMetadataCode()));
		}
		return codes;
	}
}
