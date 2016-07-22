package com.constellio.app.services.importExport.settings;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@UiTest
public class SettingsImportServicesAcceptanceTest extends SettingsImportServicesTestUtils {

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
    public void whenImportingConfigSettingsIfCollectionCodeIsEmptyThenExceptionIsRaised()
            throws Exception {

        settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode("")
                .addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
                        .setTitles(toTitlesMap("Le titre du domaine de valeurs 1", "First value list's title"))
                        .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
                        .setHierarchical(false)));

        assertThatErrorsWhileImportingSettingsExtracting()
                .contains(tuple("SettingsImportServices_invalidCollectionCode"));
    }

    @Test
    public void whenImportingConfigSettingsIfCollectionCodeDoesNotExistThenExceptionIsRaised()
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
    public void whenImportingValueListIfCodeIsInvalidThenExceptionIsRaised()
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
    public void whenImportingValueListIfCodeDoesNotStartWithDDVPrefixThenExceptionIsRaised()
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
    public void whenImportingValueListsThenSet()
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

        valueList.setTitles(toTitlesMap(TITLE_FR_UPDATED, TITLE_EN_UPDATED))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setCodeMode("DISABLED");

        importSettings();

        metadataSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType(codeA);
        assertThat(metadataSchemaType).isNotNull();
        assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR_UPDATED);

    }

    @Test
    //@InDevelopmentTest
    public void whenModifyingValueListCodeModeThenValueIsUpdated()
            throws Exception {

        String codeA = "ddvUSRcodeDuDomaineDeValeurA";
        ImportedValueList valueList = new ImportedValueList().setCode(codeA)
                .setTitles(toTitlesMap(TITLE_FR, TITLE_EN))
                .setCodeMode("DISABLED");

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        collectionSettings.addValueList(valueList);

        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaTypes schemaTypes = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
        Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isFalse();
        assertThat(codeMetadata.isEnabled()).isFalse();

        assertThat(metadataSchemaType).isNotNull();

        valueList.setCodeMode("REQUIRED_AND_UNIQUE");
        collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        collectionSettings.addValueList(valueList);
        importSettings();

        schemaTypes = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

        metadataSchemaType = schemaTypes.getSchemaType(codeA);
        codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
        assertThat(codeMetadata).isNotNull();
        assertThat(codeMetadata.isDefaultRequirement()).isTrue();
        assertThat(codeMetadata.isUniqueValue()).isTrue();
        assertThat(codeMetadata.isEnabled()).isTrue();

    }

    @Test
    public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodeIsEmptyThenExceptionIsRaised()
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
    public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodePrefixIsInvalidThenExceptionIsRaised()
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
    public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodeSuffixIsInvalidThenExceptionIsRaised()
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
    public void whenImportingTaxonomyConfigSettingsThenConfigsAreSaved()
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
        List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

        MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

        Taxonomy taxonomy2 = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_2_CODE);

        assertThat(taxonomy2).isNotNull();
        assertThat(taxonomy2.getTitle()).isEqualTo(TAXO_2_TITLE_FR);
        assertThat(taxonomy2.isVisibleInHomePage()).isTrue();
        assertThat(taxonomy2.getGroupIds()).isEmpty();
        assertThat(taxonomy2.getUserIds()).isEmpty();

        references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy2));
        assertThat(references).isEmpty();

        documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy2));
        assertThat(references).isEmpty();

    }

    @Test
    public void whenModifyingCollectionTaxonomyTitleThenConfigsAreUpdated()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN));

        settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR);

        // modify title
        collectionSettings.addTaxonomy(importedTaxonomy
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR_UPDATED, TAXO_1_TITLE_EN)));

        importSettings();

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR_UPDATED);
    }

    @Test
    public void whenModifyingTaxonomyVisibleInHomePageThenConfigsAreUpdated()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setVisibleOnHomePage(false);

        settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.isVisibleInHomePage()).isFalse();

        importedTaxonomy.setVisibleOnHomePage(true);
        collectionSettings.addTaxonomy(importedTaxonomy);

        importSettings();

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.isVisibleInHomePage()).isTrue();
    }

    @Test
    //@InDevelopmentTest
    public void whenModifyingCollectionTaxonomyUsersAndGroupsThenConfigsAreUpdated()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setUserIds(asList(gandalf, robin))
                .setGroupIds(asList("group1"));

        settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getGroupIds()).hasSize(1).containsExactly("group1");
        assertThat(taxonomy.getUserIds()).hasSize(2).containsExactly(gandalf, robin);


        assertFalse("Le titre de la taxonomie par défaut n'est pas affiché dans la table des taxo", true);

        //newWebDriver();
        //waitUntilICloseTheBrowsers();

        collectionSettings.addTaxonomy(importedTaxonomy
                .setUserIds(asList(aliceWonderland))
                .setGroupIds(asList("group2", "group3")));

        importSettings();

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getGroupIds()).hasSize(2).containsExactly("group2", "group3");
        assertThat(taxonomy.getUserIds()).hasSize(1).containsExactly(aliceWonderland);
    }

    @Test
    public void whenImportingTaxonomyThenClassifiedInIsSet()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER));

        settings.addCollectionsConfigs(collectionSettings.addTaxonomy(importedTaxonomy));

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_1_TITLE_FR);

        MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
        List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);

        MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
        references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy));
        assertThat(references).hasSize(1);
        assertThat(references).extracting("referencedSchemaType").containsOnly(TAXO_1_CODE);
    }

    @Test
    public void whenImportingTypesIfCodeIsEmptyThenExceptionIsRaised()
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
    public void whenImportingTypeTabIfCodeIsEmptyThenExceptionIsRaised()
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
    public void whenImportingTypeIfCustomSchemasCodeIsEmptyThenExceptionIsRaised()
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
    public void whenImportingTypesValuesAreSet()
            throws Exception {

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");
        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(MetadataValueType.STRING)
                .setInputMask("9999-9999");

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
        folderType.addSchema(customSchema);
        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        assertThat(schemaType.getAllMetadatas().size()).isEqualTo(94);

        Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("m1");
        assertThat(metadata1.getType()).isEqualTo(STRING);
        assertThat(metadata1.getInputMask()).isNullOrEmpty();

        Metadata metadata1Custom = schemaType.getSchema("folder_custom").get("folder_custom_m1");
        assertThat(metadata1Custom).isNotNull();

        Metadata metadata2 = schemaType.getSchema("folder_custom").get("folder_custom_m2");
        assertThat(metadata2).isNotNull();
        assertThat(metadata2.getInputMask()).isEqualTo("9999-9999");

    }

    @Test
    public void givenNewMetadataWhenModifyingMetadataLabelThenUpdated() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType()
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setLabel("m1_label");
        folderType.getDefaultSchema().addMetadata(m1);
        collectionSettings.addType(folderType);

        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
        assertThat(schemaTypeMetadata).isNotNull().hasSize(93);

        MetadataSchema defaultSchema = schemaType.getDefaultSchema();
        assertThat(defaultSchema).isNotNull();

        Metadata metadata1 = defaultSchema.get("folder_default_m1");

        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("m1_label");

        m1.setLabel("m1_label_updated");
        importSettings();

        schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        defaultSchema = schemaType.getDefaultSchema();
        metadata1 = defaultSchema.get("folder_default_m1");
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("m1_label_updated");

    }

    @Test
    public void givenNewMetadataWhenModifyingMetadataInputMaskThenUpdated() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType()
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setInputMask("9999-9999").setType(STRING);
        folderType.getDefaultSchema().addMetadata(m1);
        collectionSettings.addType(folderType);

        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
        assertThat(schemaTypeMetadata).isNotNull().hasSize(93);

        MetadataSchema defaultSchema = schemaType.getDefaultSchema();
        assertThat(defaultSchema).isNotNull();

        Metadata metadata1 = defaultSchema.get("folder_default_m1");

        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("m1");

        m1.setInputMask("9999-11111-2222");
        importSettings();

        schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        defaultSchema = schemaType.getDefaultSchema();
        metadata1 = defaultSchema.get("folder_default_m1");
        assertThat(metadata1.getInputMask()).isEqualTo("9999-11111-2222");

    }


    @Test
    public void givenNewMetadatasDefaultValuesThenDefaultValuesOK() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType()
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING);
        folderType.getDefaultSchema().addMetadata(m1);
        collectionSettings.addType(folderType);

        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
        assertThat(schemaTypeMetadata).isNotNull().hasSize(93);

        MetadataSchema defaultSchema = schemaType.getDefaultSchema();
        assertThat(defaultSchema).isNotNull();

        Metadata metadata1 = defaultSchema.get("folder_default_m1");

        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("m1");
        assertThat(metadata1.getType()).isEqualTo(STRING);
        assertThat(metadata1.getInputMask()).isNullOrEmpty();
        assertThat(metadata1.isDefaultRequirement()).isFalse();
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
    }

    // VisibleInForm=true, visibleInDisplay=true, visibleInTables= false, visibleInSearchResult=false
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

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
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

    // default: visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
    // custom : visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
    @Test
    public void whenImportNewMetadatasWithVisibleInSearchAndTablesThenMarkedAsVisibleInFormAndDisplayAndTablesAndSearch()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInForm(true).setVisibleInTables(true));
        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_default_m1")
                .doesNotContain("folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_default_m1")
                .doesNotContain("folder_custom_m2");
    }

    // VisibleInForm=true, visibleInDisplay=true, visibleInTables= false, visiInSearchResult=false
    @Test
    public void whenImportNewMetadatasWithVisibleInSearchAndFormTablesThenMarkedAsVisibleInFormAndDisplayAndTablesAndSearch()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInForm(true).setVisibleInTables(true));
        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_default_m1")
                .doesNotContain("folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_default_m1")
                .doesNotContain("folder_custom_m2");
    }

    // default_m1: visibleInDisplay=true, visibleInSearch=true, visibleInForm=false, visibleInTables=false
    @Test
    public void whenImportNewMetadatasThenMarkedAsVisibleInDisplayAndSearchResultsButNotInFormAndTables()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
                new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
                .setVisibleInDisplay(true).setVisibleInSearchResult(true).setType(STRING));

        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING)
                .setVisibleInDisplay(false).setVisibleInSearchResult(false));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");

        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title")
                .doesNotContain("folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title")
                .doesNotContain("folder_default_m1");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_default_m1", "folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_default_m1", "folder_custom_m2");
    }

    // default_m1: visibleInDisplay=false, visibleInSearch=true, visibleInForm=true, visibleInTables=false
    // custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=false, visibleInTables=false
    @Test
    public void whenImportNewMetadatasThenMarkedAsVisibleInSearchAndFormButNotInDisplayAndTables()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
                new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
                .setVisibleInDisplay(false).setVisibleInSearchResult(true).setVisibleInForm(true).setType(STRING));

        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInSearchResult(false));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");

        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title").doesNotContain("folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title")
                .doesNotContain("folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m2")
                .doesNotContain("folder_custom_m1");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1")
                .doesNotContain("folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_default_m1", "folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title")
                .doesNotContain("folder_default_m1", "folder_custom_m2");
    }

    // default_m1: visibleInDisplay=true, visibleInSearch=true, visibleInForm=true, visibleInTables=true
    // custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=true, visibleInTables=false
    @Test
    public void whenImportNewMetadatasWithAllVisibleThenAllMarkedAsVisible()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
                new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
                .setVisibleInSearchResult(true).setVisibleInTables(true).setType(STRING));

        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");

        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1")
                .doesNotContain("folder_default_m1", "folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1")
                .doesNotContain("folder_default_m1", "folder_custom_m2");
    }

    // default_m1: visibleInDisplay=false, visibleInSearch=true, visibleInForm=false, visibleInTables=true
    // custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=true, visibleInTables=false
    @Test
    public void whenImportNewMetadatasWithVisibleInSearchAndTablesThenFlagsAreSet()
            throws Exception {

        ImportedCollectionSettings collectionSettings =
                new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
                new ImportedMetadataSchema().setCode("default"));
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
        folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
                .setVisibleInSearchResult(true).setVisibleInTables(true).setType(STRING));

        customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType(STRING));

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");

        assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title", "folder_default_m1");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1")
                .doesNotContain("folder_default_m1", "folder_custom_m2");

        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1")
                .doesNotContain("folder_default_m1", "folder_custom_m2");
    }

    @Test
    public void givenNewMetadataWithVisibleInDisplayFlagDefinedThenOK()
            throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType().setDefaultSchema(
                new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInDisplay(true);
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInDisplay(false);
        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType(STRING).setVisibleInDisplay(true);
        ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType(STRING).setVisibleInDisplay(false);
        folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
                .setCode("custom").addMetadata(m3).addMetadata(m4);

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getDisplayMetadataCodes())
                .contains("folder_default_title", "folder_default_m1")
                .doesNotContain("folder_default_m2");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getDisplayMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3")
                .doesNotContain("folder_custom_m2", "folder_custom_m4");

        //Reverse flags and re-import
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

    @Test
    public void givenNewMetadataWithVisibleInFormFlagDefinedThenOK()
            throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType().setDefaultSchema(
                new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInForm(true);
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInForm(false);
        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType(STRING).setVisibleInForm(true);
        ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType(STRING).setVisibleInForm(false);
        folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m3).addMetadata(m4);

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title", "folder_default_m1")
                .doesNotContain("folder_default_m2");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3")
                .doesNotContain("folder_custom_m2", "folder_custom_m4");

        //Reverse flags and reimport
        m1.setVisibleInForm(false);
        m2.setVisibleInForm(true);
        m3.setVisibleInForm(false);
        m4.setVisibleInForm(true);
        importSettings();

        defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getFormMetadataCodes())
                .contains("folder_default_title", "folder_default_m2")
                .doesNotContain("folder_default_m1");

        customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getFormMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4")
                .doesNotContain("folder_custom_m1", "folder_custom_m3");
    }

    @Test
    public void givenNewMetadataWithVisibleInSearchFlagDefinedThenOK()
            throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType().setDefaultSchema(
                new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInSearchResult(true);
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInSearchResult(false);
        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType(STRING).setVisibleInSearchResult(true);
        ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType(STRING).setVisibleInSearchResult(false);
        folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom")
                .addMetadata(m3).addMetadata(m4);

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder).isNotNull();

        assertThat(defaultFolder.getSearchResultsMetadataCodes())
                .contains("folder_default_title", "folder_default_m1")
                .doesNotContain("folder_default_m2");

        SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
                .getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3")
                .doesNotContain("folder_custom_m2", "folder_custom_m4");

        //Reverse flags and reimport
        m1.setVisibleInSearchResult(false);
        m2.setVisibleInSearchResult(true);
        m3.setVisibleInSearchResult(false);
        m4.setVisibleInSearchResult(true);
        importSettings();

        defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getSearchResultsMetadataCodes())
                .contains("folder_default_title", "folder_default_m2")
                .doesNotContain("folder_default_m1");

        customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getSearchResultsMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4")
                .doesNotContain("folder_custom_m1", "folder_custom_m3");
    }

    @Test
    public void givenNewMetadataWithVisibleInTablesFlagDefinedThenOK()
            throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedType folderType = new ImportedType().setDefaultSchema(
                new ImportedMetadataSchema().setCode("default")).setCode("folder");
        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(STRING).setVisibleInTables(true);
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(STRING).setVisibleInTables(false);
        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType(STRING).setVisibleInTables(true);
        ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType(STRING).setVisibleInTables(false);
        folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m3).addMetadata(m4);

        collectionSettings.addType(folderType.addSchema(customSchema));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        SchemaDisplayConfig defaultFolder = getAppLayerFactory()
                .getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m1")
                .doesNotContain("folder_default_m2");

        SchemaDisplayConfig customFolder = getAppLayerFactory()
                .getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3")
                .doesNotContain("folder_custom_m2", "folder_custom_m4");

        //Reverse flags and re-import
        m1.setVisibleInTables(false);
        m2.setVisibleInTables(true);
        m3.setVisibleInTables(false);
        m4.setVisibleInTables(true);
        importSettings();

        defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
        assertThat(defaultFolder.getTableMetadataCodes())
                .contains("folder_default_title", "folder_default_m2")
                .doesNotContain("folder_default_m1");

        customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
        assertThat(customFolder.getTableMetadataCodes())
                .contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4")
                .doesNotContain("folder_custom_m1", "folder_custom_m3");
    }

    @Test
    public void givenNewMetadataWhenDefinesListOfEnabledInSchemaThenReferenceMetadataIsEnabled()
            throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "custom"))
                .setRequiredIn(toListOfString("custom"))
                .setVisibleInFormIn(toListOfString("default", "custom"));
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING)
                .setInputMask("9999-9999");

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
        folderType.addSchema(customSchema);
        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEnabled()).isTrue();

    }

    @Test
    public void whenUpdatingDuplicableThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING).setDuplicable(false);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING).setDuplicable(true);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
        folderType.addSchema(customSchema);
        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isDuplicable()).isFalse();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isDuplicable()).isFalse();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isDuplicable()).isTrue();

        defaultSchema.addMetadata(m1.setDuplicable(true));
        customSchema.addMetadata(m2.setDuplicable(false));
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isDuplicable()).isTrue();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isDuplicable()).isTrue();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isDuplicable()).isFalse();


    }

    @Test
    public void whenUpdatingEnabledThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING)
                .setEnabled(true);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING).setEnabled(true);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

        folderType.addSchema(customSchema);

        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEnabled()).isTrue();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEnabled()).isTrue();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEnabled()).isTrue();

        m1.setEncrypted(true);
        m2.setEncrypted(false);
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEncrypted()).isTrue();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEncrypted()).isTrue();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEncrypted()).isFalse();

    }

    @Test
    public void whenUpdatingEncryptedThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING)
                .setEncrypted(true);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING).setEncrypted(false);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

        folderType.addSchema(customSchema);

        ImportedMetadata m3 = new ImportedMetadata().setCode("m3")
                .setType(MetadataValueType.STRING).setEncrypted(true);
        ImportedMetadataSchema customSchema1 = new ImportedMetadataSchema().setCode("custom1").addMetadata(m3);
        folderType.addSchema(customSchema1);

        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEncrypted()).isTrue();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEncrypted()).isTrue();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEncrypted()).isFalse();

        Metadata folder_custom1_m1 = schemaType.getMetadata("folder_custom1_m1");
        assertThat(folder_custom1_m1.isEncrypted()).isTrue();

        Metadata folder_custom1_m3 = schemaType.getMetadata("folder_custom1_m3");
        assertThat(folder_custom1_m3.isEncrypted()).isTrue();

        m1.setEncrypted(false);
        m2.setEncrypted(true);
        m3.setEncrypted(false);
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEncrypted()).isFalse();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEncrypted()).isFalse();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEncrypted()).isTrue();

        folder_custom1_m1 = schemaType.getMetadata("folder_custom1_m1");
        assertThat(folder_custom1_m1.isEncrypted()).isFalse();

        folder_custom1_m3 = schemaType.getMetadata("folder_custom1_m3");
        assertThat(folder_custom1_m3.isEncrypted()).isFalse();

    }

    @Test
    public void whenUpdatingEssentialAndEssentialInSummaryThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING)
                .setEssential(true).setEssentialInSummary(false);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING).setEssential(false).setEssentialInSummary(true);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
                .setCode("custom").addMetadata(m2);

        folderType.addSchema(customSchema);

        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEssential()).isTrue();
        assertThat(folder_default_m1.isEssentialInSummary()).isFalse();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEssential()).isTrue();
        assertThat(folder_custom_m1.isEssentialInSummary()).isFalse();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEssential()).isFalse();
        assertThat(folder_custom_m2.isEssentialInSummary()).isTrue();

        m1.setEssential(false).setEssentialInSummary(true);
        m2.setEssential(true).setEssentialInSummary(false);
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isEssential()).isFalse();
        assertThat(folder_default_m1.isEssentialInSummary()).isTrue();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isEssential()).isFalse();
        assertThat(folder_custom_m1.isEssentialInSummary()).isTrue();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isEssential()).isTrue();
        assertThat(folder_custom_m2.isEssentialInSummary()).isFalse();

    }

    @Test
    public void whenUpdatingMultivalueAndMultiLingualThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING)
                .setMultiValue(true).setMultiLingual(false);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
                .setType(MetadataValueType.STRING)
                .setMultiValue(false).setMultiLingual(true);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
                .setCode("custom").addMetadata(m2);

        folderType.addSchema(customSchema);

        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isMultivalue()).isTrue();
        assertThat(folder_default_m1.isMultiLingual()).isFalse();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isMultivalue()).isTrue();
        assertThat(folder_custom_m1.isMultiLingual()).isFalse();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isMultivalue()).isFalse();
        assertThat(folder_custom_m2.isMultiLingual()).isTrue();

        m1.setMultiValue(false).setMultiLingual(true);
        m2.setMultiValue(true).setMultiLingual(false);
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isMultivalue()).isFalse();
        assertThat(folder_default_m1.isMultiLingual()).isTrue();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isMultivalue()).isFalse();
        assertThat(folder_custom_m1.isMultiLingual()).isTrue();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isMultivalue()).isTrue();
        assertThat(folder_custom_m2.isMultiLingual()).isFalse();

    }

    @Test
    public void whenUpdatingMultiLingualThenFlagIsSet() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
        ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
        folderType.setDefaultSchema(defaultSchema);

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType(MetadataValueType.STRING)
                .setMultiLingual(true);
        defaultSchema.addMetadata(m1);

        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType(MetadataValueType.STRING)
                .setMultiLingual(false);

        ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
                .setCode("custom").addMetadata(m2);

        folderType.addSchema(customSchema);

        collectionSettings.addType(folderType);
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        // TODO valider que les inheritedBehaivours multivalue=false et multiLingual=false empêchent de modifier
        Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isMultiLingual()).isTrue();

        Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isMultiLingual()).isTrue();

        Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isMultiLingual()).isFalse();

        m1.setMultiLingual(false);
        m2.setMultiLingual(true);
        importSettings();

        schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
                .getSchemaTypes(zeCollection).getSchemaType("folder");

        folder_default_m1 = schemaType.getMetadata("folder_default_m1");
        assertThat(folder_default_m1.isMultiLingual()).isFalse();

        folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
        assertThat(folder_custom_m1.isMultiLingual()).isFalse();

        folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
        assertThat(folder_custom_m2.isMultiLingual()).isTrue();

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