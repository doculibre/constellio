package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import org.joda.time.LocalDate;

import java.util.List;

public class FolderCopyRulesExpectedDestructionDatesCalculator2
		extends AbstractFolderExpectedInactiveDatesCalculator
		implements MetadataValueCalculator<List<LocalDate>> {

	@Override
	protected DisposalType getCalculatedDisposalType() {
		return DisposalType.DESTRUCTION;
	}
}
