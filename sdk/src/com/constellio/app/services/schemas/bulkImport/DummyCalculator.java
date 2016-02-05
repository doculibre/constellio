package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.sdk.tests.TestUtils.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

public class DummyCalculator implements MetadataValueCalculator<LocalDate> {
	LocalDependency<String> titleDependency = LocalDependency.toAString(Schemas.TITLE.getLocalCode());

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		if (parameters == null) {
			return TimeProvider.getLocalDate();
		} else {
			return null;
		}
	}

	@Override
	public LocalDate getDefaultValue() {
		return TimeProvider.getLocalDate().plusDays(1);
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(titleDependency);
	}
}
