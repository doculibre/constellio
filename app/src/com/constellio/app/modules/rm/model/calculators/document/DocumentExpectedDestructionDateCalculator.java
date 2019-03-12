package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.model.enums.DisposalType;

public class DocumentExpectedDestructionDateCalculator extends DocumentExpectedInactiveDateCalculator {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DESTRUCTION;
	}
}
