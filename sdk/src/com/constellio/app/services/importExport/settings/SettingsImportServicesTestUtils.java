package com.constellio.app.services.importExport.settings;

import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.*;

import static java.util.Arrays.asList;

public class SettingsImportServicesTestUtils extends ConstellioTest {

    public static final String CODE = "code";
    public static final String TITLE = "title";
    public static final String VISIBLE_IN_HOME_PAGE = "visibleInHomePage";
    public static final String CLASSIFIED_TYPES1 = "classifiedTypes";
    public static final String CLASSIFIED_TYPES = CLASSIFIED_TYPES1;
    public static final String GROUPS = "groups";
    public static final String USERS = "users";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String ENABLED = "enabled";
    public static final String ENABLED_IN = "enabledIn";
    public static final String REQUIRED = "required";
    public static final String REQUIRED_IN = "requiredIn";
    public static final String VISIBLE_IN_FORM_IN = "visibleInFormIn";
    public static final String VISIBLE_IN_DISPLAY = "visibleInDisplay";
    public static final String VISIBLE_IN_DISPLAY_IN = "visibleInDisplayIn";
    public static final String VISIBLE_IN_SEARCH_RESULT = "visibleInSearchResult";
    public static final String VISIBLE_IN_RESULT_IN = "visibleInResultIn";
    public static final String VISIBLE_IN_TABLES = "visibleInTables";
    public static final String VISIBLE_IN_TABLES_IN = "visibleInTablesIn";
    public static final String TAB = "tab";
    public static final String MULTI_VALUE = "multiValue";
    public static final String BEHAVIOURS = "behaviours";
    public static final String INPUT_MASK = "inputMask";
    public static final String SEARCHABLE = "searchable";
    public static final String ADVANCE_SEARCHABLE = "advanceSearchable";
    public static final String UNMODIFIABLE = "unmodifiable";
    public static final String SORTABLE = "sortable";
    public static final String RECORD_AUTOCOMPLETE = "recordAutocomplete";
    public static final String ESSENTIAL = "essential";
    public static final String ESSENTIAL_IN_SUMMARY = "essentialInSummary";
    public static final String MULTI_LINGUAL = "multiLingual";
    public static final String DUPLICABLE = "duplicable";
    public static final String VISIBLE_IN_FORM = "visibleInForm";
    protected static final String FOLDER = "folder";
    protected static final String DOCUMENT = "document";
    protected static final String TITLE_FR = "Le titre du domaine de valeurs 1";
    protected static final String TITLE_EN = "First value list's title";
    protected static final String TITLE_FR_UPDATED = "Nouveau titre du domaine de valeurs 1";
    protected static final String TITLE_EN_UPDATED = "First value list's updated title";
    protected static final String TAXO_1_TITLE_FR = "Le titre de la taxonomie 1";
    protected static final String TAXO_1_TITLE_FR_UPDATED = "Nouveau titre de la taxonomie 1";
    protected static final String TAXO_1_TITLE_EN = "First taxonomy's title";
    protected static final String TAXO_2_TITLE_FR = "Le titre de la taxonomie 2";
    protected static final String TAXO_2_TITLE_EN = "Second taxonomy's title";
    protected static final String CODE_1_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur1";
    protected static final String CODE_2_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur2";
    protected static final String CODE_3_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur3";
    protected static final String CODE_4_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur4";
    protected static final List<String> TAXO_USERS = asList("gandalf", "edouard");
    protected static final List<String> TAXO_USERS_UPDATED = asList("robin");
    protected static final List<String> TAXO_GROUPS = asList("heroes");
    protected static final List<String> TAXO_GROUPS_UPDATED = asList();
    protected static final String TAXO_1_CODE = "taxoMyFirstType";
    protected static final String TAXO_2_CODE = "taxoMySecondType";
    SettingsImportServices services;
    ImportedSettings settings = new ImportedSettings();
    ImportedCollectionSettings zeCollectionSettings;
    ImportedCollectionSettings anotherCollectionSettings;

    protected ImportedCollectionSettings getZeCollectionSettings() {

        zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        zeCollectionSettings.addValueList(getValueListA());
        zeCollectionSettings.addValueList(getValueListB());
        zeCollectionSettings.addValueList(getValueListC());
        zeCollectionSettings.addValueList(getValueListD());

        zeCollectionSettings.addTaxonomy(getFirstTaxonomy());

        zeCollectionSettings.addTaxonomy(getSecondTaxonomy());

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

        zeCollectionSettings.addType(getImportedType(tabParams));

        return zeCollectionSettings;
    }

