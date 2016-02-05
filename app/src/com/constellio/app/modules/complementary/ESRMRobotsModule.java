package com.constellio.app.modules.complementary;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorDocumentInFolderActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorTaxonomyActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.extensions.EsRmRobotsMappingExtension;
import com.constellio.app.modules.complementary.esRmRobots.migrations.ESRMRobotsMigrationTo5_1_2;
import com.constellio.app.modules.complementary.esRmRobots.migrations.ESRMRobotsMigrationTo5_1_5;
import com.constellio.app.modules.complementary.esRmRobots.migrations.ESRMRobotsMigrationTo5_1_6;
import com.constellio.app.modules.complementary.esRmRobots.migrations.ESRMRobotsMigrationTo5_1_7;
import com.constellio.app.modules.complementary.esRmRobots.migrations.ESRMRobotsMigrationTo5_1_9;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.extensions.api.ESModuleExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;

public class ESRMRobotsModule implements InstallableModule {
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
		return asList(new ESRMRobotsMigrationTo5_1_2(), new ESRMRobotsMigrationTo5_1_5(), new ESRMRobotsMigrationTo5_1_6(),
				new ESRMRobotsMigrationTo5_1_7(), new ESRMRobotsMigrationTo5_1_9());
	}

	@Override
	public void configureNavigation(NavigationConfig config) {

	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		RobotSchemaRecordServices robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
		RobotsManager robotsManager = robotSchemas.getRobotsManager();

		ClassifyConnectorDocumentInFolderActionExecutor.registerIn(robotsManager);
		ClassifyConnectorTaxonomyActionExecutor.registerIn(robotsManager);

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

		ESModuleExtensions esExtensions = extensions.forModule(ConstellioESModule.ID);
		esExtensions.connectorMappingExtensions.add(new EsRmRobotsMappingExtension(rm));
	}
}
