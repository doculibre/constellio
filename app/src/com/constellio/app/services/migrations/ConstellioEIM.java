package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.extensions.core.CoreSearchFieldExtension;
import com.constellio.app.services.extensions.core.CoreUserProfileFieldsExtension;
import com.constellio.app.services.factories.AppLayerFactory;
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
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_4_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_14;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_19;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_21;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_22;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_42;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_5_50;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1_3_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_3_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_4_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_2_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_6_45;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_6_9;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_1_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_4_11;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_6;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_7_7;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_0_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_0_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_2_42;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_8_3_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_0_5;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_1;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_2;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_3;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_9_0_1_88;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.ArrayList;
import java.util.List;

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
		scripts.add(new CoreMigrationTo_7_7_0_2());
		scripts.add(new CoreMigrationTo_7_7_1());
		scripts.add(new CoreMigrationTo_7_7_1_2());
		scripts.add(new CoreMigrationTo_7_7_1_6());
		scripts.add(new CoreMigrationTo_7_7_2());
		scripts.add(new CoreMigrationTo_7_7_4());
		scripts.add(new CoreMigrationTo_7_7_4_11());
		scripts.add(new CoreMigrationTo_7_7_5());
		scripts.add(new CoreMigrationTo_7_7_6());
		scripts.add(new CoreMigrationTo_7_7_7());
		scripts.add(new CoreMigrationTo_8_0());
		scripts.add(new CoreMigrationTo_8_0_1());
		scripts.add(new CoreMigrationTo_8_0_2());
		scripts.add(new CoreMigrationTo_8_1());
		scripts.add(new CoreMigrationTo_8_1_0_1());
		scripts.add(new CoreMigrationTo_8_1_2());
		scripts.add(new CoreMigrationTo_8_1_3());
		scripts.add(new CoreMigrationTo_8_2());
		scripts.add(new CoreMigrationTo_8_2_1());
		scripts.add(new CoreMigrationTo_8_2_1_1());
		scripts.add(new CoreMigrationTo_8_2_3());
		scripts.add(new CoreMigrationTo_8_2_42());
		scripts.add(new CoreMigrationTo_8_3());
		scripts.add(new CoreMigrationTo_8_3_1());
		scripts.add(new CoreMigrationTo_8_3_1_1());
		scripts.add(new CoreMigrationTo_9_0());
		//scripts.add(new CoreMigrationTo_8_3_0_1());
		scripts.add(new CoreMigrationTo_9_0_0_5());
		scripts.add(new CoreMigrationTo_9_0_1_1());
		scripts.add(new CoreMigrationTo_9_0_1_2());
		scripts.add(new CoreMigrationTo_9_0_1_3());
		scripts.add(new CoreMigrationTo_9_0_1_88());
		return scripts;
	}

	public List<SystemConfiguration> getConfigurations() {
		return ConstellioEIMConfigs.configurations;
	}

	static public void start(AppLayerFactory appLayerFactory, String collection) {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			configureBaseExtensions(appLayerFactory, collection);
		}
	}

	private static void configureBaseExtensions(AppLayerFactory appLayerFactory, String collection) {
		configureBaseAppLayerExtensions(appLayerFactory, collection);
		configureBaseModelLayerExtensions(appLayerFactory, collection);
		configureBaseDataLayerExtensions(appLayerFactory, collection);
	}

	private static void configureBaseAppLayerExtensions(AppLayerFactory appLayerFactory, String collection) {
		appLayerFactory.getExtensions().forCollection(collection)
				.pagesComponentsExtensions.add(new CoreUserProfileFieldsExtension(collection, appLayerFactory));
	}

	private static void configureBaseModelLayerExtensions(AppLayerFactory appLayerFactory, String collection) {
		appLayerFactory.getModelLayerFactory().getExtensions().forCollection(collection)
				.schemaExtensions.add(new CoreSearchFieldExtension(collection, appLayerFactory));
	}

	private static void configureBaseDataLayerExtensions(AppLayerFactory appLayerFactory, String collection) {
	}
}