    protected List<ImportedConfig> getImportedConfigs() {
        List<ImportedConfig> configs = new ArrayList<>();
        configs.add(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
        configs.add((new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false")));
        configs.add((new ImportedConfig().setKey("calculatedCloseDate").setValue("false")));

        configs.add((new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015")));
        configs.add((new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15")));

        configs.add((new ImportedConfig().setKey("yearEndDate").setValue("02/28")));
        return configs;
    }

    protected ImportedTaxonomy getSecondTaxonomy() {
        return new ImportedTaxonomy().setCode(TAXO_2_CODE)
                .setTitles(toTitlesMap(TAXO_2_TITLE_FR, TAXO_2_TITLE_EN));
    }

    protected ImportedTaxonomy getFirstTaxonomy() {
        return new ImportedTaxonomy().setCode(TAXO_1_CODE)
                .setTitles(toTitlesMap(TAXO_1_TITLE_FR, TAXO_1_TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setVisibleOnHomePage(false)
                .setUserIds(TAXO_USERS)
                .setGroupIds(TAXO_GROUPS);
    }

    protected ImportedValueList getValueListA() {
        return new ImportedValueList().setCode(CODE_1_VALUE_LIST)
                .setTitles(toTitlesMap(TITLE_FR, TITLE_EN))
                .setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
                .setCodeMode("DISABLED");
    }

    protected ImportedValueList getValueListD() {
        return new ImportedValueList().setCode(CODE_4_VALUE_LIST)
                .setTitles(toTitlesMap("Le titre du domaine de valeurs 4", "Fourth value list's title"))
                .setHierarchical(false);
    }

    protected ImportedValueList getValueListC() {
        return new ImportedValueList().setCode(CODE_3_VALUE_LIST)
                .setTitles(toTitlesMap("Le titre du domaine de valeurs 3", "Third value list's title"))
                .setCodeMode("REQUIRED_AND_UNIQUE")
                .setHierarchical(true);
    }

    protected ImportedValueList getValueListB() {
        return new ImportedValueList().setCode(CODE_2_VALUE_LIST)
                .setTitles(toTitlesMap("Le titre du domaine de valeurs 2", "Second value list's title"))
                .setClassifiedTypes(toListOfString(DOCUMENT))
                .setCodeMode("FACULTATIVE");
    }

    protected ImportedType getImportedType(Map<String, String> tabParams) {
        return new ImportedType().setCode("folder").setLabel("Dossier")
                .setTabs(toListOfTabs(tabParams))
                .setDefaultSchema(getFolderDefaultSchema())
                .addSchema(getFolderSchema());
    }

    protected ImportedMetadataSchema getFolderSchema() {
        return new ImportedMetadataSchema().setCode("USRschema1")
                .addMetadata(getImportedMetadata3());
    }

    private ImportedMetadata getImportedMetadata3() {
        return new ImportedMetadata().setCode("metadata3").setLabel("Titre métadonnée no.3")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "USRschema1", "USRschema2"))
                .setRequiredIn(Arrays.asList("USRschema1"))
                .setMultiValue(true);
    }

    protected ImportedMetadataSchema getFolderDefaultSchema() {
        return new ImportedMetadataSchema().setCode("default")
                .addMetadata(getImportedMetadata1())
                .addMetadata(getImportedMetadata2());
    }

    protected ImportedType getImportedTypeUpdated(Map<String, String> tabParams) {
        return new ImportedType().setCode("folder").setLabel("Dossier modifié")
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")
                        .addMetadata(new ImportedMetadata().setCode("metadata2").setLabel("Nouveau Titre métadonnée no.2")
                                .setType(MetadataValueType.STRING)
                                .setEnabled(true)
                                .setRequired(false) // X
                                .setTab("default")
                                .setMultiValue(true)
                                .setBehaviours(toListOfString("searchableInSimpleSearch", "searchableInAdvancedSearch",
                                        "unique", "unmodifiable", "sortable")) // X
                                .setSearchable(false) //X
                                .setAdvanceSearchable(true)
                                .setUnique(true)
                                .setUnmodifiable(false) // X
                                .setSortable(true)
                                .setRecordAutocomplete(true)
                                .setEssential(false) // X
                                .setEssentialInSummary(true)
                                .setMultiLingual(true)   // cannot be multivalue and unique at same time !
                                .setDuplicable(true)
                                .setInputMask("9999-0000")
                        ));
    }

    private ImportedMetadata getImportedMetadata2() {
        return new ImportedMetadata().setCode("metadata2").setLabel("Titre métadonnée no.2")
                .setType(MetadataValueType.STRING)
                .setEnabled(true)
                .setRequired(true)
                .setTab("zeTab")
                .setMultiValue(true)
                .setBehaviours(toListOfString("searchableInSimpleSearch", "searchableInAdvancedSearch",
                        "unique", "unmodifiable", "sortable", "recordAutocomplete", "essential",
                        "essentialInSummary", "multiLingual", "duplicable"))
                .setSearchable(true)
                .setAdvanceSearchable(true)
                .setUnique(true)
                .setUnmodifiable(true)
                .setSortable(true)
                .setRecordAutocomplete(true)
                .setEssential(true)
                .setEssentialInSummary(true)
                .setMultiLingual(true)   // cannot be multivalue and unique at same time !
                .setDuplicable(true)
                .setInputMask("9999-9999");
    }

    private ImportedMetadata getImportedMetadata1() {
        return new ImportedMetadata().setCode("metadata1").setLabel("Titre métadonnée no.1")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "USRschema1", "USRschema2"))
                .setRequiredIn(toListOfString("USRschema1"))
                .setVisibleInFormIn(toListOfString("default", "USRschema1"));
    }

    protected List<ImportedTab> toListOfTabs(Map<String, String> tabParams) {
        List<ImportedTab> tabs = new ArrayList<>();
        for (Map.Entry<String, String> entry : tabParams.entrySet()) {
            tabs.add(new ImportedTab().setCode(entry.getKey()).setValue(entry.getValue()));
        }
        return tabs;
    }

    protected List<String> toListOfString(String... values) {
        return Arrays.asList(values);
    }

    protected Map<String, String> toTitlesMap(String title_fr, String title_en) {
        Map<String, String> titles = new HashMap<>();
        titles.put("title_fr", title_fr);
        titles.put("title_en", title_en);

        return titles;
    }


}
