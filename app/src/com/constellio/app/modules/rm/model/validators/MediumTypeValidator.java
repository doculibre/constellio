package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by constellios on 2017-03-22.
 */
public class MediumTypeValidator implements RecordValidator {

    public static final String TITLE_MUST_NOT_BE_MODIFIED = "titleMustNotBeModified";
    public static final String TITLE = "title";

//    RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);

    @Override
    public void validate(RecordValidatorParams params) {
        MediumType mediumType = new MediumType(params.getValidatedRecord(), params.getTypes());
        validate(mediumType, params);
    }

    private void validate(MediumType mediumType, RecordValidatorParams params) {

       //TODO wrap le medium type avec rm

        if (!mediumType.getWrappedRecord().getCopyOfOriginalRecord().getSchemaCode().equals(mediumType.getCode())) {
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put(TITLE, mediumType.getTitle());
//
//            params.getValidationErrors().add(MediumTypeValidator.class, TITLE_MUST_NOT_BE_MODIFIED, parameters);
        }
    }
}
