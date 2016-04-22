package com.constellio.app.modules.rm.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
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
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class ValueListServicesAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager schemasManager;

	SchemasDisplayManager schemasDisplayManager;

	ValueListServices services;

	List<MetadataSchemaType> initialValueLists;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		services = new ValueListServices(getAppLayerFactory(), zeCollection);

		initialValueLists = services.getValueDomainTypes();

		schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
	}

	@Test
	public void whenCreatingValueDomainsThenCanBeRetreivedInList()
			throws Exception {

		assertThat(initialValueLists).isNotEmpty();

		services.createValueDomain("Domain 1");
		services.createValueDomain("Zé domaine de valeur 2!");

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
		//assertThat(code.isUnmodifiable()).isTrue();
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

		Taxonomy taxonomy1 = services.createTaxonomy("My ultimate taxonomy!");
		Taxonomy taxonomy2 = services.createTaxonomy("Another taxonomy!");

		taxonomy1 = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, taxonomy1.getCode());
		taxonomy2 = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, taxonomy2.getCode());

		assertThat(taxonomy1.getTitle()).isEqualTo("My ultimate taxonomy!");
		assertThat(taxonomy1.getSchemaTypes()).containsOnlyOnce(taxonomy1.getCode() + "Type");
		assertThat(taxonomy2.getTitle()).isEqualTo("Another taxonomy!");
		assertThat(taxonomy2.getSchemaTypes()).containsOnlyOnce(taxonomy2.getCode() + "Type");

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaType taxo1Type = types.getSchemaType(taxonomy1.getSchemaTypes().get(0));
		MetadataSchemaType taxo2Type = types.getSchemaType(taxonomy2.getSchemaTypes().get(0));

		assertThat(taxo1Type.getLabel(Language.French)).isEqualTo("My ultimate taxonomy!");
		assertThat(taxo2Type.getLabel(Language.French)).isEqualTo("Another taxonomy!");

		Metadata code = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.CODE);
		Metadata description = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.DESCRIPTION);
		Metadata parent = taxo1Type.getDefaultSchema().getMetadata(HierarchicalValueListItem.PARENT);

		assertThat(code.isUndeletable()).isTrue();
		//assertThat(code.isUnmodifiable()).isTrue();
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

		Taxonomy taxonomy1 = services.createTaxonomy(title, userIds, groupIds, true);

		assertThat(taxonomy1.getTitle()).isEqualTo(title);
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

		Taxonomy zeTaxo = services.createTaxonomy("Ze ultimate taxo!", new ArrayList<String>(), new ArrayList<String>(), true);

		services.createAMultivalueClassificationMetadataInGroup(zeTaxo, Folder.SCHEMA_TYPE, "ZeMagicGroup");

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
		//TODO Thiago
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

		Taxonomy zeTaxo = services.createTaxonomy("Ze ultimate taxo!", new ArrayList<String>(), new ArrayList<String>(), true);

		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		MetadataSchemaTypeBuilder zeTaxoSchemaType = types.getSchemaType(zeTaxo.getSchemaTypes().get(0));
		types.getSchema(Document.DEFAULT_SCHEMA).create("zeRef").defineTaxonomyRelationshipToType(zeTaxoSchemaType);
		schemasManager.saveUpdateSchemaTypes(types);

		List<String> typesWithTaxo = new SchemaUtils().toSchemaTypeCodes(services.getClassifiedSchemaTypes(zeTaxo));
		assertThat(typesWithTaxo).containsOnly(Document.SCHEMA_TYPE);
	}

}
