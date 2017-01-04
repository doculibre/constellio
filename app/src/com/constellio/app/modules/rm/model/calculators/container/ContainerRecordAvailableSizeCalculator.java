package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class ContainerRecordAvailableSizeCalculator implements MetadataValueCalculator<Double> {

    LocalDependency<Double> linearSizeParam = LocalDependency.toANumber(ContainerRecord.LINEAR_SIZE);

    LocalDependency<Double> capacityParam = LocalDependency.toANumber(ContainerRecord.CAPACITY);

    @Override
    public Double calculate(CalculatorParameters parameters) {
        Double linearSizeParam = parameters.get(this.linearSizeParam);
        Double capacityParam = parameters.get(this.capacityParam);

        if(capacityParam == null) {
            return null;
        } else if(linearSizeParam == null) {
            return capacityParam;
        }
        return capacityParam - linearSizeParam;
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
        return asList(linearSizeParam, capacityParam);
    }
}
