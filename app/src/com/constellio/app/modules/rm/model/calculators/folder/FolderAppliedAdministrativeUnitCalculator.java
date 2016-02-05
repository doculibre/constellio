package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderAppliedAdministrativeUnitCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> parentAdministrativeUnitParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.ADMINISTRATIVE_UNIT);
	LocalDependency<String> enteredAdministrativeUnitParam = LocalDependency.toAReference(Folder.ADMINISTRATIVE_UNIT_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentAdministrativeUnit = parameters.get(parentAdministrativeUnitParam);
		String enteredAdministrativeUnit = parameters.get(enteredAdministrativeUnitParam);

		if (parentAdministrativeUnit != null) {
			return parentAdministrativeUnit;
		} else {
			return enteredAdministrativeUnit;
		}

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentAdministrativeUnitParam, enteredAdministrativeUnitParam);
	}
}
