package com.constellio.app.modules.rm.services;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.services.ValueListServices.CreateValueListOptions;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCode;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class ValueListServicesAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager schemasManager;

	SchemasDisplayManager schemasDisplayManager;

	ValueListServices services;

	List<MetadataSchemaType> initialValueLists;

	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	TaxonomiesManager taxonomiesManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		recordServices = getModelLayerFactory().newRecordServices();

		services = new ValueListServices(getAppLayerFactory(), zeCollection);

		initialValueLists = services.getValueDomainTypes();

		schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenCreatingValueDomainsThenCanBeRetreivedInList()
			throws Exception {

		assertThat(initialValueLists).isNotEmpty();

		Map<Language, String> title1 = new HashMap<>();
		title1.put(Language.French, "Domain 1");

		Map<Language, String> title2 = new HashMap<>();
		title2.put(Language.French, "Zé domaine de valeur 2!");

		services.createValueDomain(title1, false);
		services.createValueDomain(title2, false);

		List<MetadataSchemaType> newDomainTypes = new ArrayList<>();
		for (MetadataSchemaType type : services.getValueDomainTypes()) {
			boolean newList = true;
			for (MetadataSchemaType initialValueList : initialValueLists) {
				newList &= !initialValueList.getCode().equals(type.getCode());
			}
			if (newList) {
				newDomainTypes.add(type);
			}
		}
		assertThat(newDomainTypes).hasSize(2);
		assertThat(newDomainTypes.get(0).getLabel(Language.French)).isEqualTo("Domain 1");
		assertThat(newDomainTypes.get(1).getLabel(Language.French)).isEqualTo("Zé domaine de valeur 2!");

		Metadata code = newDomainTypes.get(0).getDefaultSchema().getMetadata(ValueListItem.CODE);
		Metadata description = newDomainTypes.get(0).getDefaultSchema().getMetadata(ValueListItem.DESCRIPTION);

		assertThat(code.isUndeletable()).isTrue();
		//assertThat(code.getUnmodifiable()).isTrue();
		assertThat(code.isSearchable()).isTrue();
		assertThat(code.getType()).isSameAs(MetadataValueType.STRING);

		assertThat(description.isUndeletable()).isTrue();
		assertThat(description.isSearchable()).isTrue();
		assertThat(description.getType()).isSameAs(MetadataValueType.TEXT);

	}

	@Test
	public void whenCreatingATaxonomyThenCreateTypeWithParentRelationAndAddATaxonomy()
			throws Exception {

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		assertThat(initialValueLists).isNotEmpty();

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.English, "My ultimate taxonomy!");

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.English, "Another taxonomy!");

		Taxonomy taxonomy1 = services.createTaxonomy(labelTitle1, true);
		Taxonomy taxonomy2 = services.createTaxonomy(labelTitle2, true);

		taxonomy1 = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, taxonomy1.getCode());
		taxonomy2 = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, taxonomy2.getCode());

		assertThat(taxonomy1.getTitle().get(Language.English)).isEqualTo("My ultimate taxonomy!");
		assertThat(taxonomy1.getSchemaTypes()).containsOnlyOnce(taxonomy1.getCode() + "Type");
		assertThat(taxonomy2.getTitle().get(Language.English)).isEqualTo("Another taxonomy!");
		assertThat(taxonomy2.getSchemaTypes()).containsOnlyOnce(taxonomy2.getCode() + "Type");

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType taxo1Type = types.getSchemaType(taxonomy1.getSchemaTypes().get(0));
		MetadataSchemaType taxo2Type = types.getSchemaType(taxonomy2.getSchemaTypes().get(0));

		assertThat(taxo1Type.getLabel(Language.English)).isEqualTo("My ultimate taxonomy!");
		assertThat(taxo2Type.getLabel(Language.English)).isEqualTo("Another taxonomy!");

		Metadata code = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.CODE);
		Metadata description = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.DESCRIPTION);
		Metadata parent = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.PARENT);

		assertThat(code.isUndeletable()).isTrue();
		//assertThat(code.getUnmodifiable()).isTrue();
		assertThat(code.isSearchable()).isTrue();
		assertThat(code.getType()).isSameAs(MetadataValueType.STRING);

		assertThat(description.isUndeletable()).isTrue();
		assertThat(description.isSearchable()).isTrue();
		assertThat(description.getType()).isSameAs(MetadataValueType.TEXT);

		assertThat(parent.getAllowedReferences().getAllowedSchemaType()).isEqualTo(taxo1Type.getCode());
		assertThat(parent.isChildOfRelationship()).isTrue();

	}

	@Test
	public void whenCreateTaxonomyWithTitleUsersAndGroupsThenItIsCreated()
			throws Exception {
		List<String> userIds = new ArrayList<>();
		userIds.add("chuck");
		userIds.add("bob");

		List<String> groupIds = new ArrayList<>();
		groupIds.add("heroes");
		groupIds.add("legends");
		String title = "Taxo1";
		String abv = "T1";

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "Taxo1");

		Map<Language, String> labelAbv = new HashMap<>();
		labelTitle.put(Language.French, "T1");

		Taxonomy taxonomy1 = services.createTaxonomy(labelTitle, labelAbv, userIds, groupIds, true, true);

		assertThat(taxonomy1.getTitle().get(Language.French)).isEqualTo(title);
		assertThat(taxonomy1.getAbbreviation().get(Language.French)).isEqualTo(abv);
		assertThat(taxonomy1.getUserIds()).isEqualTo(userIds);
		assertThat(taxonomy1.getGroupIds()).isEqualTo(groupIds);
		assertThat(taxonomy1.getSchemaTypes()).containsOnlyOnce(taxonomy1.getCode() + "Type");
	}

	@Test
	public void givenClassifiedObjectHasCustomSchemasWhenCreatingClassificationMetadataThenVisibleInAllOfThem()
			throws Exception {
		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("custom1");
		types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("custom2");
		schemasManager.saveUpdateSchemaTypes(types);

		String metadataCode = createMetadataAndValidate();
		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadataCode);

		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA);
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).contains("folder_default_" + metadataLocalCode);
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("folder_default_" + metadataLocalCode);

		schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, "folder_custom1");
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).contains("folder_custom1_" + metadataLocalCode);
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("folder_custom1_" + metadataLocalCode);

		schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, "folder_custom2");
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).contains("folder_custom2_" + metadataLocalCode);
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains("folder_custom2_" + metadataLocalCode);
	}

	@Test
	public void givenGroupNotExistingWhenCreateAMultivalueClassificationMetadataInGroupThenMtadataAndGroupCreatedCorrectly() {
		createMetadataAndValidate();
	}

	@Test
	public void givenTaxonomyWithMetadataWhenDeletedThenTypeTaxonomyAndMetadataAreDeleted()
			throws ValidationException {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "Ze ultimate taxo!");

		Map<Language, String> labelAbv = new HashMap<>();
		labelTitle.put(Language.French, "ultimate");

		Taxonomy zeTaxo = services.createTaxonomy("zeTaxo", labelTitle, labelAbv, new ArrayList<>(), new ArrayList<>(),
				true, true);

		Metadata referenceMetadata = services
				.createAMultivalueClassificationMetadataInGroup(zeTaxo, Folder.SCHEMA_TYPE, "ZeMagicGroup", "Ze Magic Group");

		assertThat(taxonomiesManager.getEnabledTaxonomies(zeCollection)).extracting("code").contains("taxozeTaxo");
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("taxozeTaxoType")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata(referenceMetadata.getCode())).isTrue();

		services.deleteValueListOrTaxonomy("taxozeTaxoType");

		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("taxozeTaxoType")).isFalse();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata(referenceMetadata.getCode())).isFalse();
		assertThat(taxonomiesManager.getEnabledTaxonomies(zeCollection)).extracting("code").doesNotContain("taxozeTaxo");

	}

	@Test
	public void givenGroupExistingWhenCreateAMultivalueClassificationMetadataInGroupThenMetadataCreatedCorrectlyInGroup() {

		Map<String, Map<Language, String>> groups = new HashMap<>();
		Map<Language, String> labels = new HashMap<>();
		labels.put(Language.French, "Ze magic group");
		groups.put("ZeMagicGroup", labels);
		schemasDisplayManager.saveType(schemasDisplayManager.getType(zeCollection, Folder.SCHEMA_TYPE)
				.withNewMetadataGroup(groups));

		createMetadataAndValidate();
	}

	private String createMetadataAndValidate() {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "Ze ultimate taxo!");

		Taxonomy zeTaxo = services.createTaxonomy(labelTitle, new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
				true, true);

		services.createAMultivalueClassificationMetadataInGroup(zeTaxo, Folder.SCHEMA_TYPE, "ZeMagicGroup", "Ze Magic Group");

		String metadataCode = "folder_default_" + zeTaxo.getCode() + "Ref";
		Metadata metadata = schemasManager.getSchemaTypes(zeCollection).getMetadata(metadataCode);
		assertThat(metadata.getLabel(Language.French)).isEqualTo("Ze ultimate taxo!");
		assertThat(metadata.isTaxonomyRelationship()).isTrue();
		assertThat(metadata.isMultivalue()).isTrue();
		assertThat(metadata.getAllowedReferences().getAllowedSchemaType()).isEqualTo(zeTaxo.getSchemaTypes().get(0));

		MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(zeCollection, metadata.getCode());
		assertThat(metadataDisplayConfig.getMetadataCode()).isEqualTo(metadataCode);
		assertThat(metadataDisplayConfig.getInputType()).isEqualTo(MetadataInputType.LOOKUP);

		//FIXME
		SchemaTypeDisplayConfig typeDisplayConfig = schemasDisplayManager.getType(zeCollection, Folder.SCHEMA_TYPE);
		assertThat(typeDisplayConfig.getMetadataGroup().keySet()).containsOnlyOnce("ZeMagicGroup");

		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, Folder.DEFAULT_SCHEMA);
		assertThat(schemaDisplayConfig.getFormMetadataCodes()).contains(metadataCode);
		assertThat(schemaDisplayConfig.getDisplayMetadataCodes()).contains(metadataCode);

		List<String> typesWithTaxo = new SchemaUtils().toSchemaTypeCodes(services.getClassifiedSchemaTypes(zeTaxo));
		assertThat(typesWithTaxo).containsOnly(Folder.SCHEMA_TYPE);

		return metadataCode;
	}

	@Test
	public void whenCreatingAMetadataWithoutUsingTheServiceThenTypeIsConsideredAClassificedSchemaType()
			throws Exception {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "Ze ultimate taxo!");

		Taxonomy zeTaxo = services.createTaxonomy(labelTitle, new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
				true, true);

		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder zeTaxoSchemaType = types.getSchemaType(zeTaxo.getSchemaTypes().get(0));
		types.getSchema(Document.DEFAULT_SCHEMA).create("zeRef").defineTaxonomyRelationshipToType(zeTaxoSchemaType);
		schemasManager.saveUpdateSchemaTypes(types);

		List<String> typesWithTaxo = new SchemaUtils().toSchemaTypeCodes(services.getClassifiedSchemaTypes(zeTaxo));
		assertThat(typesWithTaxo).containsOnly(Document.SCHEMA_TYPE);
	}

	@Test
	public void givenAValueListWithoutRecordsWhenDeletingItThenSchemaTypeAndMetadatasUsingItAreRemoved()
			throws Exception {

		CreateValueListOptions options = new CreateValueListOptions();
		options.setCreateMetadatasAsMultivalued(true);
		options.typesWithReferenceMetadata = asList("administrativeUnit");

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "zora");

		MetadataSchemaType zoraDomain = services.createValueDomain("ddvUSRZora", labelTitle, options);

		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("ddvUSRZora")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata("administrativeUnit_default_USRZora")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).getMetadata("administrativeUnit_default_USRZora").isMultivalue())
				.isTrue();
		services.deleteValueListOrTaxonomy("ddvUSRZora");

		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("ddvUSRZora")).isFalse();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata("administrativeUnit_default_USRZora")).isFalse();

	}

	@Test
	public void givenAValueListWithRecordsWhenDeletingItThenSchemaTypeAndMetadatasUsingItAreRemoved()
			throws Exception {

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "zora");

		CreateValueListOptions options = new CreateValueListOptions();
		options.setCreateMetadatasAsMultivalued(false);
		options.typesWithReferenceMetadata = asList("administrativeUnit");

		MetadataSchemaType zoraDomain = services.createValueDomain("ddvUSRZora", labelTitle, options);

		recordServices
				.add(recordServices.newRecordWithSchema(zoraDomain.getDefaultSchema()).set(TITLE, "test")
						.set(Schemas.CODE, "test"));

		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("ddvUSRZora")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata("administrativeUnit_default_USRZora")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).getMetadata("administrativeUnit_default_USRZora").isMultivalue())
				.isFalse();

		try {
			services.deleteValueListOrTaxonomy("ddvUSRZora");
			fail("Exception expected");

		} catch (ValidationException e) {
			assertThat(extractingSimpleCode(e.getValidationErrors())).containsOnly("ValueListServices_valueListHasRecords");
			assertThat(frenchMessages(e.getValidationErrors()))
					.containsOnly("Le domaine de valeurs «zora» ne peut pas être supprimé, car il n'est pas vide");
		}

		assertThat(schemasManager.getSchemaTypes(zeCollection).hasType("ddvUSRZora")).isTrue();
		assertThat(schemasManager.getSchemaTypes(zeCollection).hasMetadata("administrativeUnit_default_USRZora")).isTrue();

	}

}
