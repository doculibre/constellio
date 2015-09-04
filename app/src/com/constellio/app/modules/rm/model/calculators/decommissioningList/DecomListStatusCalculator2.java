/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
