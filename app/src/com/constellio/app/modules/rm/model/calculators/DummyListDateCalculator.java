package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Collections.singletonList;

public class DummyListDateCalculator implements MetadataValueCalculator<List<LocalDate>> {
	LocalDependency<String> titleParam = LocalDependency.toAString(Folder.TITLE);

	@Override
	public List<LocalDate> calculate(CalculatorParameters parameters) {
		return singletonList(new LocalDate());
	}

	@Override
	public List<LocalDate> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return singletonList(titleParam);
	}
}
