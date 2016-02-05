package com.constellio.app.modules.rm.model.calculators;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.calculators.MetadataValueCalculator;

public class FolderCopyRulesExpectedDepositDatesCalculator
		extends AbstractFolderExpectedInactiveDatesCalculator
		implements MetadataValueCalculator<List<LocalDate>> {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DEPOSIT;
	}
}
