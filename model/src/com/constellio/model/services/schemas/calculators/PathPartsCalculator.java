package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PathPartsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> pathDependency = LocalDependency.toAStringList("path");

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return getPathsParts(parameters.getId(), parameters.get(pathDependency));
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
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
		return Arrays.asList(pathDependency);
	}

	private List<String> getPathsParts(String id, List<String> paths) {
		Set<String> pathsParts = new HashSet<>();

		for (String path : paths) {
			String last = null;
			if (path != null) {
				String[] splittedPath = path.split("/");
				if (splittedPath.length >= 3) {
					String taxonomyCode = splittedPath[1];
					for (int i = 2; i < splittedPath.length; i++) {
						int level = i - 2;
						String pathAncestorId = splittedPath[i];
						if (!id.equals(pathAncestorId)) {
							pathsParts.add(pathAncestorId);
							last = pathAncestorId;
						}

					}
				}
			}
			if (last != null) {
				pathsParts.add("_LAST_" + last);
			}

		}
		return new ArrayList<>(pathsParts);
	}

}
