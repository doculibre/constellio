package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import org.joda.time.LocalDate;

public class DocumentExpectedDestructionDateCalculator extends DocumentExpectedInactiveDateCalculator
		implements MetadataValueCalculator<LocalDate> {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DESTRUCTION;
	}
}
