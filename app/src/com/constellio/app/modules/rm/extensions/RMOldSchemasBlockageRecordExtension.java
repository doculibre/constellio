package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;

public class RMOldSchemasBlockageRecordExtension extends RecordExtension {

	@Override
	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(FilingSpace.SCHEMA_TYPE)) {
			throw new ImpossibleRuntimeException(
					"Creation of Filing space records is no longer possible, use administrative units instead.");
		}
	}

}
