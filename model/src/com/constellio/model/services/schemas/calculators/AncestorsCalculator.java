package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.substringAfter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

public class AncestorsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<String> principalPathParam = LocalDependency.toAString(Schemas.PRINCIPAL_PATH.getLocalCode());

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		String path = parameters.get(principalPathParam);

		List<String> ancestors = new ArrayList<>();
		if (path != null) {
			String[] splittedPath = path.split("/");
			if (splittedPath.length >= 3) {
				String taxonomyCode = splittedPath[1];
				for (int i = 2; i < splittedPath.length; i++) {
					String id = splittedPath[i];
					if (!parameters.getId().equals(id)) {
						ancestors.add(id);
					}

				}
			}
		}
		return ancestors;
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
		return asList(principalPathParam);
	}
}
