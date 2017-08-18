package com.constellio.app.modules.rm.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.List;

public class TemporaryRecordDestructionDateCalculator implements MetadataValueCalculator<LocalDateTime> {
    private static final int DEFAULT_NUMBER_OF_DAYS_BEFORE_DESTRUCTION = 7;
    public LocalDependency<Double> numberOfDaysParams = LocalDependency.toANumber(TemporaryRecord.DAY_BEFORE_DESTRUCTION);
    @Override
    public LocalDateTime calculate(CalculatorParameters parameters) {
        return addDaysToCurrentDate(Integer.parseInt(parameters.get(numberOfDaysParams).toString()));
    }

    @Override
    public LocalDateTime getDefaultValue() {
        return addDaysToCurrentDate(DEFAULT_NUMBER_OF_DAYS_BEFORE_DESTRUCTION);
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
        return Collections.singletonList(numberOfDaysParams);
    }

    private LocalDateTime addDaysToCurrentDate(int numberOfDays) {
        return new LocalDateTime().millisOfDay().withMaximumValue().plusDays(numberOfDays);
    }
}
