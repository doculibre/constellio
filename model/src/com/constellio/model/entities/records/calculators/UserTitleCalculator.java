package com.constellio.model.entities.records.calculators;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class UserTitleCalculator implements MetadataValueCalculator<String> {

	ConfigDependency<String> titlePatternParam = ConstellioEIMConfigs.USER_TITLE_PATTERN.dependency();
	LocalDependency<String> firstNameParam = LocalDependency.toAString(User.FIRSTNAME);
	LocalDependency<String> lastNameParam = LocalDependency.toAString(User.LASTNAME);
	LocalDependency<String> usernameParam = LocalDependency.toAString(User.USERNAME);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String titlePattern = parameters.get(titlePatternParam);
		String firstName = parameters.get(firstNameParam);
		String lastName = parameters.get(lastNameParam);
		String username = parameters.get(usernameParam);

		firstName = firstName == null ? "" : firstName;
		lastName = lastName == null ? "" : lastName;
		username = username == null ? "" : username;

		titlePattern = titlePattern.replace("${firstName}", firstName).replace("${lastName}", lastName)
				.replace("${username}", username);

		String pattern = ".*[A-Za-z0-9]+.*";
		boolean isValid = titlePattern.matches(pattern);
		if (!isValid) {
			titlePattern = username;
		}
		return titlePattern;
	}

	@Override
	public String getDefaultValue() {
		return null;
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
		return Arrays.asList(titlePatternParam, firstNameParam, lastNameParam, usernameParam);
	}
}
