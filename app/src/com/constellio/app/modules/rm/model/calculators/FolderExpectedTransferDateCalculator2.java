package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import org.joda.time.LocalDate;

import java.util.List;

public class FolderExpectedTransferDateCalculator2
		extends AbstractFolderExpectedDateCalculator {

	LocalDependency<List<LocalDate>> transferDatesParam = LocalDependency
			.toADate(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).whichIsMultivalue();
	private LocalDependency<LocalDate> manualExpectedTransferDate = LocalDependency
			.toADate(Folder.MANUAL_EXPECTED_TRANSFER_DATE);

	@Override
	LocalDependency<List<LocalDate>> getDatesDependency() {
		return transferDatesParam;
	}

	@Override
	protected LocalDependency<LocalDate> getManualDateDependency() {
		return manualExpectedTransferDate;
	}

	@Override
	protected LocalDependency<LocalDate> getOtherModeManualDateDependency() {
		return null;
	}
}