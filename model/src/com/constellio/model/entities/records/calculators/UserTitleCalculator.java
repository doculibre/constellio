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

        titlePattern = titlePattern.replace("${firstName}", firstName).replace("${lastName}", lastName);

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
