package com.constellio.app.services.importExport.settings;

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
import com.constellio.model.entities.schemas.MetadataSchema;
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
    final String INVALID_COLLECTION_CODE = "invalidCollectionCode";
    final String COLLECTION_CODE_NOT_FOUND = "collectionCodeNotFound";
    final String CODE = "code";
    final String INVALID_VALUE_LIST_CODE = "InvalidValueListCode";
    final String EMPTY_TAXONOMY_CODE = "EmptyTaxonomyCode";
    final String INVALID_TAXONOMY_CODE_PREFIX = "InvalidTaxonomyCodePrefix";
    final String INVALID_TAXONOMY_CODE_SUFFIX = "InvalidTaxonomyCodeSuffix";
    final String DDV_PREFIX = "ddvUSR";
    final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
    final String CONFIGURATION_NOT_FOUND = "configurationNotFound";
    final String EMPTY_TYPE_CODE = "emptyTypeCode";
    final String EMPTY_TAB_CODE = "emptyTabCode";
    final String NULL_DEFAULT_SCHEMA = "nullDefaultSchema";
    final String INVALID_SCHEMA_CODE = "invalidSchemaCode";

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

        final List<TypeMetadataDisplaySetting> displaySettings = new ArrayList<>();

        schemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {

                for (ImportedType importedType : settings.getTypes()) {
                    TypeMetadataDisplaySetting displayConfigs = new TypeMetadataDisplaySetting();
                    displayConfigs.setSchemaType(importedType.getCode());
                    displaySettings.add(displayConfigs);

                    MetadataSchemaTypeBuilder typeBuilder;
                    if (!schemaTypes.hasType(importedType.getCode())) {
                        typeBuilder = types.createNewSchemaType(importedType.getCode());
                    } else {
                        typeBuilder = types.getSchemaType(importedType.getCode());
                    }

                    // Default schema metadata
                    MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();

                    // TODO set tabs

                    importCustomSchemata(types, importedType, displayConfigs, typeBuilder);

                    importSchemaMetadatas(typeBuilder, displayConfigs, importedType.getDefaultSchema(), defaultSchemaBuilder, types);

                }
            }
        });

        // affichage dans la page


        /*

        importedMetadata.getVisibleInDisplayIn();
        importedMetadata.getVisibleInFormIn();
        importedMetadata.getVisibleInResultIn();
        importedMetadata.isVisibleInDisplay();
        importedMetadata.isVisibleInForm();
        importedMetadata.isVisibleInSearchResult();
        importedMetadata.isVisibleInTables();

        com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder#updateSchemaTypeDisplayConfig
        com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder#updateSchemaDisplayConfig
        com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder#updateMetadataDisplayConfig

        si tu fais cette ligne, il ne se passera rien :
        transactionBuilder.updateSchemaDisplayConfig(zeSchema).withNewDisplayMetadataBefore(metadataCode, before) (edited)
        ​*transactionBuilder.add(*​ transactionBuilder.updateSchemaDisplayConfig(zeSchema).withNewDisplayMetadataBefore(metadataCode, before))
        */

        /**/
        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        SchemaTypesDisplayTransactionBuilder transactionBuilder = new SchemaTypesDisplayTransactionBuilder(schemaTypes, displayManager);

        Map<String, MetadataSchema> schemataCache = new HashMap<>();

        /*
        for (TypeMetadataDisplaySetting displaySetting : displaySettings) {

            // Update schemaType
            transactionBuilder
                    .updateSchemaTypeDisplayConfig(schemaTypes.getSchemaType(displaySetting.getSchemaType()));

            // foreach schema in which display is updated
            for (String schemaCode : displaySetting.getSchemata()) {
                String prefixedSchemaCode = displaySetting.getSchemaType() + "_" + schemaCode;
                if (schemaTypes.hasSchema(prefixedSchemaCode)) {
                    MetadataSchema metadataSchema = schemataCache.get(schemaCode);
                    if (metadataSchema == null) {
                        metadataSchema = schemaTypes.getSchema(prefixedSchemaCode);
                        schemataCache.put(schemaCode, metadataSchema);
                    }

                    // update schema display config
                    transactionBuilder.updateSchemaDisplayConfig(metadataSchema)
                            .withNewDisplayMetadataBefore(displaySetting.getMetadata(), "before");

                    Metadata metadata = metadataSchema.getMetadata(displaySetting.getMetadata());

                    transactionBuilder.updateMetadataDisplayConfig(metadata);
                    transactionBuilder.add(transactionBuilder.updateSchemaDisplayConfig(metadataSchema)
                            .withNewDisplayMetadataBefore(displaySetting.getMetadata(), "before"));
                }
            }
        }
        */
    }

    private void importCustomSchemata(MetadataSchemaTypesBuilder types, ImportedType importedType,
                                      TypeMetadataDisplaySetting displaySetting, MetadataSchemaTypeBuilder typeBuilder) {
        for (ImportedMetadataSchema importedMetadataSchema : importedType.getCustomSchemas()) {
            importSchema(types, displaySetting, typeBuilder, importedMetadataSchema);
        }
    }

    private void importSchema(MetadataSchemaTypesBuilder types, TypeMetadataDisplaySetting displaySetting,
                              MetadataSchemaTypeBuilder typeBuilder, ImportedMetadataSchema importedMetadataSchema) {
        MetadataSchemaBuilder customSchemaBuilder;
        displaySetting.addSchema(importedMetadataSchema.getCode());
        try {
            customSchemaBuilder = typeBuilder.createCustomSchema(importedMetadataSchema.getCode(), new HashMap<String, String>());
        } catch (MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined e) {
            customSchemaBuilder = typeBuilder.getCustomSchema(importedMetadataSchema.getCode());
        }
        importSchemaMetadatas(typeBuilder, displaySetting, importedMetadataSchema, customSchemaBuilder, types);
    }

    private void importSchemaMetadatas(MetadataSchemaTypeBuilder typeBuilder, TypeMetadataDisplaySetting displaySetting,
                                       ImportedMetadataSchema importedMetadataSchema, MetadataSchemaBuilder schemaBuilder, MetadataSchemaTypesBuilder typesBuilder) {
        for (ImportedMetadata importedMetadata : importedMetadataSchema.getAllMetadata()) {
            // ... displaySetting.setMetadata(importedMetadata.getCode());
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

        metadataBuilder.addLabel(Language.French, importedMetadata.getLabel());
        metadataBuilder.setDefaultRequirement(importedMetadata.isRequired());

        metadataBuilder.setDuplicable(importedMetadata.isDuplicable());
        metadataBuilder.setEnabled(importedMetadata.isEnabled());
        metadataBuilder.setEncrypted(importedMetadata.isEncrypted());
        metadataBuilder.setEssential(importedMetadata.isEssential());
        metadataBuilder.setEssentialInSummary(importedMetadata.isEssentialInSummary());
        metadataBuilder.setInputMask(importedMetadata.getInputMask());
        metadataBuilder.setMultiLingual(importedMetadata.isMultiLingual());
        metadataBuilder.setUniqueValue(importedMetadata.isUnique());
        if (!importedMetadata.isUnique()) {
            metadataBuilder.setMultivalue(importedMetadata.isMultiValue());
        }
        // TODO valider si SchemaAutocomplete == isRecordAutocomplete()
        metadataBuilder.setSchemaAutocomplete(importedMetadata.isRecordAutocomplete());
        metadataBuilder.setSearchable(importedMetadata.isSearchable());
        // TODO valider si/comnment setter advanceSearchable
        metadataBuilder.setSortable(importedMetadata.isSortable());
        // TODO valider si la métadonnée fait partie de l'implort
        metadataBuilder.setSystemReserved(false);
        // TODO valider si la métadonnée fait partie de l'import
        metadataBuilder.setTaxonomyRelationship(false);
        // TODO valider comment seeter le groupe auquel appartient la métadonnée.
        // metadataBuilder.setMetadataGroupTab(importedMetadata.getTab());
        // metadataBuilder.setType(importedMetadata.getType());
        // TODO valider a quoi correspond la propriété undeletable
        //metadataBuilder.setUndeletable(importedMetadata.isUnmodifiable());
        metadataBuilder.setUnmodifiable(importedMetadata.isUnmodifiable());

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
                    String title = importedTaxonomy.getTitles().get(TITLE_FR);

                    if (!schemaTypes.hasType(importedTaxonomy.getCode())) {

                        Taxonomy taxonomy = valueListServices.lazyCreateTaxonomy(typesBuilder, taxoCode, title);

                        taxonomies.put(taxonomy, importedTaxonomy);
                    } else {
                        Taxonomy taxonomy = getTaxonomyFor(collectionCode, importedTaxonomy)
                                .withTitle(importedTaxonomy.getTitles().get(TITLE_FR))
                                .withUserIds(importedTaxonomy.getUserIds())
                                .withGroupIds(importedTaxonomy.getGroupIds())
                                .withVisibleInHomeFlag(importedTaxonomy.isVisibleOnHomePage());

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
                    .withVisibleInHomeFlag(importedTaxonomy.isVisibleOnHomePage());
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

                    if (!collectionSchemaTypes.hasType(code)) {

                        String codeModeText = importedValueList.getCodeMode();
                        ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode schemaTypeCodeMode =
                                ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;

                        if (StringUtils.isNotBlank(codeModeText)) {
                            schemaTypeCodeMode = EnumUtils.getEnum(ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.class, codeModeText);
                        }

                        ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(schemaTypesBuilder);

                        if (!importedValueList.isHierarchical()) {
                            builder.createValueListItemSchema(code,
                                    importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
                        } else {
                            builder.createHierarchicalValueListItemSchema(code,
                                    importedValueList.getTitles().get(TITLE_FR), schemaTypeCodeMode);
                        }
                    } else {
                        Map<Language, String> labels = new HashMap<>();
                        labels.put(Language.French, importedValueList.getTitles().get(TITLE_FR));
                        labels.put(Language.English, importedValueList.getTitles().get(TITLE_EN));
                        schemaTypesBuilder.getSchemaType(importedValueList.getCode()).setLabels(labels);

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
        if (!Arrays.asList("true", "false").contains(String.valueOf(importedConfig.getValue()))) {
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
}
