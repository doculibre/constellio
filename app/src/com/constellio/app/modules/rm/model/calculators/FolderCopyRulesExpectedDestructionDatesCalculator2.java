package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.enums.DisposalType;

public class FolderCopyRulesExpectedDestructionDatesCalculator2
		extends AbstractFolderExpectedInactiveDatesCalculator {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DESTRUCTION;
	}
}
