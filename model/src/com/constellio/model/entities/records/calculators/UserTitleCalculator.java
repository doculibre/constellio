package com.constellio.model.entities.records.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.Arrays;
import java.util.List;

public class UserTitleCalculator extends AbstractMetadataValueCalculator<String> {

	ConfigDependency<String> titlePatternParam = ConstellioEIMConfigs.USER_TITLE_PATTERN.dependency();
	LocalDependency<String> firstNameParam = LocalDependency.toAString(User.FIRSTNAME);
	LocalDependency<String> lastNameParam = LocalDependency.toAString(User.LASTNAME);
	LocalDependency<String> usernameParam = LocalDependency.toAString(User.USERNAME);
	LocalDependency<String> emailParam = LocalDependency.toAString(User.EMAIL);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String titlePattern = parameters.get(titlePatternParam);
		String firstName = parameters.get(firstNameParam);
		String lastName = parameters.get(lastNameParam);
		String username = parameters.get(usernameParam);
		String email = parameters.get(emailParam);

		firstName = firstName == null ? "" : firstName;
		lastName = lastName == null ? "" : lastName;
		username = username == null ? "" : username;
		email = email == null ? "" : email;

		titlePattern = titlePattern.replace("${firstName}", firstName).replace("${lastName}", lastName)
				.replace("${username}", username).replace("${email}", email);

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
		return Arrays.asList(titlePatternParam, firstNameParam, lastNameParam, usernameParam, emailParam);
	}
}
