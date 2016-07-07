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

    public static final String INVALID_CONFIGURATION = "invalidConfiguration";
    AppLayerFactory appLayerFactory;
    SystemConfigurationsManager manager;

    public SettingsImportServices(AppLayerFactory appLayerFactory) {
        this.appLayerFactory = appLayerFactory;
    }

    public void importSettings(ImportedSettings settings) throws ValidationException {

        ValidationErrors validationErrors = new ValidationErrors();
        manager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();

        for (ImportedConfig importedConfig : settings.getConfigs()) {

            validate(importedConfig, validationErrors);

            SystemConfiguration config = manager.getConfigurationWithCode(importedConfig.getKey());
            if (config != null) {
                if (config.getType() == SystemConfigurationType.BOOLEAN) {
                    if(Arrays.asList("true", "false").contains(String.valueOf(importedConfig.getValue()))){
                        Object value = Boolean.valueOf(importedConfig.getValue());
                        manager.setValue(config, value);
                    } else {
                       Map<String, Object> parameters = toParametersMap(importedConfig);
                       validationErrors.add(INVALID_CONFIGURATION, parameters);
                    }
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
    }

    private void validate(ImportedConfig importedConfig, ValidationErrors validationErrors) {
        SystemConfiguration config = manager.getConfigurationWithCode(importedConfig.getKey());
        if (config == null) {
            Map<String, Object> parameters = toParametersMap(importedConfig);
            validationErrors.add(SettingsImportServices.class.getName() + "_" + importedConfig.getKey(), parameters);
        }
    }

    private Map<String, Object> toParametersMap(ImportedConfig importedConfig) {
        Map<String, Object> parameters = new HashMap();
        parameters.put(importedConfig.getKey(), importedConfig.getValue());
        return parameters;
    }
}
