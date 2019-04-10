package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

public class FolderCalendarYearCalculator extends AbstractMetadataValueCalculator<LocalDate> {
	LocalDependency<String> calendarYearParam = LocalDependency.toAString(Schemas.LEGACY_ID.getLocalCode());

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		/*String calendarYearStr = parameters.get(calendarYearParam);

		LocalDate localDate = null;
		try {
			int calendarYear = Integer.parseInt(calendarYearStr);
			localDate = new LocalDate(calendarYear, 1, 1);
		} catch (NumberFormatException e) {
		}

		return localDate;*/
		return new LocalDate();
	}

	@Override
	public LocalDate getDefaultValue() {
		return null;
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
		return Arrays.asList(calendarYearParam);
	}
}
