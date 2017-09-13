package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.HashMap;
import java.util.Map;

public class JasperFilePrintableValidator implements RecordMetadataValidator<Content> {
    @Override
    public void validate(Metadata metadata, Content value, ConfigProvider configProvider, ValidationErrors validationErrors) {
        if(!value.getCurrentVersion().getFilename().endsWith(".jasper")) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("FILE_TYPE", value);
            validationErrors.add(getClass(), "INVALID_FILE_TYPE", parameters);
        }
    }
}
