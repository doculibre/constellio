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
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

@UiTest
public class SettingsImportServicesAcceptanceTest extends SettingsImportServicesTestUtils {

    Users users = new Users();

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

        settings.addCollectionsConfigs(getZeCollectionSettings());

        importSettings();

        MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

        MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
        assertThat(metadataSchemaType).isNotNull();

        Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_2_CODE);

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

        taxonomy = getAppLayerFactory().getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_2_CODE);

        assertThat(taxonomy).isNotNull();
        assertThat(taxonomy.getTitle()).isEqualTo(TAXO_2_TITLE_FR);
        assertThat(taxonomy.isVisibleInHomePage()).isTrue();
        assertThat(taxonomy.getGroupIds()).isEmpty();
        assertThat(taxonomy.getUserIds()).isEmpty();

    }

    @Test
    public void whenModifyingCollectionTaxonomyConfigSettingsThenConfigsAreUpdated() throws Exception {

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setVisibleOnHomePage(false)
                .setUserIds(TAXO_USERS)
                .setGroupIds(TAXO_GROUPS);

        settings.addCollectionsConfigs(getZeCollectionSettings());// collectionSettings.addTaxonomy(importedTaxonomy));

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
                .addType(getImportedType(tabParams)));

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

        ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        collectionSettings.addType(getImportedType(tabParams));
        settings.addCollectionsConfigs(collectionSettings);

        importSettings();

        MetadataSchemaType schemaType = metadataSchemasManager
                .getSchemaTypes(zeCollection).getSchemaType("folder");
        assertThat(schemaType).isNotNull();
        List<Metadata> schemaTypeMetadata = schemaType.getAllMetadatas();
        assertThat(schemaTypeMetadata).isNotNull().hasSize(95);

        // Default schema
        MetadataSchema defaultSchema = schemaType.getDefaultSchema();

        Metadata metadata1 = defaultSchema.get("metadata1");
        assertThat(metadata1).isNotNull();
        assertThat(metadata1.getLabel(Language.French)).isEqualTo("Titre métadonnée no.1");
        assertThat(metadata1.getType()).isEqualTo(MetadataValueType.STRING);

        MetadataSchema USRschema1 = schemaType.getSchema("USRschema1");
        Metadata customSchema1Metadata1 = USRschema1.get("metadata1");
        assertThat(customSchema1Metadata1).isNotNull();
        assertThat(customSchema1Metadata1.isEnabled()).isTrue();
        assertThat(customSchema1Metadata1.isDefaultRequirement()).isTrue();

        // TODO valider les metadataGroups/tab

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
    public void setUp() throws Exception {
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