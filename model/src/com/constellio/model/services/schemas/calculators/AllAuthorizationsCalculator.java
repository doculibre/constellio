/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class AllAuthorizationsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> authorizationsParam = LocalDependency.toAStringList("authorizations");
	LocalDependency<List<String>> inheritedAuthorizationsParam = LocalDependency.toAStringList("inheritedauthorizations");
	LocalDependency<List<String>> removedAuthorizationsParam = LocalDependency.toAStringList("removedauthorizations");
	LocalDependency<Boolean> detachedAuthorizationsParam = LocalDependency.toABoolean("detachedauthorizations");

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> calculatedAuthorizations = new HashSet<>();
		List<String> authorizations = parameters.get(authorizationsParam);
		List<String> inheritedAuthorizations = parameters.get(inheritedAuthorizationsParam);
		List<String> removedAuthorizations = parameters.get(removedAuthorizationsParam);
		Boolean detachedAuthorizations = parameters.get(detachedAuthorizationsParam);

		if (detachedAuthorizations != null && parameters.get(detachedAuthorizationsParam)) {
			calculatedAuthorizations.addAll(authorizations);
		} else {
			calculatedAuthorizations.addAll(inheritedAuthorizations);
			calculatedAuthorizations.addAll(authorizations);
			calculatedAuthorizations.removeAll(removedAuthorizations);
		}
		return new ArrayList<>(calculatedAuthorizations);
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
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
		return Arrays.asList(authorizationsParam, inheritedAuthorizationsParam, removedAuthorizationsParam,
				detachedAuthorizationsParam);
	}
}
