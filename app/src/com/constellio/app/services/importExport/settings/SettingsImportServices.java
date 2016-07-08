package com.constellio.app.services.importExport.settings;

import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;

public class SettingsImportServices {

    static final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
    static final String CONFIGURATION_NOT_FOUND = "configurationNotFound";
    public static final String INVALID_COLLECTION_CODE = "InvalidCollectionCode";
    public static final String COLLECTION_CODE = "code";

    AppLayerFactory appLayerFactory;
    SystemConfigurationsManager systemConfigurationsManager;
    MetadataSchemasManager schemasManager;

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
                    Object result = Enum.valueOf((Class<? extends Enum>)config.getEnumClass(), importedConfig.getValue());
                    systemConfigurationsManager.setValue(config, result);
                }
            }
        }

        for(ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()){
            String collectionCode = collectionSettings.getCode();
            for(final ImportedValueList importedValueList : collectionSettings.getValueLists()) {
                //if(!schemasManager.getSchemaTypes(collectionCode).hasSchema(importedValueList.getCode())){
                    schemasManager.modify(collectionCode, new MetadataSchemaTypesAlteration() {
                        @Override
                        public void alter(MetadataSchemaTypesBuilder schemaTypesBuilder) {
                            String codeModeText = importedValueList.getCodeMode();
                            ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode schemaTypeCodeMode =
                                    ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE;
                            if(StringUtils.isNotBlank(codeModeText)){
                                schemaTypeCodeMode = EnumUtils.getEnum(ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeCodeMode.class, codeModeText);
                            }

                            ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(schemaTypesBuilder);
                            if(importedValueList.isHierarchical()){
                                builder.createValueListItemSchema(importedValueList.getCode(),
                                        importedValueList.getTitles().get("title_fr"), schemaTypeCodeMode);
                            } else {
                                builder.createHierarchicalValueListItemSchema(importedValueList.getCode(),
                                        importedValueList.getTitles().get("title_fr"), schemaTypeCodeMode);
                            }
                        }
                    });
                //}
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private void validate(ImportedSettings settings, ValidationErrors validationErrors) throws ValidationException {

        validateGlobalConfigs(settings, validationErrors);

        for(ImportedCollectionSettings collectionSettings : settings.getCollectionsConfigs()){
            if(StringUtils.isBlank(collectionSettings.getCode())){
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("config", COLLECTION_CODE);
                parameters.put("value", collectionSettings.getCode());
                validationErrors.add(SettingsImportServices.class,
                        INVALID_COLLECTION_CODE, parameters);
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
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
        if(!Arrays.asList("true", "false").contains(String.valueOf(importedConfig.getValue()))){
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
        if(importedConfig.getValue() == null){
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
