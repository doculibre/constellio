package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordImplRuntimeException;
import com.constellio.model.services.records.RecordValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class MediumTypeValidator implements RecordValidator {

	public static final String DM_CODE_MUST_NOT_BE_MODIFIED = "DMcodeMustNotBeModified";
	public static final String ANALOGICAL_CANNOT_BE_ACTIVATED_ON_CONTENT = "cannotBeAnalogicalAndActivatedOnContent";
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

			if (mediumType.isAnalogical() && mediumType.isActivatedOnContent()) {
				params.getValidationErrors().add(MediumTypeValidator.class, ANALOGICAL_CANNOT_BE_ACTIVATED_ON_CONTENT);
			}

		} catch (RecordImplRuntimeException.RecordImplException_UnsupportedOperationOnUnsavedRecord e) {
		}
	}
}
