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
package com.constellio.model.services.migrations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.core.listeners.UserTitlePatternConfigScript;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class ConstellioEIMConfigs {

	private static List<SystemConfiguration> modifiableConfigs = new ArrayList<>();
	public static List<SystemConfiguration> configurations;

	//Retention calendar configs
	public static final SystemConfiguration USER_TITLE_PATTERN, ENTERED_VALUES_OVER_EXTRACTED_VALUES;

	public static final SystemConfiguration USER_ROLES_IN_AUTHORIZATIONS;

	public static final SystemConfiguration LOGO;
	public static final SystemConfiguration  LOGO_LINK;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(null, "others");
		add(USER_TITLE_PATTERN = others.createString("userTitlePattern").scriptedBy(UserTitlePatternConfigScript.class)
				.withDefaultValue("${firstName} ${lastName}"));
		add(ENTERED_VALUES_OVER_EXTRACTED_VALUES = others.createBooleanTrueByDefault("enteredValuesOverExtractedValues"));

		// Associer ou non des rôles utilisateur aux autorisations
		add(USER_ROLES_IN_AUTHORIZATIONS = others.createBooleanFalseByDefault("userRolesInAuthorizations"));

		add(LOGO = others.createBinary("logo"));
		add(LOGO_LINK = others.createString("logoLink", "http://www.constellio.com"));


		configurations = Collections.unmodifiableList(modifiableConfigs);
	}

	static void add(SystemConfiguration configuration) {
		modifiableConfigs.add(configuration);
	}

	SystemConfigurationsManager manager;

	String collection;

	public ConstellioEIMConfigs(SystemConfigurationsManager manager, String collection) {
		this.manager = manager;
		this.collection = collection;
	}

	public boolean isEnteredValuesOverExtractedValues() {
		return manager.getValue(ENTERED_VALUES_OVER_EXTRACTED_VALUES);
	}

	public String getUserTitlePattern() {
		return manager.getValue(USER_TITLE_PATTERN);
	}

	public boolean seeUserRolesInAuthorizations() {
		return manager.getValue(USER_ROLES_IN_AUTHORIZATIONS);
	}

	public static Collection<? extends SystemConfiguration> getCoreConfigs() {
		return configurations;
	}
}
