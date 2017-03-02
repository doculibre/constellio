package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class StorageSpaceLinearSizeCalculator implements MetadataValueCalculator<Double> {

    LocalDependency<Double> enteredLinearSizeParam = LocalDependency.toANumber(StorageSpace.LINEAR_SIZE_ENTERED);

    LocalDependency<Double> linearSizeSumParam = LocalDependency.toANumber(StorageSpace.LINEAR_SIZE_SUM);

    LocalDependency<Double> childLinearSizeSumParam = LocalDependency.toANumber(StorageSpace.CHILD_LINEAR_SIZE_SUM);

    LocalDependency<Double> numberOfChildSizeSumParam = LocalDependency.toANumber(StorageSpace.NUMBER_OF_CHILD);

    @Override
    public Double calculate(CalculatorParameters parameters) {
        Double enteredLinearSizeParam = parameters.get(this.enteredLinearSizeParam);
        Double enteredLinearSizeSumParam = parameters.get(this.linearSizeSumParam);
        Double enteredChildLinearSizeSumParam = parameters.get(this.childLinearSizeSumParam);
        Double enteredNumberOfChildSizeSumParam = parameters.get(this.numberOfChildSizeSumParam);

        if (enteredNumberOfChildSizeSumParam != null && enteredNumberOfChildSizeSumParam > 0) {
            return enteredChildLinearSizeSumParam;
        } else {
            return enteredLinearSizeParam != null ? enteredLinearSizeParam : enteredLinearSizeSumParam;
        }
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
        return asList(enteredLinearSizeParam, linearSizeSumParam, childLinearSizeSumParam, numberOfChildSizeSumParam);
    }
}
