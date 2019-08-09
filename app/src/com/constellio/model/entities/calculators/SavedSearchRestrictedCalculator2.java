package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static com.constellio.app.modules.restapi.core.util.ListUtils.nullToEmpty;
import static java.util.Arrays.asList;

public class SavedSearchRestrictedCalculator2 implements MetadataValueCalculator<Boolean> {

	private LocalDependency<List<String>> shareGroupsParam = LocalDependency.toAReferenceList(SavedSearch.SHARED_GROUPS);
	private LocalDependency<List<String>> shareUsersParam = LocalDependency.toAReferenceList(SavedSearch.SHARED_USERS);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		return !nullToEmpty(parameters.get(shareGroupsParam)).isEmpty() ||
			   !nullToEmpty(parameters.get(shareUsersParam)).isEmpty();
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(shareGroupsParam, shareUsersParam);
	}

}
