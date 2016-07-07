package com.constellio.app.services.importExport.settings;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SettingsImportServices {

    static final String INVALID_CONFIGURATION_VALUE = "invalidConfigurationValue";
    static final String CONFIGURATION_NOT_FOUND = "configurationNotFound";

    AppLayerFactory appLayerFactory;
    SystemConfigurationsManager systemConfigurationsManager;

    public SettingsImportServices(AppLayerFactory appLayerFactory) {
        this.appLayerFactory = appLayerFactory;
    }

    public void importSettings(ImportedSettings settings) throws ValidationException {

        ValidationErrors validationErrors = new ValidationErrors();
        systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
        // pour les schemata et domaines de valeur
        // MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

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
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private void validate(ImportedSettings settings, ValidationErrors validationErrors) throws ValidationException {

        for (ImportedConfig importedConfig : settings.getConfigs()) {
            SystemConfiguration config = systemConfigurationsManager.getConfigurationWithCode(importedConfig.getKey());
            if (config == null) {
                Map<String, Object> parameters = toParametersMap(importedConfig);
                validationErrors.add(SettingsImportServices.class, CONFIGURATION_NOT_FOUND, parameters);
            } else {
                if (config.getType() == SystemConfigurationType.BOOLEAN) {
                    if(!Arrays.asList("true", "false").contains(String.valueOf(importedConfig.getValue()))){
                        Map<String, Object> parameters = toParametersMap(importedConfig);
                        validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
                    }
                } else if (config.getType() == SystemConfigurationType.INTEGER) {
                    try {
                        Integer.parseInt(importedConfig.getValue());
                    } catch (NumberFormatException e) {
                        Map<String, Object> parameters = toParametersMap(importedConfig);
                        validationErrors.add(SettingsImportServices.class, INVALID_CONFIGURATION_VALUE, parameters);
                    }
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private Map<String, Object> toParametersMap(ImportedConfig importedConfig) {
        Map<String, Object> parameters = new HashMap();
        parameters.put(importedConfig.getKey(), importedConfig.getValue());
        return parameters;
    }
}
