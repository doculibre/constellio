package com.constellio.app.modules.robots;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.extensions.RobotSystemCheckExtension;
import com.constellio.app.modules.robots.migrations.RobotsMigrationCombo;
import com.constellio.app.modules.robots.migrations.RobotsMigrationTo5_1_2;
import com.constellio.app.modules.robots.migrations.RobotsMigrationTo5_1_3;
import com.constellio.app.modules.robots.migrations.RobotsMigrationTo6_3;
import com.constellio.app.modules.robots.model.actions.RunExtractorsActionExecutor;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.modules.robots.ui.navigation.RobotsNavigationConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConstellioRobotsModule implements InstallableSystemModule, ModuleWithComboMigration {
	public static final String ID = "robots";
	public static final String NAME = "Constellio Robots";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return Arrays.asList(
				new RobotsMigrationTo5_1_2(),
				new RobotsMigrationTo5_1_3(),
				new RobotsMigrationTo6_3()
		);
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		RobotsNavigationConfiguration.configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		registerManagers(collection, appLayerFactory);
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		RobotsManager robotsManager = robotSchemas.getRobotsManager();

		RunExtractorsActionExecutor.registerIn(robotsManager);
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

	}

	private void registerManagers(String collection, AppLayerFactory appLayerFactory) {
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		appLayerFactory.registerManager(collection, ConstellioRobotsModule.ID, RobotsManager.ID, new RobotsManager(robotSchemas));
	}

	@Override
	public boolean isComplementary() {
		return false;
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return Collections.unmodifiableList(RobotsConfigs.configurations);
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return RobotsPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		RobotsNavigationConfiguration.configureNavigation(appLayerFactory.getNavigatorConfigurationService());


	}


	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		extensions.systemCheckExtensions.add(new RobotSystemCheckExtension(collection, appLayerFactory));
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}

	@Override
	public ComboMigrationScript getComboMigrationScript() {
		return new RobotsMigrationCombo();
	}
}
