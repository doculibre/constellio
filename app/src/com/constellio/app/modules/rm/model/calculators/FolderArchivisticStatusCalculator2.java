package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

public class FolderArchivisticStatusCalculator2 extends AbstractMetadataValueCalculator<FolderStatus> {
	LocalDependency<LocalDate> transferDateParam = LocalDependency.toADate(Folder.ACTUAL_TRANSFER_DATE);
	LocalDependency<LocalDate> depositDateParam = LocalDependency.toADate(Folder.ACTUAL_DEPOSIT_DATE);
	LocalDependency<LocalDate> destructionDateParam = LocalDependency.toADate(Folder.ACTUAL_DESTRUCTION_DATE);
	LocalDependency<FolderStatus> manualArchivisticStatus = LocalDependency.toAnEnum(Folder.MANUAL_ARCHIVISTIC_STATUS);
	ConfigDependency<AllowModificationOfArchivisticStatusAndExpectedDatesChoice> manualMetadataChoiceConfigDependency
			= RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES.dependency();

	@Override
	public FolderStatus calculate(CalculatorParameters parameters) {
		AllowModificationOfArchivisticStatusAndExpectedDatesChoice manualMetadataChoice = parameters.get(manualMetadataChoiceConfigDependency);
		if (manualMetadataChoice == null || manualMetadataChoice == AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED) {
			return calculateWithoutConsideringManualMetadata(parameters);
		} else {
			FolderStatus manualStatus = parameters.get(manualArchivisticStatus);
			if (manualStatus == null) {
				return calculateWithoutConsideringManualMetadata(parameters);
			} else {
				return manualStatus;
			}
		}

	}

	private FolderStatus calculateWithoutConsideringManualMetadata(CalculatorParameters parameters) {
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
		return Arrays.asList(transferDateParam, depositDateParam, destructionDateParam, manualArchivisticStatus,
				manualMetadataChoiceConfigDependency);
	}
}
