package com.constellio.app.services.importExport.settings;

import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SettingsImportServices {

    public static final String TAXO_PREFIX = "taxo";
    public static final String TAXO_SUFFIX = "Type";
    public static final String TITLE_FR = "title_fr";
    static final String INVALID_COLLECTION_CODE = "InvalidCollectionCode";
    static final String COLLECTION_CODE_NOT_FOUND = "collectionCodeNotFound";
    static final String CODE = "code";
    static final String INVALID_VALUE_LIST_CODE = "InvalidValueListCode";
    static final String EMPTY_TAXONOMY_CODE = "EmptyTaxonomyCode";
    static final String INVALID_TAXONOMY_CODE_PREFIX = "InvalidTaxonomyCodePrefix";
    static final String INVALID_TAXONOMY_CODE_SUFFIX = "InvalidTaxonomyCodeSuffix";
    static final String DDV_PREFIX = "ddv";
    static final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
    static final String CONFIGURATION_NOT_FOUND = "configurationNotFound";
    static Logger LOG = Logger.getLogger(SettingsImportServices.class);

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

        run(settings, validationErrors);
    }

    private void run(ImportedSettings settings, ValidationErrors validationErrors) throws ValidationException {

        importGlobalConfigurations(settings, validationErrors);

        for (final ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {
            final String collectionCode = collectionSettings.getCode();

            final MetadataSchemaTypes collectionSchemaTypes = schemasManager.getSchemaTypes(collectionCode);
            for (final ImportedValueList importedValueList : collectionSettings.getValueLists()) {

                MetadataSchemaType schemaType = null;
                final String code = importedValueList.getCode();

                try {
                    schemaType = collectionSchemaTypes.getSchemaType(code);
                } catch (Exception e) {
                    LOG.error("schemaType '" + code + "' does not exist !");
                }

                if (schemaType == null) {
                    schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
                        @Override
                        public void alter(MetadataSchemaTypesBuilder schemaTypesBuilder) {

                            String codeModeText = importedValueList.getCodeMode();
                            ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode schemaTypeCodeMode =
                                    ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;

                            if (StringUtils.isNotBlank(codeModeText)) {
                                schemaTypeCodeMode = EnumUtils.getEnum(ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.class, codeModeText);
                            }

                            ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(schemaTypesBuilder);

                            if (importedValueList.isHierarchical()) {
                                builder.createValueListItemSchema(code,
                                        importedValueList.getTitles().get("title_fr"), schemaTypeCodeMode);
                            } else {
                                builder.createHierarchicalValueListItemSchema(code,
                                        importedValueList.getTitles().get("title_fr"), schemaTypeCodeMode);
                            }
                        }
                    });
                }
            }

            final Map<Taxonomy, ImportedTaxonomy> taxonomies = new HashMap<>();
            valueListServices = new ValueListServices(appLayerFactory, collectionCode);
            // Modifier schema
            for (final ImportedTaxonomy importedTaxonomy : collectionSettings.getTaxonomies()) {
                schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
                    @Override
                    public void alter(MetadataSchemaTypesBuilder typesBuilder) {
                        try {

                            String typeCode = importedTaxonomy.getCode();
                            String taxoCode = StringUtils.substringBetween(typeCode, "taxo", "Type");
                            String title = importedTaxonomy.getTitles().get("title_fr");
                            // Ajouter la taxonomie
                            taxonomies.put(valueListServices
                                    .lazyCreateTaxonomy(typesBuilder, taxoCode, title), importedTaxonomy);
                        } catch (Exception e) {
                            LOG.error("Modifying taxonomy failed !", e);
                        }
                    }
                });
            }

            for (Map.Entry<Taxonomy, ImportedTaxonomy> entry : taxonomies.entrySet()) {
                Taxonomy taxonomy = entry.getKey();
                ImportedTaxonomy importedTaxo = entry.getValue();
                taxonomy.withUserIds(importedTaxo.getUsers());
                taxonomy.withGroupIds(importedTaxo.getUserGroups());
                taxonomy.withVisibleInHomeFlag(importedTaxo.isVisibleOnHomePage());
                // TODO Valider ajout classifiedTypes !
                appLayerFactory.getModelLayerFactory().getTaxonomiesManager().editTaxonomy(taxonomy);
            }

        }
    }

    private void importGlobalConfigurations(ImportedSettings settings, ValidationErrors validationErrors) {
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

        for (ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()) {

            String collectionCode = collectionSettings.getCode();
            if (StringUtils.isBlank(collectionCode)) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("config", CODE);
                parameters.put("value", collectionCode);
                validationErrors.add(SettingsImportServices.class,
                        INVALID_COLLECTION_CODE, parameters);
            } else {
                try {
                    schemasManager.getSchemaTypes(collectionCode);
                } catch (Exception e) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("config", CODE);
                    parameters.put("value", collectionCode);
                    validationErrors.add(SettingsImportServices.class,
                            COLLECTION_CODE_NOT_FOUND, parameters);
                }
            }

            for (ImportedValueList importedValueList : collectionSettings.getValueLists()) {
                checkValueListCode(validationErrors, importedValueList);
            }

            for (ImportedTaxonomy importedTaxonomy : collectionSettings.getTaxonomies()) {
                String code = importedTaxonomy.getCode();
                if (StringUtils.isBlank(code)) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("config", CODE);
                    parameters.put("value", importedTaxonomy.getCode());
                    validationErrors.add(SettingsImportServices.class,
                            EMPTY_TAXONOMY_CODE, parameters);
                } else if (!code.startsWith(TAXO_PREFIX)) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("config", CODE);
                    parameters.put("value", importedTaxonomy.getCode());
                    validationErrors.add(SettingsImportServices.class,
                            INVALID_TAXONOMY_CODE_PREFIX, parameters);
                } else if (!code.endsWith(TAXO_SUFFIX)) {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("config", CODE);
                    parameters.put("value", importedTaxonomy.getCode());
                    validationErrors.add(SettingsImportServices.class,
                            INVALID_TAXONOMY_CODE_SUFFIX, parameters);
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private void checkValueListCode(ValidationErrors validationErrors, ImportedValueList importedValueList) {
        if (StringUtils.isBlank(importedValueList.getCode()) ||
                !importedValueList.getCode().startsWith(DDV_PREFIX)) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("config", importedValueList.getCode());
            parameters.put("value", importedValueList.getTitles().get("title_fr"));
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
        parameters.put("config", importedConfig.getKey());
        parameters.put("value", importedConfig.getValue());
        return parameters;
    }
}
