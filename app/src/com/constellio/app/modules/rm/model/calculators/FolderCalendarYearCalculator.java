package com.constellio.app.modules.rm.model.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderCalendarYearCalculator implements MetadataValueCalculator<LocalDate> {
	//LocalDependency<String> calendarYearParam = LocalDependency.toAString(Folder.CALENDAR_YEAR_ENTERED);

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
		return new ArrayList<>();//Arrays.asList(calendarYearParam);
	}
}
