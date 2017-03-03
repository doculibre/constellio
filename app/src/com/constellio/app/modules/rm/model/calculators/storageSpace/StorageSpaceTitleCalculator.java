package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class StorageSpaceTitleCalculator implements MetadataValueCalculator<String> {

    ReferenceDependency<String> parentStorageParam = ReferenceDependency.toAString(StorageSpace.PARENT_STORAGE_SPACE, StorageSpace.TITLE);

    LocalDependency<String> codeParam = LocalDependency.toAString(StorageSpace.CODE);

    @Override
    public String calculate(CalculatorParameters parameters) {
        String parentTitle = parameters.get(this.parentStorageParam);
        String code = parameters.get(this.codeParam);
        String totalTitle = "";

        if(parentTitle != null) {
            totalTitle = parentTitle + "-";
        }
        return totalTitle + code;
    }

    @Override
    public String getDefaultValue() {
        return "";
    }

    @Override
    public MetadataValueType getReturnType() {
        return MetadataValueType.STRING;
    }

    @Override
    public boolean isMultiValue() {
        return false;
    }

    @Override
    public List<? extends Dependency> getDependencies() {
        return asList(parentStorageParam, codeParam);
    }
}
