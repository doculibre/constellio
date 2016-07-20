package com.constellio.app.services.importExport.settings;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;

@UiTest
public class SettingsImportServicesAcceptanceTest extends SettingsImportServicesTestUtils {

	public static final String CODE_METADATA_3 = "metadata3";
	public static final String TITLE_METADATA_3 = "Titre métadonnée no.3";
	Users users = new Users();

	SystemConfigurationsManager systemConfigurationsManager;
	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager schemasDisplayManager;
	boolean runTwice;

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet()
			throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_configurationNotFound", "calculatedCloseDateUnknown"));
	}

	@Test
	public void whenImportBadBooleanConfigValueThenValidationExceptionThrown()
			throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate", "notABoolean"));
	}

	@Test
	public void whenImportingBadIntegerConfigValueThenValidationExceptionThrown()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule")
				.setValue("helloInteger"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue",
						"calculatedCloseDateNumberOfYearWhenFixedRule", "helloInteger"));
	}

	@Test
	public void whenImportingNullValueConfigsThenNullValueExceptionIsRaised()
			throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue(null));

		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate"));
	}

	@Test
	public void whenImportConfigsThenSetted()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		settings.addConfig(new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015"));
		settings.addConfig(new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15"));

		settings.addConfig(new ImportedConfig().setKey("yearEndDate").setValue("02/28"));

		settings.addConfig(new ImportedConfig().setKey("decommissioningDateBasedOn").setValue("OPEN_DATE"));

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER))
				.isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE))
				.isEqualTo(2015);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR))
				.isEqualTo(15);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.YEAR_END_DATE)).isEqualTo("02/28");

		assertThat(systemConfigurationsManager.getValue(RMConfigs.DECOMMISSIONING_DATE_BASED_ON))
				.isEqualTo(DecommissioningDateBasedOn.OPEN_DATE);
	}

	@Test
	public void whenImportingCollectionConfigSettingsIfCollectionCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode("")
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1", "First value list's title"))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_invalidCollectionCode"));

	}

	@Test
	public void whenImportingCollectionConfigSettingsIfCollectionCodeDoesNotExistThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode("unknonCollection")
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1", "First value list's title"))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_collectionCodeNotFound"));

	}

	@Test
	public void whenImportingCollectionValueListIfCodeIsInvalidThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(null)
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1", "First value list's title"))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	public void whenImportingCollectionValueListIfCodeDoesNotStartWithDDVPrefixThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode("USRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1", "First value list's title"))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	//@InDevelopmentTest
	public void whenImportingCollectionConfigsSettingsThenSetted()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addValueList(getValueListA());
		collectionSettings.addValueList(getValueListB());
		collectionSettings.addValueList(getValueListC());
		collectionSettings.addValueList(getValueListD());

		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(CODE_1_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR);
		Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		metadataSchemaType = schemaTypes.getSchemaType(CODE_2_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 2");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		metadataSchemaType = schemaTypes.getSchemaType(CODE_3_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 3");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isTrue();
		Metadata parentMetadata = metadataSchemaType.getDefaultSchema().get("parent");
		assertThat(parentMetadata).isNotNull();
		assertThat(parentMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);

		metadataSchemaType = schemaTypes.getSchemaType(CODE_4_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 4");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		// TODO Valid3r classifiedTypes : Obtenir les schemata qu'on a défini, et vérifier q'une métadonnée de type reférenc3 pointant sur le domaine de valeur a été créée

		//newWebDriver();
		//waitUntilICloseTheBrowsers();
	}

	@Test
	public void whenModifyingValueListTitleThenValueIsUpdated()
			throws Exception {

		String codeA = "ddvUSRcodeDuDomaineDeValeurA";
		ImportedValueList valueList = new ImportedValueList().setCode(codeA)
				.setTitles(toTitlesMap(TITLE_FR, TITLE_EN))
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addValueList(valueList);

		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR);
		Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();
		assertThat(codeMetadata.isChildOfRelationship()).isFalse();

		valueList.setTitles(toTitlesMap(TITLE_FR_UPDATED, TITLE_EN_UPDATED))
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");

		importSettings();

		metadataSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType(codeA);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR_UPDATED);
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();
		assertThat(codeMetadata.isChildOfRelationship()).isFalse();

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode(null)
						.setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUserIds(TAXO_USERS)
						.setGroupIds(TAXO_GROUPS)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_EmptyTaxonomyCode"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodePrefixIsInvalidThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode("anotherPrefixTaxonomy")
						.setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUserIds(TAXO_USERS)
						.setGroupIds(TAXO_GROUPS)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidTaxonomyCodePrefix"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeSuffixIsInvalidThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode("taxoPrefixTaxonomy")
						.setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUserIds(TAXO_USERS)
						.setGroupIds(TAXO_GROUPS)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidTaxonomyCodeSuffix"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsThenConfigsAreSaved()
			throws Exception {

		settings.addCollectionsConfigs(getZeCollectionSettings());

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy1 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy1).isNotNull();
		assertThat(taxonomy1.getTitle()).isEqualTo(TAXO_1_TITLE_FR);
		assertThat(taxonomy1.isVisibleInHomePage()).isFalse();
		assertThat(taxonomy1.getGroupIds()).hasSize(1).isEqualTo(TAXO_GROUPS);
		assertThat(taxonomy1.getUserIds()).hasSize(2).isEqualTo(TAXO_USERS);

		MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		Taxonomy taxonomy2 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_2_CODE);

		assertThat(taxonomy2).isNotNull();
		assertThat(taxonomy2.getTitle()).isEqualTo(TAXO_2_TITLE_FR);
		assertThat(taxonomy2.isVisibleInHomePage()).isTrue();
		assertThat(taxonomy2.getGroupIds()).isEmpty();
		assertThat(taxonomy2.getUserIds()).isEmpty();

		references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy2));
		assertThat(references).isEmpty();

		documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy2));
		assertThat(references).isEmpty();

	}

	@Test
	public void whenModifyingCollectionTaxonomyConfigSettingsThenConfigsAreUpdated()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);
		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
				.setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setVisibleOnHomePage(false)
				.setUserIds(TAXO_USERS)
				.setGroupIds(TAXO_GROUPS);

		settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR);
		assertThat(taxonomy.isVisibleInHomePage()).isFalse();
		assertThat(taxonomy.getGroupIds()).hasSize(1).isEqualTo(TAXO_GROUPS);
		assertThat(taxonomy.getUserIds()).hasSize(2).isEqualTo(TAXO_USERS);

		MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		collectionSettings.addTaxonomy(importedTaxonomy
				.setTitles(toTitlesMap(TAXO_1_TITLE_FR_UPDATED, TAXO_1_TITLE_EN))
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setVisibleOnHomePage(false)
				.setUserIds(TAXO_USERS_UPDATED)
				.setGroupIds(TAXO_GROUPS_UPDATED));

		importSettings();

		taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR_UPDATED);
		assertThat(taxonomy.isVisibleInHomePage()).isFalse();
		assertThat(taxonomy.getGroupIds()).isEmpty();
		assertThat(taxonomy.getUserIds()).hasSize(1).isEqualTo(TAXO_USERS_UPDATED);

		folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

		// TODO Valider si on met à jour les classifiedTypes !

	}

	@Test
	public void whenImportingCollectionTypesIfCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addType(new ImportedType().setCode(null).setLabel("Dossier")
						.setTabs(toListOfTabs(tabParams))
						.setDefaultSchema(getFolderDefaultSchema())
						.addSchema(getFolderSchema()))
		);

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_emptyTypeCode"));

	}

	@Test
	public void whenImportingCollectionTypeTabIfCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<String, String> tabParams = new HashMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("", "Mon onglet");

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addType(getImportedType(tabParams)));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_emptyTabCode"));

	}

	@Test
	public void whenImportingCollectionTypeIfCustomSchemasCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addType(new ImportedType().setCode(CODE_FOLDER_SCHEMA_TYPE).setLabel("Dossier")
						.setTabs(toListOfTabs(tabParams))
						.setDefaultSchema(getFolderDefaultSchema())
						.addSchema(getFolderSchema().setCode(null))
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_invalidSchemaCode"));

	}

	@Test
	public void whenImportingCollectionTypesValuesAreSet()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addType(getImportedType(tabParams));
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType(CODE_FOLDER_SCHEMA_TYPE);
		assertThat(schemaType).isNotNull();
		List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
		assertThat(schemaTypeMetadata).isNotNull().hasSize(95);

		validateTypeImport(schemaType, false);

	}

	@Test
	public void whenModifyingCollectionTypesValuesAreSet()
			throws Exception {
		Map<String, String> tabParams = getTabsMap();

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addType(getImportedType(tabParams));
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType(CODE_FOLDER_SCHEMA_TYPE);
		assertThat(schemaType).isNotNull();
		List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
		assertThat(schemaTypeMetadata).isNotNull().hasSize(95);

		validateTypeImport(schemaType, false);

		collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addType(getImportedTypeUpdated());
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		validateTypeImport(schemaType, true);
	}

	@Test
	public void whenImportNewMetadatasWithVisibleInTablesAndSearchThenMarkedAsVisibleInTablesAndSearch()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1").setType(STRING)
				.setVisibleInSearchResult(true).setVisibleInTables(true));
		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

		assertThat(customFolder.getTableMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");
	}

	@Test
	public void whenImportNewMetadatasThenMarkedAsVisibleInFormAndDisplayButNotInTablesAndSearch()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1").setType(STRING));
		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

		assertThat(customFolder.getTableMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");
	}

	@Test
	public void givenNewMetadatasWithVisibleInDisplayFlagDefinedThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInDisplay(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInDisplay(false);
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType(STRING).setVisibleInDisplay(true);
		ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType(STRING).setVisibleInDisplay(false);
		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m3).addMetadata(m4);

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionsConfigs(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes())
				.contains("folder_default_title", "folder_default_m1")
				.doesNotContain("folder_default_m2");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3")
				.doesNotContain("folder_custom_m2", "folder_custom_m4");

		//Reverse flags and reimport
		m1.setVisibleInDisplay(false);
		m2.setVisibleInDisplay(true);
		m3.setVisibleInDisplay(false);
		m4.setVisibleInDisplay(true);
		importSettings();

		defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes())
				.contains("folder_default_title", "folder_default_m2")
				.doesNotContain("folder_default_m1");

		customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4")
				.doesNotContain("folder_custom_m1", "folder_custom_m3");
	}

	private void validateTypeImport(MetadataSchemaType schemaType, boolean isUpdate) {
		// Default schema
		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		assertThat(defaultSchema).isNotNull();

		Metadata metadata1 = defaultSchema.get(CODE_METADATA_1);
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(Language.French)).isEqualTo(TITLE_METADATA_1);
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();
		assertThat(metadata1.isDefaultRequirement()).isTrue();
		assertThat(metadata1.isDuplicable()).isFalse();
		assertThat(metadata1.isEnabled()).isTrue();
		assertThat(metadata1.isEncrypted()).isFalse();
		assertThat(metadata1.isEssential()).isFalse();
		assertThat(metadata1.isEssentialInSummary()).isFalse();
		assertThat(metadata1.isMultiLingual()).isFalse();
		assertThat(metadata1.isMultivalue()).isFalse();
		assertThat(metadata1.isSearchable()).isFalse();
		assertThat(metadata1.isSchemaAutocomplete()).isFalse();
		assertThat(metadata1.isSortable()).isFalse();
		assertThat(metadata1.isSystemReserved()).isFalse();
		assertThat(metadata1.isUndeletable()).isFalse();
		assertThat(metadata1.isUniqueValue()).isFalse();
		assertThat(metadata1.isUnmodifiable()).isFalse();

		Metadata metadata2 = defaultSchema.get(CODE_METADATA_2);
		assertThat(metadata2).isNotNull();

		assertThat(metadata2.getType()).isEqualTo(STRING);
		assertThat(metadata2.isEnabled()).isTrue();
		assertThat(metadata2.isEssentialInSummary()).isTrue();
		assertThat(metadata2.isUniqueValue()).isTrue();
		assertThat(metadata2.isMultivalue()).isFalse();

		MetadataDisplayConfig metadata2DisplayConfig = getAppLayerFactory()
				.getMetadataSchemasDisplayManager().getMetadata(zeCollection, metadata2.getCode());
		assertThat(metadata2DisplayConfig).isNotNull();
		//assertThat(metadata2DisplayConfig.getMetadataGroupCode()).isEqualTo("zeTab");

		if (!isUpdate) {
			assertThat(metadata2.getLabel(Language.French)).isEqualTo(TITLE_METADATA_2);
			assertThat(metadata2.getInputMask()).isEqualTo("9999-9999");
			assertThat(metadata2.isDefaultRequirement()).isTrue();
			assertThat(metadata2.isDuplicable()).isTrue();
			assertThat(metadata2.isEncrypted()).isFalse();
			assertThat(metadata2.isEssential()).isTrue();
			assertThat(metadata2.isSearchable()).isTrue();
			// TODO valider advanceSearchable : voir display config
			assertThat(metadata2.isSchemaAutocomplete()).isTrue();
			assertThat(metadata2.isSortable()).isTrue();
			assertThat(metadata2.isUnmodifiable()).isTrue();

			assertThat(metadata2DisplayConfig.isVisibleInAdvancedSearch()).isFalse();

		} else {
			assertThat(metadata2.getLabel(Language.French)).isEqualTo(TITLE_METADATA_2_UPDATED);
			assertThat(metadata2.getInputMask()).isEqualTo("9999-0000");
			assertThat(metadata2.isDefaultRequirement()).isFalse();
			assertThat(metadata2.isDuplicable()).isTrue();
			assertThat(metadata2.isEncrypted()).isFalse();
			assertThat(metadata2.isEssential()).isFalse();
			assertThat(metadata2.isSearchable()).isTrue();
			// TODO valider advanceSearchable : voir display config
			assertThat(metadata2.isSchemaAutocomplete()).isTrue();
			assertThat(metadata2.isSortable()).isTrue();
			assertThat(metadata2.isUnmodifiable()).isFalse();
			assertThat(metadata2DisplayConfig.isVisibleInAdvancedSearch()).isTrue();

		}

		MetadataSchema USRschema1 = schemaType.getSchema(CODE_SCHEMA_1);
		assertThat(USRschema1).isNotNull();

		Metadata metadata3 = USRschema1.get(CODE_METADATA_3);
		assertThat(metadata3).isNotNull();
		assertThat(metadata3.getLabel(Language.French)).isEqualTo(TITLE_METADATA_3);
		assertThat(metadata3.getType()).isEqualTo(STRING);
		assertThat(metadata3.getInputMask()).isNullOrEmpty();
		assertThat(metadata3.isDefaultRequirement()).isTrue();
		assertThat(metadata3.isDuplicable()).isFalse();
		assertThat(metadata3.isEnabled()).isTrue();
		assertThat(metadata3.isEncrypted()).isFalse();
		assertThat(metadata3.isEssential()).isFalse();
		assertThat(metadata3.isEssentialInSummary()).isFalse();
		assertThat(metadata3.isMultiLingual()).isFalse();
		assertThat(metadata3.isMultivalue()).isTrue();
		assertThat(metadata3.isSearchable()).isFalse();
		assertThat(metadata3.isSchemaAutocomplete()).isFalse();
		assertThat(metadata3.isSortable()).isFalse();
		assertThat(metadata3.isSystemReserved()).isFalse();
		assertThat(metadata3.isUndeletable()).isFalse();
		assertThat(metadata3.isUniqueValue()).isFalse();
		assertThat(metadata3.isUnmodifiable()).isFalse();
	}

	//-------------------------------------------------------------------------------------

	private void importSettings()
			throws com.constellio.model.frameworks.validation.ValidationException {
		try {
			services.importSettings(settings);
		} catch (ValidationException e) {
			runTwice = false;
			throw e;

		} catch (RuntimeException e) {
			runTwice = false;
			throw e;
		}
	}

	private ListAssert<Tuple> assertThatErrorsWhileImportingSettingsExtracting(String... parameters)
			throws com.constellio.model.frameworks.validation.ValidationException {

		try {
			services.importSettings(settings);
			runTwice = false;
			fail("ValidationException expected");
			return assertThat(new ArrayList<Tuple>());
		} catch (ValidationException e) {

			return assertThat(extractingSimpleCodeAndParameters(e, parameters));
		}
	}

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users),
				withCollection("anotherCollection"));
		services = new SettingsImportServices(getAppLayerFactory());
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		runTwice = true;
	}

	@After
	public void tearDown()
			throws Exception {

		if (runTwice) {
			runTwice = false;
			try {
				SettingsImportServicesAcceptanceTest.class.getMethod(skipTestRule.getCurrentTestName()).invoke(this);
			} catch (Exception e) {
				throw new AssertionError("An exception occured when running the test a second time", e);
			}
		}
	}
}