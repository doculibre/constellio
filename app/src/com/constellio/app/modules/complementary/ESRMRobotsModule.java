package com.constellio.app.modules.complementary;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorDocumentInFolderActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderDirectlyInThePlanActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInParentFolderActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInTaxonomyActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.extensions.EsRmRobotsActionParametersFieldFactoryExtension;
import com.constellio.app.modules.complementary.esRmRobots.extensions.EsRmRobotsMappingExtension;
import com.constellio.app.modules.complementary.esRmRobots.migrations.*;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.extensions.api.ESModuleExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ESRMRobotsModule implements InstallableModule, ModuleWithComboMigration {
	public static final String ID = "es_rm_robots";

	@Override
	public String getName() {
		return ID;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		List<MigrationScript> migrations = new ArrayList<>();

		migrations.add(new ESRMRobotsMigrationTo5_1_2());
		migrations.add(new ESRMRobotsMigrationTo5_1_5());
		migrations.add(new ESRMRobotsMigrationTo5_1_6());
		migrations.add(new ESRMRobotsMigrationTo5_1_7());
		migrations.add(new ESRMRobotsMigrationTo5_1_9());
		migrations.add(new ESRMRobotsMigrationTo6_0());
		migrations.add(new ESRMRobotsMigrationTo6_1());
		migrations.add(new ESRMRobotsMigrationTo6_2_2_1());
		migrations.add(new ESRMRobotsMigrationTo7_1());

		return migrations;
	}

	@Override
	public void configureNavigation(NavigationConfig config) {

	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		RobotsManager robotsManager = robotSchemas.getRobotsManager();

		ClassifyConnectorDocumentInFolderActionExecutor.registerIn(robotsManager);
		ClassifyConnectorFolderInTaxonomyActionExecutor.registerIn(robotsManager);
		ClassifyConnectorFolderInParentFolderActionExecutor.registerIn(robotsManager);
		ClassifyConnectorFolderDirectlyInThePlanActionExecutor.registerIn(robotsManager);

		setupAppLayerExtensions(collection, appLayerFactory);
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public boolean isComplementary() {
		return true;
	}

	@Override
	public List<String> getDependencies() {
		return asList(ConstellioRMModule.ID, ConstellioESModule.ID, ConstellioRobotsModule.ID);
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return new ArrayList<>();
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return new HashMap<>();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		extensions.recordFieldFactoryExtensions.add(new EsRmRobotsActionParametersFieldFactoryExtension());

		ESModuleExtensions esExtensions = extensions.forModule(ConstellioESModule.ID);
		esExtensions.connectorMappingExtensions.add(new EsRmRobotsMappingExtension(rm));
	}

	@Override
	public ComboMigrationScript getComboMigrationScript() {
		return new ESRMRobotsMigrationCombo();
	}
}
