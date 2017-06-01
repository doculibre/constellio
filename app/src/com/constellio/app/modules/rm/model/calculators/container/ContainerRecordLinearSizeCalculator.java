package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class ContainerRecordLinearSizeCalculator implements MetadataValueCalculator<Double> {

    LocalDependency<Double> enteredLinearSizeParam = LocalDependency.toANumber(ContainerRecord.LINEAR_SIZE_ENTERED);

    LocalDependency<Double> linearSizeSumParam = LocalDependency.toANumber(ContainerRecord.LINEAR_SIZE_SUM);

    LocalDependency<Double> capacityParam = LocalDependency.toANumber(ContainerRecord.CAPACITY);

    LocalDependency<Boolean> isFullParam = LocalDependency.toABoolean(ContainerRecord.FULL);

    @Override
    public Double calculate(CalculatorParameters parameters) {
        Double enteredLinearSizeParam = parameters.get(this.enteredLinearSizeParam);
        Double enteredLinearSizeSumParam = parameters.get(this.linearSizeSumParam);
        Double capacityParam = parameters.get(this.capacityParam);
        Boolean isFull = parameters.get(this.isFullParam);
        if(Boolean.TRUE.equals(isFull)) {
            return capacityParam;
        }

        if(enteredLinearSizeParam != null && enteredLinearSizeSumParam != null && capacityParam != null) {
            if (Math.abs(enteredLinearSizeSumParam - capacityParam) < 0.001) {
                enteredLinearSizeSumParam = capacityParam;
            }
        }

        return enteredLinearSizeParam != null ? enteredLinearSizeParam : enteredLinearSizeSumParam;
    }

    @Override
    public Double getDefaultValue() {
        return null;
    }

    @Override
    public MetadataValueType getReturnType() {
        return MetadataValueType.NUMBER;
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }

    @Override
    public List<? extends Dependency> getDependencies() {
        return asList(enteredLinearSizeParam, linearSizeSumParam, capacityParam, isFullParam);
    }
}
