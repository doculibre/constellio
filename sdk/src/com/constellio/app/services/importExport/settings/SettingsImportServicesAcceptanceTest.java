package com.constellio.app.services.importExport.settings;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class SettingsImportServicesAcceptanceTest extends ConstellioTest {

    static final String FOLDER = "folder";
    static final String DOCUMENT = "document";
    static final String TITLE_FR = "Le titre du domaine de valeurs 1";
    static final String TITLE_EN = "First value list's title";
    static final String TITLE_FR_UPDATED = "Nouveau titre du domaine de valeurs 1";
    static final String TITLE_EN_UPDATED = "First value list's updated title";
    static final String TAXO_1_TITLE_FR = "Le titre de la taxo 1";
    static final String TAXO_1_TITLE_FR_UPDATED = "Nouveau titre de la taxo 1";
    static final String TAXO_1_TITLE_EN = "First taxonomy's title";
    static final String TAXO_2_TITLE_FR = "Le titre de la taxo 2";
    static final String TAXO_2_TITLE_EN = "Second taxonomy's title";

    static final List<String> TAXO_USERS = asList("username1", "username2");
    static final List<String> TAXO_USERS_UPDATED = asList("username1");
    static final List<String> TAXO_GROUPS = asList("groupCode1", "groupCode2");
    static final List<String> TAXO_GROUPS_UPDATED = asList("groupCode1", "groupCode2", "groupCode3");

    SettingsImportServices services;
    ImportedSettings settings = new ImportedSettings();
    ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings();
    ImportedCollectionSettings anotherCollectionSettings = new ImportedCollectionSettings();
    SystemConfigurationsManager systemConfigurationsManager;
    MetadataSchemasManager metadataSchemasManager;
    boolean runTwice;


    @Test
    public void whenImportingUnknownConfigsThenConfigsAreNotSet() throws Exception {

        settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

        assertThatErrorsWhileImportingSettingsExtracting("config").contains(
                tuple("SettingsImportServices_configurationNotFound", "calculatedCloseDateUnknown"));
    }

    @Test
    public void whenImportBadBooleanConfigValueThenValidationExceptionThrown() throws Exception {

        settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));

        assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
                tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate", "notABoolean"));
    }

    @Test
    public void whenImportingBadIntegerConfigValueThenValidationExceptionThrown() throws Exception {
        settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule")
                .setValue("helloInteger"));

        assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
                tuple("SettingsImportServices_invalidConfigurationValue",
                        "calculatedCloseDateNumberOfYearWhenFixedRule", "helloInteger"));
    }

    @Test
    public void whenImportingNullValueConfigsThenNullValueExceptionIsRaised() throws Exception {

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
        assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER)).isEqualTo(false);
        assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

        assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE)).isEqualTo(2015);
        assertThat(systemConfigurationsManager.getValue(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR)).isEqualTo(15);

        assertThat(systemConfigurationsManager.getValue(RMConfigs.YEAR_END_DATE)).isEqualTo("02/28");

        assertThat(systemConfigurationsManager.getValue(RMConfigs.DECOMMISSIONING_DATE_BASED_ON))
                .isEqualTo(DecommissioningDateBasedOn.OPEN_DATE);
    }

    @Test
    public void whenImportingCollectionConfigSettingsIfCollectionCodeIsEmptyThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionConfigSettingsIfCollectionCodeDoesNotExistThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionValueListIfCodeIsInvalidThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionValueListIfCodeDoesNotStartWithDDVPrefixThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionConfigsSettingsThenSetted()
            throws Exception {

        String codeA = "ddvUSRcodeDuDomaineDeValeurA";
        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addValueList(new ImportedValueList().setCode(codeA)
                        .setTitles(toTitlesMap(TITLE_FR, TITLE_EN))
                        .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                        .setCodeMode("DISABLED")
                ));

        String codeB = "ddvUSRcodeDuDomaineDeValeurB";
        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addValueList(new ImportedValueList().setCode(codeB)
                        .setTitles(toTitlesMap("Le titre du domaine de valeurs 2", "Second value list's title"))
                        .setClassifiedTypes(toListOfString(DOCUMENT))
                        .setCodeMode("REQUIRED_AND_UNIQUE")//.setCodeMode("FACULTATIVE")
                ));

        String codeC = "ddvUSRcodeDuDomaineDeValeurC";
        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addValueList(new ImportedValueList().setCode(codeC)
                        .setTitles(toTitlesMap("Le titre du domaine de valeurs 3", "Third value list's title"))
                        .setCodeMode("REQUIRED_AND_UNIQUE")
                        .setHierarchical(true)
                ));

        String codeD = "ddvUSRcodeDuDomaineDeValeurD";
        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addValueList(new ImportedValueList().setCode(codeD)
                        .setTitles(toTitlesMap("Le titre du domaine de valeurs 4", "Fourth value list's title"))
                        .setHierarchical(false)
                ));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
        assertThat(metadataSchemaType).isNotNull();
        assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR);
        Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isFalse();
        assertThat(codeMetadata.isUniqueValue()).isFalse();
        assertThat(codeMetadata.isEnabled()).isFalse();
        assertThat(codeMetadata.isChildOfRelationship()).isFalse();

        metadataSchemaType = schemaTypes.getSchemaType(codeB);
        assertThat(metadataSchemaType).isNotNull();
        assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 2");
        codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isTrue();
        assertThat(codeMetadata.isUniqueValue()).isTrue();
        assertThat(codeMetadata.isEnabled()).isTrue();

        metadataSchemaType = schemaTypes.getSchemaType(codeC);
        assertThat(metadataSchemaType).isNotNull();
        assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 3");
        codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isTrue();
        assertThat(codeMetadata.isUniqueValue()).isTrue();
        assertThat(codeMetadata.isEnabled()).isTrue();

        metadataSchemaType = schemaTypes.getSchemaType(codeD);
        assertThat(metadataSchemaType).isNotNull();
        assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 4");
        codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isTrue();
        assertThat(codeMetadata.isUniqueValue()).isTrue();
        assertThat(codeMetadata.isEnabled()).isTrue();
        Metadata parentMetadata = metadataSchemaType.getDefaultSchema().get("parent");
        assertThat(parentMetadata).isNotNull();
        assertThat(parentMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);

        // TODO Valid3r classifiedTypes : Obtenir les schemata qu'on a défini, et vérifier q'une métadonnée de type reférenc3 pointant sur le domaine de valeur a été créée

    }

    @Test
    public void whenModifyingValueListTitleThenValueIsUpdated() throws Exception {

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
        Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
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
        codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isFalse();
        assertThat(codeMetadata.isUniqueValue()).isFalse();
        assertThat(codeMetadata.isEnabled()).isFalse();
        assertThat(codeMetadata.isChildOfRelationship()).isFalse();

    }

    @Test
    public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeIsEmptyThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodePrefixIsInvalidThenExceptionIsRaised() throws Exception {


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
    public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeSuffixIsInvalidThenExceptionIsRaised() throws Exception {

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
    public void whenImportingCollectionTaxonomyConfigSettingsThenConfigsAreSaved() throws Exception {

        String taxo1Code = "taxoMyFirstType";
        String taxo2Code = "taxoMySecondType";

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addTaxonomy(new ImportedTaxonomy().setCode(taxo1Code)
                        .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
                        .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                        .setVisibleOnHomePage(false)
                        .setUserIds(TAXO_USERS)
                        .setGroupIds(TAXO_GROUPS)
                ));

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addTaxonomy(new ImportedTaxonomy().setCode(taxo2Code)
                        .setTitles(toTitlesMap(TAXO_2_TITLE_FR, TAXO_2_TITLE_EN))
                ));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(taxo1Code);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, taxo1Code);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR);
        assertThat(taxonomy.isVisibleInHomePage()).isFalse();
        assertThat(taxonomy.getGroupIds()).hasSize(2).isEqualTo(TAXO_GROUPS);
        assertThat(taxonomy.getUserIds()).hasSize(2).isEqualTo(TAXO_USERS);

        MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
        List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);

        MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, taxo2Code);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_2_TITLE_FR);
        assertThat(taxonomy.isVisibleInHomePage()).isTrue();
        assertThat(taxonomy.getGroupIds()).isEmpty();
        assertThat(taxonomy.getUserIds()).isEmpty();
    }

    @Test
    public void whenModifyingCollectionTaxonomyConfigSettingsThenConfigsAreUpdated() throws Exception {

        String taxo1Code = "taxoMyFirstType";

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(taxo1Code)
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setVisibleOnHomePage(false)
                .setUserIds(TAXO_USERS)
                .setGroupIds(TAXO_GROUPS);
        settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(taxo1Code);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, taxo1Code);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR);
        assertThat(taxonomy.isVisibleInHomePage()).isFalse();
        assertThat(taxonomy.getGroupIds()).hasSize(2).isEqualTo(TAXO_GROUPS);
        assertThat(taxonomy.getUserIds()).hasSize(2).isEqualTo(TAXO_USERS);

        MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
        List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);

        MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);

        collectionSettings.addTaxonomy(importedTaxonomy
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR_UPDATED, TAXO_1_TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setVisibleOnHomePage(false)
                .setUserIds(TAXO_USERS_UPDATED)
                .setGroupIds(TAXO_GROUPS_UPDATED));

        importSettings();

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, taxo1Code);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR_UPDATED);
        assertThat(taxonomy.isVisibleInHomePage()).isFalse();
        assertThat(taxonomy.getGroupIds()).hasSize(3).isEqualTo(TAXO_GROUPS_UPDATED);
        assertThat(taxonomy.getUserIds()).hasSize(1).isEqualTo(TAXO_USERS_UPDATED);

        folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
        references = folderSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);

        documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(Arrays.asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(taxo1Code);


        // TODO Valider si on met à jour les classifiedTypes !
    }

    @Test
    public void whenImportingCollectionTypesIfCodeIsEmptyThenExceptionIsRaised() throws Exception {

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

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
    public void whenImportingCollectionTypeTabIfCodeIsEmptyThenExceptionIsRaised() throws Exception {

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("", "Mon onglet");

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addType(new ImportedType().setCode("folder").setLabel("Dossier")
                        .setTabs(toListOfTabs(tabParams))
                        .setDefaultSchema(getFolderDefaultSchema())
                        .addSchema(getFolderSchema())
                ));

        assertThatErrorsWhileImportingSettingsExtracting()
                .contains(tuple("SettingsImportServices_emptyTabCode"));

    }


    @Test
    public void whenImportingCollectionTypeIfCustomSchemasCodeIsEmptyThenExceptionIsRaised() throws Exception {

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addType(new ImportedType().setCode("folder").setLabel("Dossier")
                        .setTabs(toListOfTabs(tabParams))
                        .setDefaultSchema(getFolderDefaultSchema())
                        .addSchema(getFolderSchema().setCode(null))
                ));

        assertThatErrorsWhileImportingSettingsExtracting()
                .contains(tuple("SettingsImportServices_invalidSchemaCode"));

    }

    @Test
    public void whenImportingCollectionTypesValuesAreSet() throws Exception {
        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
                .addType(new ImportedType().setCode("folder").setLabel("Dossier")
                        .setTabs(toListOfTabs(tabParams))
                        .setDefaultSchema(getFolderDefaultSchema())
                        .addSchema(getFolderSchema())
                ));

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
        assertThat(schemaType).isNotNull();
    }

    //-------------------------------------------------------------------------------------

    private ImportedMetadataSchema getFolderSchema() {
        return new ImportedMetadataSchema().setCode("USRschema1")
                .addMetadata(new ImportedMetadata().setCode("metadata3").setLabel("Titre métadonnée no.3")
                        .setType(MetadataValueType.STRING.name())
                        .setEnabledIn(toListOfString("default", "USRschema1", "USRschema2"))
                        .setMultiValue(true)
                        .setRequiredIn(toListOfString("USRschema1")));
    }

    private ImportedMetadataSchema getFolderDefaultSchema() {
        return new ImportedMetadataSchema().setCode("default")
                .addMetadata(new ImportedMetadata().setCode("metadata1").setLabel("Titre métadonnée no.1")
                        .setType(MetadataValueType.STRING.name())
                        .setEnabledIn(toListOfString("default", "USRschema1", "USRschema2"))
                        .setRequiredIn(toListOfString("USRschema1")))
                .addMetadata(new ImportedMetadata().setCode("metadata2").setLabel("Titre métadonnée no.2")
                        .setEnabled(true)
                        .setType(MetadataValueType.STRING.name())
                        .setTabCode("zeTab")
                        .setMultiValue(true)
                        .setBehaviours(toListOfString("searchable", "advanced-search")));
    }

    private List<ImportedTab> toListOfTabs(Map<String, String> tabParams) {
        List<ImportedTab> tabs = new ArrayList<>();
        for (Map.Entry<String, String> entry : tabParams.entrySet()) {
            tabs.add(new ImportedTab().setCode(entry.getKey()).setValue(entry.getValue()));
        }
        return tabs;
    }

    private List<String> toListOfString(String... values) {
        return Arrays.asList(values);
    }

    private Map<String, String> toTitlesMap(String title_fr, String title_en) {
        Map<String, String> titles = new HashMap<>();
        titles.put("title_fr", title_fr);
        titles.put("title_en", title_en);

        return titles;
    }

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
    public void setUp() throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule(),
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
