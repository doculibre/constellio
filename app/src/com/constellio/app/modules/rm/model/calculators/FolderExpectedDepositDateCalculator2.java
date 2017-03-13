package com.constellio.app.modules.rm.model.calculators;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

public class FolderExpectedDepositDateCalculator2
		extends AbstractFolderExpectedDateCalculator
		implements MetadataValueCalculator<LocalDate> {

	LocalDependency<List<LocalDate>> depositDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).whichIsMultivalue();

	LocalDependency<LocalDate> manualExpectedDepositDate = LocalDependency.toADate(Folder.MANUAL_EXPECTED_DEPOSIT_DATE);

	LocalDependency<LocalDate> manualExpectedDestructionDate = LocalDependency
			.toADate(Folder.MANUAL_EXPECTED_DESTRUCTION_DATE);

	@Override
	LocalDependency<List<LocalDate>> getDatesDependency() {
		return depositDatesParam;
	}

	@Override
	protected LocalDependency<LocalDate> getManualDateDependency() {
		return manualExpectedDepositDate;
	}

	@Override
	protected LocalDependency<LocalDate> getOtherModeManualDateDependency() {
		return manualExpectedDestructionDate;
	}
}
