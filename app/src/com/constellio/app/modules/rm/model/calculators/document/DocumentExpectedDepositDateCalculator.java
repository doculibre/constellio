package com.constellio.app.modules.rm.model.calculators.document;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.calculators.MetadataValueCalculator;

public class DocumentExpectedDepositDateCalculator extends DocumentExpectedInactiveDateCalculator
		implements MetadataValueCalculator<LocalDate> {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DEPOSIT;
	}
}
