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
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_1_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_5_2;
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
				new CoreMigrationTo_5_1_0(),
				new CoreMigrationTo_5_1_1_3(),
				new CoreMigrationTo_5_1_2(),
				new CoreMigrationTo_5_1_3(),
				new CoreMigrationTo_5_1_4(),
				new CoreMigrationTo_5_1_6(),
				new CoreMigrationTo_5_1_7(),
				new CoreMigrationTo_5_2()
		);
	}

	public List<SystemConfiguration> getConfigurations() {
		return ConstellioEIMConfigs.configurations;
	}
}
