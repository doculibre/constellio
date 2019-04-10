package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import org.joda.time.LocalDateTime;

import java.util.List;

import static java.util.Arrays.asList;

public class TemporaryRecordDestructionDateCalculator extends AbstractMetadataValueCalculator<LocalDateTime> {
	private static final double DEFAULT_NUMBER_OF_DAYS_BEFORE_DESTRUCTION = 7.0;
	public LocalDependency<Double> numberOfDaysParams = LocalDependency.toANumber(TemporaryRecord.DAY_BEFORE_DESTRUCTION);
	public LocalDependency<LocalDateTime> creationDate = LocalDependency.toADateTime(Schemas.CREATED_ON.getLocalCode());

	@Override
	public LocalDateTime calculate(CalculatorParameters parameters) {
		return addDaysToCurrentDate(parameters.get(creationDate), parameters.get(numberOfDaysParams));
	}

	@Override
	public LocalDateTime getDefaultValue() {
		return addDaysToCurrentDate(new LocalDateTime(), DEFAULT_NUMBER_OF_DAYS_BEFORE_DESTRUCTION);
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE_TIME;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(numberOfDaysParams, creationDate);
	}

	private LocalDateTime addDaysToCurrentDate(LocalDateTime creationDate, Double numberOfDays) {
		return numberOfDays >= 0 ? creationDate.millisOfDay().withMaximumValue().plusDays(numberOfDays.intValue()) : creationDate.millisOfDay().withMaximumValue().minusDays(Math.abs(numberOfDays.intValue()));
	}
}
