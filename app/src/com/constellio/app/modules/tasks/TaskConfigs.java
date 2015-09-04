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
package com.constellio.app.modules.tasks;

import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.ConstellioRMModule.ID;

public class TaskConfigs {

	static List<SystemConfiguration> configurations = new ArrayList<>();


	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public TaskConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

}
