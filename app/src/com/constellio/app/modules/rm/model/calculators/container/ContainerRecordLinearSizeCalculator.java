package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class ContainerRecordLinearSizeCalculator implements MetadataValueCalculator<String> {

    LocalDependency<String> enteredLinearSizeParam = LocalDependency.toAReference(ContainerRecord.LINEAR_SIZE_ENTERED);

    @Override
    public String calculate(CalculatorParameters parameters) {
        String enteredLinearSizeParam = parameters.get(this.enteredLinearSizeParam);

        return enteredLinearSizeParam;
    }

    @Override
    public String getDefaultValue() {
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
        return asList(enteredLinearSizeParam);
    }
}
