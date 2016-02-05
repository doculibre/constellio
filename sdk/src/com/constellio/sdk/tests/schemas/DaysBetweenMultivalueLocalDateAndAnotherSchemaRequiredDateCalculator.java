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

public class DaysBetweenMultivalueLocalDateAndAnotherSchemaRequiredDateCalculator implements MetadataValueCalculator<Double> {

	public static AtomicInteger invokationCounter = new AtomicInteger();

	ReferenceDependency<List<LocalDateTime>> anotherSchemaDateParam = ReferenceDependency
			.toADateTime("dateRef", "dateMeta").whichIsMultivalue().whichIsRequired();
	LocalDependency<List<LocalDateTime>> dateParam = LocalDependency.toADateTime("dateTimeMetadata").whichIsMultivalue()
			.whichIsRequired();

	@Override
	public Double calculate(CalculatorParameters values) {
		invokationCounter.incrementAndGet();
		List<LocalDateTime> anotherSchemaDateList = values.get(anotherSchemaDateParam);
		List<LocalDateTime> dateList = values.get(dateParam);
		Double maxDaysBetween = 0.0;
		for (LocalDateTime anotherSchemaDate : anotherSchemaDateList) {
			for (LocalDateTime date : dateList) {
				Double daysBetween = Math.abs((double) Days.daysBetween(date, anotherSchemaDate).getDays());
				maxDaysBetween = Math.max(maxDaysBetween, daysBetween);
			}
		}
		return maxDaysBetween;
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
