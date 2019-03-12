package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.model.enums.DisposalType;

public class DocumentExpectedDepositDateCalculator extends DocumentExpectedInactiveDateCalculator {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DEPOSIT;
	}
}
