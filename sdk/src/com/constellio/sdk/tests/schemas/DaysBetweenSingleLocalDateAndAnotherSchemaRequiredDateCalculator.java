package com.constellio.sdk.tests.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.Days;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator implements MetadataValueCalculator<Double> {

	public static AtomicInteger invokationCounter = new AtomicInteger();

	ReferenceDependency<LocalDateTime> anotherSchemaDateParam = ReferenceDependency.toADateTime("dateRef", "dateMeta")
			.whichIsRequired();
	LocalDependency<LocalDateTime> dateParam = LocalDependency.toADateTime("dateTimeMetadata").whichIsRequired();

	@Override
	public Double calculate(CalculatorParameters values) {
		invokationCounter.incrementAndGet();
		LocalDateTime anotherSchemaDate = values.get(anotherSchemaDateParam);
		LocalDateTime date = values.get(dateParam);
		return Math.abs((double) Days.daysBetween(date, anotherSchemaDate).getDays());
	}

	@Override
	public Double getDefaultValue() {
		return -1.0;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.NUMBER;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(anotherSchemaDateParam, dateParam);
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

}
