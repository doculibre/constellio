package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListStatusCalculator2 implements MetadataValueCalculator<DecomListStatus> {
	LocalDependency<LocalDate> processingDate = LocalDependency.toADate(DecommissioningList.PROCESSING_DATE);
	LocalDependency<LocalDate> approvalDate = LocalDependency.toADate(DecommissioningList.APPROVAL_DATE);
	LocalDependency<List<DecomListValidation>> validations = LocalDependency.toAStructure(DecommissioningList.VALIDATIONS)
			.whichIsMultivalue();
	LocalDependency<LocalDate> approvalRequestDate = LocalDependency.toADate(DecommissioningList.APPROVAL_REQUEST_DATE);

	@Override
	public DecomListStatus calculate(CalculatorParameters parameters) {
		if (parameters.get(processingDate) != null) {
			return DecomListStatus.PROCESSED;
		}

		if (parameters.get(approvalDate) != null) {
			return DecomListStatus.APPROVED;
		}

		List<DecomListValidation> validations = parameters.get(this.validations);
		boolean validationCompleted = isValidated(validations);
		boolean validationRequested = !(validations.isEmpty() || validationCompleted);

		if (validationRequested) {
			return DecomListStatus.IN_VALIDATION;
		}

		if (parameters.get(approvalRequestDate) != null) {
			return DecomListStatus.IN_APPROVAL;
		}

		if (validationCompleted) {
			return DecomListStatus.VALIDATED;
		}

		return DecomListStatus.GENERATED;
	}

	@Override
	public DecomListStatus getDefaultValue() {
		return DecomListStatus.GENERATED;
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
		return Arrays.asList(processingDate, approvalDate, validations, approvalRequestDate);
	}

	private boolean isValidated(List<DecomListValidation> validations) {
		if (validations.isEmpty()) {
			return false;
		}
		for (DecomListValidation validation : validations) {
			if (!validation.isValidated()) {
				return false;
			}
		}
		return true;
	}
}
