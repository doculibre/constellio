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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class UserTokensCalculator2 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency
			.toAStringList(User.ALL_USER_AUTHORIZATIONS);
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return TokensCalculator2.getTokensForAuthorizationIds(parameters.get(allAuthorizationsParam),
				parameters.get(manualTokensParam));
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
		return Arrays.asList(allAuthorizationsParam, manualTokensParam);
	}

}
