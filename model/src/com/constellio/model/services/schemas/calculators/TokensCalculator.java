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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class TokensCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency.toARequiredStringList("allauthorizations");

	public static List<String> getTokensForAuthorizationIds(List<String> authorizationIds) {
		List<String> calculatedTokens = new ArrayList<>();
		for (String auth : authorizationIds) {
			if (!auth.startsWith("-")) {
				String[] authSplitted = auth.split("_");
				String accessCode = authSplitted[0];
				String roles = authSplitted[1];
				String authId = authSplitted[2];
				if (accessCode.length() == 1) {
					calculatedTokens.add(accessCode + "_" + roles + "_" + authId);
				} else {
					for (int i = 0; i < accessCode.length(); i++) {
						calculatedTokens.add(accessCode.charAt(i) + "_" + roles + "_" + authId);
					}
				}
			}
		}
		return calculatedTokens;
	}

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return getTokensForAuthorizationIds(parameters.get(allAuthorizationsParam));
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(allAuthorizationsParam);
	}
}
