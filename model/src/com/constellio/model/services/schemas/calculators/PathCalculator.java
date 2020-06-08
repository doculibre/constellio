package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class PathCalculator extends AbstractMetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	SpecialDependency<String> idDependency = SpecialDependencies.IDENTIFIER;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		if (parameters.getId().equals("zeSubFolder")) {
			System.out.println("!!");
		}

		List<String> calculatedValue = new ArrayList<>();

		String id = parameters.get(idDependency);
		List<String> parentPathsValue = getParentPaths(parameters);
		if (parentPathsValue != null && !parentPathsValue.isEmpty()) {
			for (String parentPath : parentPathsValue) {
				calculatedValue.add(parentPath + "/" + id);
			}
		} else {
			calculatedValue.add("/" + id);
		}
		return calculatedValue;
	}

	private List<String> getParentPaths(CalculatorParameters parameters) {
		List<String> calculatedValue = new ArrayList<>();
		HierarchyDependencyValue paramValue = parameters.get(taxonomiesParam);

		List<String> paramValuePaths = new ArrayList<>(paramValue.getPaths());
		for (Iterator<String> it = paramValuePaths.iterator(); it.hasNext(); ) {
			if (it.next() == null) {
				it.remove();
			}
		}
		if (paramValuePaths != null && !paramValuePaths.isEmpty()) {
			calculatedValue = paramValuePaths;
		} else if (paramValue.getTaxonomy() != null) {
			calculatedValue = Arrays.asList("/" + paramValue.getTaxonomy().getCode());
		}

		Collections.sort(calculatedValue);

		for (int i = 0; i < calculatedValue.size(); i++) {
			String calculatedValueAtI = calculatedValue.get(i);
			if (calculatedValueAtI != null) {
				for (int j = 0; j < calculatedValue.size(); j++) {
					String calculatedValueAtJ = calculatedValue.get(j);
					if (i != j && calculatedValueAtJ != null && calculatedValueAtI.startsWith(calculatedValueAtJ)) {
						calculatedValue.set(j, null);
					}
				}
			}
		}

		return LangUtils.withoutNulls(calculatedValue);
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(taxonomiesParam, idDependency);
	}
}
