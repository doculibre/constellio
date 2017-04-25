package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class StorageSpaceSingleContainerAvailableSizeCalculator implements MetadataValueCalculator<Double> {

    LocalDependency<Double> numberOfContainersParam = LocalDependency.toANumber(StorageSpace.NUMBER_OF_CONTAINERS);

    LocalDependency<Double> capacityParam = LocalDependency.toANumber(StorageSpace.CAPACITY);

    @Override
    public Double calculate(CalculatorParameters parameters) {
        Double numberOfContainersParam = parameters.get(this.numberOfContainersParam);
        Double capacityParam = parameters.get(this.capacityParam);

        if(numberOfContainersParam == null || numberOfContainersParam.equals(0)) {
            return capacityParam;
        }
        return 0.0D;
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
        return asList(numberOfContainersParam, capacityParam);
    }
}
