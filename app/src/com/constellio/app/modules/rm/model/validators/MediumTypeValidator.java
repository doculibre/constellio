package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordImplRuntimeException;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class MediumTypeValidator implements RecordValidator {

    public static final String DM_CODE_MUST_NOT_BE_MODIFIED = "DMcodeMustNotBeModified";
    public static final String CODE = "code";

    @Override
    public void validate(RecordValidatorParams params) {
        MediumType mediumType = new MediumType(params.getValidatedRecord(), params.getTypes());
        validate(mediumType, params);
    }

    private void validate(MediumType mediumType, RecordValidatorParams params) {


        try {
            String oldCode = mediumType.getWrappedRecord().getCopyOfOriginalRecord().get(Schemas.CODE);

            if (oldCode.equals("DM") && !oldCode.equals(mediumType.getCode())) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put(CODE, mediumType.getCode());

                params.getValidationErrors().add(MediumTypeValidator.class, DM_CODE_MUST_NOT_BE_MODIFIED, parameters);
            }
        } catch (RecordImplRuntimeException.RecordImplException_UnsupportedOperationOnUnsavedRecord e) {
        }
    }
}
