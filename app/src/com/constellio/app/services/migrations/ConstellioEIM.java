package com.constellio.app.services.migrations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.migrations.scripts.*;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

public class ConstellioEIM {

	public List<MigrationScript> getMigrationScripts() {

		List<MigrationScript> scripts = new ArrayList<>();

		scripts.add(new CoreMigrationTo_5_0_1());
		scripts.add(new CoreMigrationTo_5_0_4());
		scripts.add(new CoreMigrationTo_5_0_5());
		scripts.add(new CoreMigrationTo_5_0_6_6());
		scripts.add(new CoreMigrationTo_5_0_7());
		scripts.add(new CoreMigrationTo_5_1_0());
		scripts.add(new CoreMigrationTo_5_1_1_3());
		scripts.add(new CoreMigrationTo_5_1_2());
		scripts.add(new CoreMigrationTo_5_1_3());
		scripts.add(new CoreMigrationTo_5_1_4());
		scripts.add(new CoreMigrationTo_5_1_6());
		scripts.add(new CoreMigrationTo_5_1_7());
		scripts.add(new CoreMigrationTo_5_2());
		scripts.add(new CoreMigrationTo_6_0());
		scripts.add(new CoreMigrationTo_6_1());
		scripts.add(new CoreMigrationTo_6_3());
		scripts.add(new CoreMigrationTo_6_4());
		scripts.add(new CoreMigrationTo_6_4_1());
		scripts.add(new CoreMigrationTo_6_5());
		scripts.add(new CoreMigrationTo_6_5_14());
		scripts.add(new CoreMigrationTo_6_5_19());
		scripts.add(new CoreMigrationTo_6_5_21());
		scripts.add(new CoreMigrationTo_6_5_50());
		scripts.add(new CoreMigrationTo_6_5_22());
		scripts.add(new CoreMigrationTo_6_5_42());
		scripts.add(new CoreMigrationTo_6_6());
		scripts.add(new CoreMigrationTo_7_0());
		scripts.add(new CoreMigrationTo_7_0_1());
		scripts.add(new CoreMigrationTo_7_1());
		scripts.add(new CoreMigrationTo_7_1_1());
		scripts.add(new CoreMigrationTo_7_1_3_1());
		scripts.add(new CoreMigrationTo_7_2());
		scripts.add(new CoreMigrationTo_7_3());
		scripts.add(new CoreMigrationTo_7_3_0_1());
		scripts.add(new CoreMigrationTo_7_4());
		scripts.add(new CoreMigrationTo_7_4_2());
		scripts.add(new CoreMigrationTo_7_4_3());
		scripts.add(new CoreMigrationTo_7_5());
		scripts.add(new CoreMigrationTo_7_6());
		scripts.add(new CoreMigrationTo_7_6_2());
		scripts.add(new CoreMigrationTo_7_6_2_1());
		scripts.add(new CoreMigrationTo_7_6_6());
		scripts.add(new CoreMigrationTo_7_6_6_45());
		scripts.add(new CoreMigrationTo_7_6_9());
		scripts.add(new CoreMigrationTo_7_7_0_1());
		scripts.add(new CoreMigrationTo_7_7_0_2());
		scripts.add(new CoreMigrationTo_7_7_1());
		scripts.add(new CoreMigrationTo_7_7_2());
		scripts.add(new CoreMigrationTo_7_7_4());
		scripts.add(new CoreMigrationTo_7_7_5());

		return scripts;
	}

	public List<SystemConfiguration> getConfigurations() {
		return ConstellioEIMConfigs.configurations;
	}
}
