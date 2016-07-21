package com.constellio.app.services.importExport.settings;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class SettingsImportServices {

    public static final String CLASSIFIED_IN_GROUP_LABEL = "classifiedInGroupLabel";
    static final String TAXO_PREFIX = "taxo";
    static final String TYPE = "Type";
    static final String TAXO_SUFFIX = TYPE;
    static final String TITLE_FR = "title_fr";
    static final String TITLE_EN = "title_en";
    static final String TAXO = "taxo";
    static final String CONFIG = "config";
    static final String VALUE = "value";
    static final String ENCRYPTED = "encrypted";
    static final String INVALID_COLLECTION_CODE = "invalidCollectionCode";
    static final String COLLECTION_CODE_NOT_FOUND = "collectionCodeNotFound";
    static final String CODE = "code";
    static final String INVALID_VALUE_LIST_CODE = "InvalidValueListCode";
    static final String EMPTY_TAXONOMY_CODE = "EmptyTaxonomyCode";
    static final String INVALID_TAXONOMY_CODE_PREFIX = "InvalidTaxonomyCodePrefix";
    static final String INVALID_TAXONOMY_CODE_SUFFIX = "InvalidTaxonomyCodeSuffix";
    static final String DDV_PREFIX = "ddvUSR";
    static final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
    static final String CONFIGURATION_NOT_FOUND = "configurationNotFound";
    static final String EMPTY_TYPE_CODE = "emptyTypeCode";
    static final String EMPTY_TAB_CODE = "emptyTabCode";
    static final String NULL_DEFAULT_SCHEMA = "nullDefaultSchema";
    static final String INVALID_SCHEMA_CODE = "invalidSchemaCode";

    static final String SEARCHABLE = "searchable";
    static final String ADVANCE_SEARCHABLE = "advanceSearchable";
    static final String UNMODIFIABLE = "unmodifiable";
    public static final String SORTABLE = "sortable";
    public static final String RECORD_AUTOCOMPLETE = "recordAutocomplete";
    public static final String ESSENTIAL = "essential";
    public static final String ESSENTIAL_IN_SUMMARY = "essentialInSummary";
    public static final String MULTI_LINGUAL = "multiLingual";
    public static final String DUPLICABLE = "duplicable";
    static final String UNIQUE = "unique";

    AppLayerFactory appLayerFactory;
    SystemConfigurationsManager systemConfigurationsManager;
    MetadataSchemasManager schemasManager;
    ValueListServices valueListServices;

    public SettingsImportServices(AppLayerFactory appLayerFactory) {
        this.appLayerFactory = appLayerFactory;
    }

    public void importSettings(ImportedSettings settings) throws ValidationException {

        ValidationErrors validationErrors = new ValidationErrors();
        systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
        schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

        validate(settings, validationErrors);

        run(settings);
    }

    private void run(ImportedSettings settings) throws ValidationException {

        importGlobalConfigurations(settings);

        for (final ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {

            importCollectionConfigurations(collectionSettings);
        }
    }

    private void importCollectionConfigurations(final ImportedCollectionSettings collectionSettings) {
        final String collectionCode = collectionSettings.getCode();
        final MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collectionCode);

        importCollectionsValueLists(collectionSettings, collectionCode, schemaTypes);

        importCollectionTaxonomies(collectionSettings, collectionCode, schemaTypes);

        importCollectionTypes(collectionSettings, collectionCode, schemaTypes);

    }

    private void importCollectionTypes(final ImportedCollectionSettings settings,
                                       String collection, final MetadataSchemaTypes schemaTypes) {

        schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {

                for (ImportedType importedType : settings.getTypes()) {

                    MetadataSchemaTypeBuilder typeBuilder;
                    if (!schemaTypes.hasType(importedType.getCode())) {
                        typeBuilder = types.createNewSchemaType(importedType.getCode());
                    } else {
                        typeBuilder = types.getSchemaType(importedType.getCode());
                    }

                    // Default schema metadata
                    MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();

                    importCustomSchemata(types, importedType.getCustomSchemas(), typeBuilder);

                    importSchemaMetadatas(typeBuilder, importedType.getDefaultSchema(), defaultSchemaBuilder, types);

                    // Valider si les changements sont pris en charge !!!
                }


            }
        });

        updateSettingsMetadata(settings, schemaTypes);

    }

    private void updateSettingsMetadata(ImportedCollectionSettings settings, MetadataSchemaTypes schemaTypes) {
        // TODO set tabs
        // 1- créer tab si elle n'existe pas
        // 2- configurer la propriété tab de la métadonnée

        // affichage dans la page
        /*
        // TODO valider si/comnment setter advanceSearchable:  voir DisplayConfig/MetadataDisplay
        // TODO valider comment seeter le groupe/tab : voir displayConfig

        importedMetadata.getVisibleInDisplay();
        importedMetadata.getVisibleInDisplayIn();

        importedMetadata.getVisibleInForm();
        importedMetadata.getVisibleInFormIn();

        importedMetadata.getVisibleInResultIn();
        importedMetadata.getVisibleInSearchResult();

        importedMetadata.getVisibleInTables();
        importedMetadata.getVisibleInTablesIn();
        */

        // TODO create metadata tab if not available
        // TODO set metadta tab

        List<ImportedTypeMetadata> typeMetadata = loadTypeMetadata(settings.getTypes());

        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        SchemaTypesDisplayTransactionBuilder transactionBuilder = new SchemaTypesDisplayTransactionBuilder(schemaTypes, displayManager);

        /*
        // Display
        for (ImportedTypeMetadata typeItem : typeMetadata) {

            Map<String, MetadataSchema> metadataSchemataCache = new HashMap<>();

            String typeCode = typeItem.getType();

            // Update schemaType
            // Creer le group
            // l'ajouter au type
            transactionBuilder.updateSchemaTypeDisplayConfig(schemaTypes.getSchemaType(typeCode));

            // foreach schema in which display is updated
            for (String schema : typeItem.getSchemata()) {
                String prefixedSchemaCode = typeCode + "_" + schema;
                MetadataSchema metadataSchema = metadataSchemataCache.get(schema);
                if (metadataSchema == null && schemaTypes.hasSchema(prefixedSchemaCode)) {
                    metadataSchema = schemaTypes.getSchema(prefixedSchemaCode);
                    metadataSchemataCache.put(schema, metadataSchema);
                }

                if (metadataSchema != null) {
                    // display
                    for (String metadataCode : typeItem.getVisibleInDisplayFor(schema)) {

                        String prefixedMetadataCode = typeCode + "_" + schema + "_" + metadataCode;

                        // update metadata
                        SchemaDisplayConfig displayConfig =
                                transactionBuilder.updateSchemaDisplayConfig(metadataSchema)
                                        .withNewDisplayMetadataQueued(metadataCode);
                        // ajouter le group au displayConfig de la metadonne.
                        transactionBuilder.add(displayConfig);
                    }
                }
            }
        }
        displayManager.execute(transactionBuilder.build());
        */
    }

    private List<ImportedTypeMetadata> loadTypeMetadata(List<ImportedType> types) {
        List<ImportedTypeMetadata> list = new ArrayList<>();
        for (ImportedType type : types) {
            ImportedTypeMetadata importedTypeMetadata = new ImportedTypeMetadata();
            importedTypeMetadata.setType(type.getCode());

            List<ImportedMetadataSchema> importedMetadataSchemata = new ArrayList<>();
            importedMetadataSchemata.add(type.getDefaultSchema());
            importedMetadataSchemata.addAll(type.getCustomSchemas());

            addTypeSchemaMetadata(importedMetadataSchemata, importedTypeMetadata);

            list.add(importedTypeMetadata);
        }
        return list;
    }

    private void addTypeSchemaMetadata(List<ImportedMetadataSchema> customMetadataSchemas, ImportedTypeMetadata importedTypeMetadata) {
        String schema;
        for (ImportedMetadataSchema importedMetadataSchema : customMetadataSchemas) {
            schema = importedMetadataSchema.getCode();
            importedTypeMetadata.addSchema(schema);
            for (ImportedMetadata importedMetadata : importedMetadataSchema.getAllMetadata()) {
                addImportedMetadataVisibleProperties(importedTypeMetadata, schema, importedMetadata);
            }
        }
    }

    private void addImportedMetadataVisibleProperties(ImportedTypeMetadata importedTypeMetadata,
                                                      String schema, ImportedMetadata importedMetadata) {

        addMetadataVisibleInDisplayItem(importedTypeMetadata, schema, importedMetadata);

        addMetadataVisibleInFormItem(importedTypeMetadata, schema, importedMetadata);

        addMetadataVisibleInSearchResultItem(importedTypeMetadata, schema, importedMetadata);

        addMetadataVisibleInTablesItems(importedTypeMetadata, schema, importedMetadata);
    }

    private void addMetadataVisibleInDisplayItem(ImportedTypeMetadata importedTypeMetadata, String schema, ImportedMetadata importedMetadata) {
        if (importedMetadata.getVisibleInDisplay() != null && importedMetadata.getVisibleInDisplay()) {
            importedTypeMetadata.addMetadataIsVisibleInDisplay(schema, importedMetadata);
        }
        if (importedMetadata.getVisibleInDisplayIn().size() > 0) {
            for (String targetSchema : importedMetadata.getVisibleInDisplayIn()) {
                importedTypeMetadata.addMetadataIsVisibleInDisplay(targetSchema, importedMetadata);
            }
        }
    }

    private void addMetadataVisibleInFormItem(ImportedTypeMetadata importedTypeMetadata, String schema, ImportedMetadata importedMetadata) {
        if (importedMetadata.getVisibleInForm() != null && importedMetadata.getVisibleInForm()) {
            importedTypeMetadata.addMetadataIsVisibleInForm(schema, importedMetadata);
        }
        if (importedMetadata.getVisibleInFormIn().size() > 0) {
            for (String targetSchema : importedMetadata.getVisibleInFormIn()) {
                importedTypeMetadata.addMetadataIsVisibleInForm(targetSchema, importedMetadata);
            }
        }
    }

    private void addMetadataVisibleInSearchResultItem(ImportedTypeMetadata importedTypeMetadata, String schema, ImportedMetadata importedMetadata) {
        if (importedMetadata.getVisibleInSearchResult() != null && importedMetadata.getVisibleInSearchResult()) {
            importedTypeMetadata.addMetadataIsVisibleInResult(schema, importedMetadata);
        }
        if (importedMetadata.getVisibleInResultIn().size() > 0) {
            for (String targetSchema : importedMetadata.getVisibleInResultIn()) {
                importedTypeMetadata.addMetadataIsVisibleInResult(targetSchema, importedMetadata);
            }
        }
    }

    private void addMetadataVisibleInTablesItems(ImportedTypeMetadata importedTypeMetadata, String schema, ImportedMetadata importedMetadata) {
        if (importedMetadata.getVisibleInTables() != null && importedMetadata.getVisibleInTables()) {
            importedTypeMetadata.addMetadataIsVisibleInTables(schema, importedMetadata);
        }
        if (importedMetadata.getVisibleInTablesIn().size() > 0) {
            for (String targetSchema : importedMetadata.getVisibleInTablesIn()) {
                importedTypeMetadata.addMetadataIsVisibleInTables(targetSchema, importedMetadata);
            }
        }
    }

    private void importCustomSchemata(MetadataSchemaTypesBuilder types, List<ImportedMetadataSchema> importedMetadataSchemata,
                                      MetadataSchemaTypeBuilder typeBuilder) {
        for (ImportedMetadataSchema importedMetadataSchema : importedMetadataSchemata) {
            importSchema(types, typeBuilder, importedMetadataSchema);
        }
    }

    private void importSchema(MetadataSchemaTypesBuilder types,
                              MetadataSchemaTypeBuilder typeBuilder, ImportedMetadataSchema importedMetadataSchema) {
        MetadataSchemaBuilder customSchemaBuilder;
        try {
            customSchemaBuilder = typeBuilder.createCustomSchema(importedMetadataSchema.getCode(), new HashMap<String, String>());
        } catch (MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined e) {
            customSchemaBuilder = typeBuilder.getCustomSchema(importedMetadataSchema.getCode());
        }
        importSchemaMetadatas(typeBuilder, importedMetadataSchema, customSchemaBuilder, types);
    }

    private void importSchemaMetadatas(MetadataSchemaTypeBuilder typeBuilder,
                                       ImportedMetadataSchema importedMetadataSchema, MetadataSchemaBuilder schemaBuilder, MetadataSchemaTypesBuilder typesBuilder) {
        for (ImportedMetadata importedMetadata : importedMetadataSchema.getAllMetadata()) {
            createAndAddMetadata(typeBuilder, schemaBuilder, importedMetadata, typesBuilder);
        }
    }

    private void createAndAddMetadata(MetadataSchemaTypeBuilder typeBuilder, MetadataSchemaBuilder schemaBuilder,
                                      ImportedMetadata importedMetadata, MetadataSchemaTypesBuilder typesBuilder) {
        MetadataBuilder metadataBuilder;
        try {
            metadataBuilder = schemaBuilder.create(importedMetadata.getCode());
            metadataBuilder.setType(importedMetadata.getType());
        } catch (MetadataSchemaBuilderRuntimeException.MetadataAlreadyExists e) {
            metadataBuilder = schemaBuilder.get(importedMetadata.getCode());
        }

        if (StringUtils.isNotBlank(importedMetadata.getLabel())) {
            Map<Language, String> labels = new HashMap<>();
            labels.put(Language.French, importedMetadata.getLabel());
            metadataBuilder.setLabels(labels);
        }

        if (importedMetadata.getRequired() != null) {
            metadataBuilder.setDefaultRequirement(importedMetadata.getRequired());
        }

        List<String> properties = new ArrayList<>();
        if (StringUtils.isNotBlank(importedMetadata.getBehaviours())) {
            properties.addAll(asList(StringUtils.split(importedMetadata.getBehaviours(), ',')));
        }


        if (properties.contains("duplicable")) {
            metadataBuilder.setDuplicable(true);
        }

        if (importedMetadata.getEnabled() != null) {
            metadataBuilder.setEnabled(importedMetadata.getEnabled());
        }

        if (properties.contains(ENCRYPTED)) {
            metadataBuilder.setEncrypted(true);
        }

        if (properties.contains(ESSENTIAL)) {
            metadataBuilder.setEssential(true);
        }

        if (properties.contains(ESSENTIAL_IN_SUMMARY)) {
            metadataBuilder.setEssentialInSummary(true);
        }

        metadataBuilder.setInputMask(importedMetadata.getInputMask());

        if (properties.contains(MULTI_LINGUAL)) {
            metadataBuilder.setMultiLingual(true);
        }

        if (properties.contains(UNIQUE)) {
            metadataBuilder.setUniqueValue(true);
        }

        if (!properties.contains(UNIQUE)) {
            if (importedMetadata.getMultiValue() != null && !importedMetadata.getMultiValue()) {
                metadataBuilder.setMultivalue(importedMetadata.getMultiValue());
            }
        }

        if (properties.contains(RECORD_AUTOCOMPLETE)) {
            metadataBuilder.setSchemaAutocomplete(true);
        }

        if (properties.contains(SEARCHABLE)) {
            metadataBuilder.setSearchable(true);
        }

        if (properties.contains(SORTABLE)) {
            metadataBuilder.setSortable(true);
        }

        if (properties.contains(UNMODIFIABLE)) {
            metadataBuilder.setUnmodifiable(false);
        }

        if ("default".equals(schemaBuilder.getCode())) {
            for (String targetSchema : importedMetadata.getEnabledIn()) {
                String prefixedSchemaCode = typeBuilder.getCode() + "_" + targetSchema;
                MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchema(prefixedSchemaCode);
                if (metadataSchemaBuilder != null) {
                    MetadataBuilder targetSchemaMetadataBuilder = metadataSchemaBuilder.get(importedMetadata.getCode());
                    targetSchemaMetadataBuilder.setEnabled(true);

                    if (importedMetadata.getRequiredIn().contains(targetSchema)) {
                        targetSchemaMetadataBuilder.setDefaultRequirement(true);
                    }
                }
            }
        }
    }

    private void importCollectionTaxonomies(final ImportedCollectionSettings settings,
                                            final String collectionCode, final MetadataSchemaTypes schemaTypes) {

        final Map<Taxonomy, ImportedTaxonomy> taxonomies = new HashMap<>();
        valueListServices = new ValueListServices(appLayerFactory, collectionCode);

        schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder typesBuilder) {

                for (final ImportedTaxonomy importedTaxonomy : settings.getTaxonomies()) {
                    String typeCode = importedTaxonomy.getCode();
                    String taxoCode = StringUtils.substringBetween(typeCode, TAXO, TYPE);
                    String title = null;
                    if (StringUtils.isNotBlank(importedTaxonomy.getTitles().get(TITLE_FR))) {
                        title = importedTaxonomy.getTitles().get(TITLE_FR);
                    }

                    if (!schemaTypes.hasType(importedTaxonomy.getCode())) {
                        Taxonomy taxonomy = valueListServices.lazyCreateTaxonomy(typesBuilder, taxoCode, title);

                        taxonomies.put(taxonomy, importedTaxonomy);
                    } else {
                        Taxonomy taxonomy = getTaxonomyFor(collectionCode, importedTaxonomy)
                                .withTitle(importedTaxonomy.getTitles().get(TITLE_FR))
                                .withUserIds(importedTaxonomy.getUserIds())
                                .withGroupIds(importedTaxonomy.getGroupIds())
                                .withVisibleInHomeFlag(importedTaxonomy.getVisibleOnHomePage());

                        appLayerFactory.getModelLayerFactory().getTaxonomiesManager().editTaxonomy(taxonomy);
                    }
                }
            }
        });

        for (Map.Entry<Taxonomy, ImportedTaxonomy> entry : taxonomies.entrySet()) {
            Taxonomy taxonomy = entry.getKey();
            ImportedTaxonomy importedTaxonomy = entry.getValue();
            Taxonomy currTaxonomy = taxonomy
                    .withUserIds(importedTaxonomy.getUserIds())
                    .withGroupIds(importedTaxonomy.getGroupIds())
                    .withVisibleInHomeFlag(importedTaxonomy.getVisibleOnHomePage());
            appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
                    .addTaxonomy(currTaxonomy, schemasManager);

            String groupLabel = $(CLASSIFIED_IN_GROUP_LABEL);
            for (String classifiedType : importedTaxonomy.getClassifiedTypes()) {
                valueListServices.createAMultivalueClassificationMetadataInGroup(taxonomy, classifiedType, groupLabel);
            }
        }
    }

    private Taxonomy getTaxonomyFor(String collectionCode, ImportedTaxonomy importedTaxonomy) {
        return appLayerFactory.getModelLayerFactory()
                .getTaxonomiesManager().getTaxonomyFor(collectionCode, importedTaxonomy.getCode());
    }

    private void importCollectionsValueLists(final ImportedCollectionSettings collectionSettings,
                                             final String collectionCode, final MetadataSchemaTypes collectionSchemaTypes) {
        schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder schemaTypesBuilder) {

                for (final ImportedValueList importedValueList : collectionSettings.getValueLists()) {

                    final String code = importedValueList.getCode();

                    String codeModeText = importedValueList.getCodeMode();
                    ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode schemaTypeCodeMode =
                            ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;

                    if (StringUtils.isNotBlank(codeModeText)) {
                        schemaTypeCodeMode = EnumUtils.getEnum(ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.class, codeModeText);
                    }

                    if (!collectionSchemaTypes.hasType(code)) {

                        ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(schemaTypesBuilder);

                        if (!importedValueList.isHierarchical()) {

                            builder.createValueListItemSchema(code,
                                    importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
                        } else {
                            builder.createHierarchicalValueListItemSchema(code,
                                    importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
                        }

                    } else {
                        MetadataSchemaTypeBuilder builder = schemaTypesBuilder.getSchemaType(importedValueList.getCode());

                        if (!importedValueList.getTitles().isEmpty()) {
                            Map<Language, String> labels = new HashMap<>();
                            labels.put(Language.French, importedValueList.getTitles().get(TITLE_FR));
                            labels.put(Language.English, importedValueList.getTitles().get(TITLE_EN));
                            builder.setLabels(labels);
                        }

                        if (StringUtils.isNotBlank(codeModeText)) {

                            MetadataBuilder metadataBuilder = builder.getDefaultSchema().getMetadata("code");
                            if (ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.DISABLED == schemaTypeCodeMode) {
                                metadataBuilder.setDefaultRequirement(false);
                                metadataBuilder.setEnabled(false);
                            } else if (ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.FACULTATIVE == schemaTypeCodeMode) {
                                metadataBuilder.setDefaultRequirement(false);
                                metadataBuilder.setEnabled(false);
                                metadataBuilder.setUniqueValue(false);
                            } else if (ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE == schemaTypeCodeMode) {
                                metadataBuilder.setEnabled(true);
                                metadataBuilder.setDefaultRequirement(true);
                                metadataBuilder.setUniqueValue(true);
                            }
                        }
                        // TODO update code mode
                    }
                }
            }
        });
    }

    private void importGlobalConfigurations(ImportedSettings settings) {
        for (ImportedConfig importedConfig : settings.getConfigs()) {
            SystemConfiguration config = systemConfigurationsManager.getConfigurationWithCode(importedConfig.getKey());
            if (config != null) {
                if (config.getType() == SystemConfigurationType.BOOLEAN) {
                    Object value = Boolean.valueOf(importedConfig.getValue());
                    systemConfigurationsManager.setValue(config, value);
                } else if (config.getType() == SystemConfigurationType.INTEGER) {
                    int value = Integer.parseInt(importedConfig.getValue());
                    systemConfigurationsManager.setValue(config, value);
                } else if (config.getType() == SystemConfigurationType.STRING) {
                    systemConfigurationsManager.setValue(config, importedConfig.getValue().trim());
                } else if (config.getType() == SystemConfigurationType.ENUM) {
                    Object result = Enum.valueOf((Class<? extends Enum>) config.getEnumClass(), importedConfig.getValue());
                    systemConfigurationsManager.setValue(config, result);
                }
            }
        }
    }

    private void validate(ImportedSettings settings, ValidationErrors validationErrors) throws ValidationException {

        validateGlobalConfigs(settings, validationErrors);

        validateCollectionConfigs(settings, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private void validateCollectionConfigs(ImportedSettings settings, ValidationErrors validationErrors) {

        for (ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {

            validateCollectionCode(validationErrors, collectionSettings);

            validateCollectionValueLists(validationErrors, collectionSettings);

            validateCollectionTaxonomies(validationErrors, collectionSettings);

            validateCollectionTypes(validationErrors, collectionSettings);

        }
    }

    private void validateCollectionTypes(ValidationErrors errors, ImportedCollectionSettings settings) {
        for (ImportedType importedType : settings.getTypes()) {

            validateTypeCode(errors, importedType.getCode());

            validateHasDefaultSchema(errors, importedType.getDefaultSchema());

            validateTabs(errors, importedType.getTabs());

            validateCustomSchemas(errors, importedType.getCustomSchemas());

        }
    }

    private void validateCustomSchemas(ValidationErrors errors, List<ImportedMetadataSchema> customSchema) {
        for (ImportedMetadataSchema schema : customSchema) {
            if (StringUtils.isBlank(schema.getCode())) {
                Map<String, Object> parameters = new HashMap();
                parameters.put(CONFIG, CODE);
                parameters.put(VALUE, schema.getCode());
                errors.add(SettingsImportServices.class, INVALID_SCHEMA_CODE, parameters);
            }
        }
    }

    private void validateTabs(ValidationErrors errors, List<ImportedTab> importedTabs) {
        for (ImportedTab tab : importedTabs) {
            if (StringUtils.isBlank(tab.getCode())) {
                Map<String, Object> parameters = new HashMap();
                parameters.put(CONFIG, CODE);
                parameters.put(VALUE, tab.getCode());
                errors.add(SettingsImportServices.class, EMPTY_TAB_CODE, parameters);
            }
        }
    }

    private void validateHasDefaultSchema(ValidationErrors errors, ImportedMetadataSchema defaultSchema) {
        if (defaultSchema == null) {
            Map<String, Object> parameters = new HashMap();
            parameters.put(CONFIG, "default-schema");
            parameters.put(VALUE, null);
            errors.add(SettingsImportServices.class, NULL_DEFAULT_SCHEMA, parameters);
        }
    }

    private void validateTypeCode(ValidationErrors errors, String typeCode) {
        if (StringUtils.isBlank(typeCode)) {
            Map<String, Object> parameters = new HashMap();
            parameters.put(CONFIG, CODE);
            parameters.put(VALUE, typeCode);
            errors.add(SettingsImportServices.class, EMPTY_TYPE_CODE, parameters);
        }
    }

    private void validateCollectionTaxonomies(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
        for (ImportedTaxonomy importedTaxonomy : collectionSettings.getTaxonomies()) {
            validateTaxonomyCode(validationErrors, importedTaxonomy);
        }
    }

    private void validateTaxonomyCode(ValidationErrors validationErrors, ImportedTaxonomy importedTaxonomy) {
        String code = importedTaxonomy.getCode();
        if (StringUtils.isBlank(code)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(CONFIG, CODE);
            parameters.put(VALUE, importedTaxonomy.getCode());
            validationErrors.add(SettingsImportServices.class,
                    EMPTY_TAXONOMY_CODE, parameters);
        } else if (!code.startsWith(TAXO_PREFIX)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(CONFIG, CODE);
            parameters.put(VALUE, importedTaxonomy.getCode());
            validationErrors.add(SettingsImportServices.class,
                    INVALID_TAXONOMY_CODE_PREFIX, parameters);
        } else if (!code.endsWith(TAXO_SUFFIX)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(CONFIG, CODE);
            parameters.put(VALUE, importedTaxonomy.getCode());
            validationErrors.add(SettingsImportServices.class,
                    INVALID_TAXONOMY_CODE_SUFFIX, parameters);
        }
    }

    private void validateCollectionValueLists(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
        for (ImportedValueList importedValueList : collectionSettings.getValueLists()) {
            validateValueListCode(validationErrors, importedValueList);
        }
    }

    private void validateCollectionCode(ValidationErrors validationErrors, ImportedCollectionSettings collectionSettings) {
        String collectionCode = collectionSettings.getCode();
        if (StringUtils.isBlank(collectionCode)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(CONFIG, CODE);
            parameters.put(VALUE, collectionCode);
            validationErrors.add(SettingsImportServices.class,
                    INVALID_COLLECTION_CODE, parameters);
        } else {
            try {
                schemasManager.getSchemaTypes(collectionCode);
            } catch (Exception e) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put(CONFIG, CODE);
                parameters.put(VALUE, collectionCode);
                validationErrors.add(SettingsImportServices.class,
                        COLLECTION_CODE_NOT_FOUND, parameters);
            }
        }
    }

    private void validateValueListCode(ValidationErrors validationErrors, ImportedValueList importedValueList) {
        if (StringUtils.isBlank(importedValueList.getCode()) ||
                !importedValueList.getCode().startsWith(DDV_PREFIX)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(CONFIG, importedValueList.getCode());
            parameters.put(VALUE, importedValueList.getTitles().get(TITLE_FR));
            validationErrors.add(SettingsImportServices.class,
                    INVALID_VALUE_LIST_CODE, parameters);
        }
    }

    private void validateGlobalConfigs(ImportedSettings settings, ValidationErrors validationErrors) {
        for (ImportedConfig importedConfig : settings.getConfigs()) {
            SystemConfiguration config = systemConfigurationsManager.getConfigurationWithCode(importedConfig.getKey());
            if (config == null) {
                Map<String, Object> parameters = toParametersMap(importedConfig);
                validationErrors.add(SettingsImportServices.class, CONFIGURATION_NOT_FOUND, parameters);
            } else if (importedConfig.getValue() == null) {
                Map<String, Object> parameters = toParametersMap(importedConfig);
                validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
            } else {
                if (config.getType() == SystemConfigurationType.BOOLEAN) {
                    validateBooleanValueConfig(validationErrors, importedConfig);
                } else if (config.getType() == SystemConfigurationType.INTEGER) {
                    validateIntegerValueConfig(validationErrors, importedConfig);
                } else if (config.getType() == SystemConfigurationType.STRING) {
                    validateStringValueConfig(validationErrors, importedConfig);
                }
            }
        }
    }

    private void validateBooleanValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
        if (!asList("true", "false").contains(String.valueOf(importedConfig.getValue()))) {
            Map<String, Object> parameters = toParametersMap(importedConfig);
            validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
        }
    }

    private void validateIntegerValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
        try {
            Integer.parseInt(importedConfig.getValue());
        } catch (NumberFormatException e) {
            Map<String, Object> parameters = toParametersMap(importedConfig);
            validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
        }
    }

    private void validateStringValueConfig(ValidationErrors validationErrors, ImportedConfig importedConfig) {
        if (importedConfig.getValue() == null) {
            Map<String, Object> parameters = toParametersMap(importedConfig);
            validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
        }
    }

    private Map<String, Object> toParametersMap(ImportedConfig importedConfig) {
        Map<String, Object> parameters = new HashMap();
        parameters.put(CONFIG, importedConfig.getKey());
        parameters.put(VALUE, importedConfig.getValue());
        return parameters;
    }

    private void alterSchemaDisplayList(ImportedType type, SchemaTypesDisplayTransactionBuilder transactionBuilder) {

        alterSchemaList(type, transactionBuilder, new SchemaList() {

            @Override
            public List<String> getValues(SchemaDisplayConfig config) {
                return config.getDisplayMetadataCodes();
            }

            @Override
            public SchemaDisplayConfig setValues(SchemaDisplayConfig config, List<String> newValues) {
                return config.withDisplayMetadataCodes(newValues);
            }

            @Override
            public List<String> getVisibleIn(ImportedMetadata metadata) {
                return metadata.getVisibleInDisplayIn();
            }

            @Override
            public Boolean isVisible(ImportedMetadata metadata) {
                return metadata.getVisibleInDisplay();
            }
        });
    }

    private void alterSchemaList(ImportedType type,
                                 SchemaTypesDisplayTransactionBuilder transactionBuilder,
                                 SchemaList list) {

        //TODO Code générique aux 4 lists!

    }

    private interface SchemaList {

        List<String> getValues(SchemaDisplayConfig config);

        SchemaDisplayConfig setValues(SchemaDisplayConfig config, List<String> newValues);

        List<String> getVisibleIn(ImportedMetadata metadata);

        //return true, false ou null si l'attribut n'est pas spécifié
        Boolean isVisible(ImportedMetadata metadata);
    }
}
