package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

public class DecomListStatusCalculator extends AbstractMetadataValueCalculator<DecomListStatus> {

	LocalDependency<LocalDate> processedDateParam = LocalDependency.toADate(DecommissioningList.PROCESSING_DATE);

	@Override
	public DecomListStatus calculate(CalculatorParameters parameters) {
		LocalDate processedDate = parameters.get(processedDateParam);

		return processedDate == null ? DecomListStatus.GENERATED : DecomListStatus.PROCESSED;
	}

	@Override
	public DecomListStatus getDefaultValue() {
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
		return Arrays.asList(processedDateParam);
	}
}
