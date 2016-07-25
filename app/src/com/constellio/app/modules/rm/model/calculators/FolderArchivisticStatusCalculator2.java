package com.constellio.app.modules.rm.model.calculators;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderArchivisticStatusCalculator2 implements MetadataValueCalculator<FolderStatus> {
	LocalDependency<LocalDate> transferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	LocalDependency<LocalDate> depositDateParam = LocalDependency.toADate(Folder.ACTUAL_DEPOSIT_DATE);
	LocalDependency<LocalDate> destructionDateParam = LocalDependency.toADate(Folder.ACTUAL_DESTRUCTION_DATE);

	@Override
	public FolderStatus calculate(CalculatorParameters parameters) {
		LocalDate transferDate = parameters.get(transferDateParam);
		LocalDate depositDate = parameters.get(depositDateParam);
		LocalDate destructionDate = parameters.get(destructionDateParam);

		FolderStatus status;

		if (depositDate != null) {
			status = FolderStatus.INACTIVE_DEPOSITED;

		} else if (destructionDate != null) {
			status = FolderStatus.INACTIVE_DESTROYED;

		} else if (transferDate != null) {
			status = FolderStatus.SEMI_ACTIVE;

		} else {
			status = FolderStatus.ACTIVE;
		}

		return status;
	}

	@Override
	public FolderStatus getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.ENUM;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(transferDateParam, depositDateParam, destructionDateParam);
	}
}
