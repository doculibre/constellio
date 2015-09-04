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
package com.constellio.app.services.migrations;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_0_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_0;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class ConstellioEIM {
	public List<MigrationScript> getMigrationScripts() {
		return Arrays.asList(
				new CoreMigrationTo_5_0_1(),
				new CoreMigrationTo_5_0_4(),
				new CoreMigrationTo_5_0_5(),
				new CoreMigrationTo_5_0_6_6(),
				new CoreMigrationTo_5_0_7(),
				new CoreMigrationTo_5_1_0()
		);
	}

	public List<SystemConfiguration> getConfigurations() {
		return ConstellioEIMConfigs.configurations;
	}
}
