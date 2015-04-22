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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.Migration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.services.factories.ModelLayerFactory;

public class MigrationServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationServices.class);
	private static final String VERSION_PROPERTIES_FILE = "/version.properties";

	ConfigManager configManager;
	ConstellioEIM constellioEIM;
	AppLayerFactory appLayerFactory;
	ConstellioModulesManagerImpl constellioModulesManager;
	ConstellioPluginManager constellioPluginManager;
	DataLayerFactory dataLayerFactory;
	ModelLayerFactory modelLayerFactory;

	public MigrationServices(ConstellioEIM constellioEIM, AppLayerFactory appLayerFactory,
			ConstellioModulesManagerImpl constellioModulesManager, ConstellioPluginManager constellioPluginManager) {
		super();
		this.constellioEIM = constellioEIM;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();

		this.constellioModulesManager = constellioModulesManager;
		this.constellioPluginManager = constellioPluginManager;
		this.configManager = dataLayerFactory.getConfigManager();
	}

	private void addPropertiesFileWithVersion(String collection, String version, Map<String, String> properties) {
		properties.put(collection + "_version", version);
		configManager.add(VERSION_PROPERTIES_FILE, properties);
	}

	public String getCurrentVersion(String collection) {
		PropertiesConfiguration propertiesConfiguration = configManager.getProperties(VERSION_PROPERTIES_FILE);
		return propertiesConfiguration == null ? null : propertiesConfiguration.getProperties().get(collection + "_version");
	}

	private List<Migration> getAllMigrationsFor(String collection) {
		List<Migration> migrations = new ArrayList<>();

		List<Module> modules = getModulesManager().getInstalledModules();
		for (Module module : modules) {
			for (MigrationScript script : ((InstallableModule) module).getMigrationScripts()) {
				migrations.add(new Migration(collection, module.getId(), script));
			}
		}

		for (MigrationScript script : constellioEIM.getMigrationScripts()) {
			migrations.add(new Migration(collection, null, script));
		}

		LOGGER.info("All required migrations : " + migrations);
		Collections.sort(migrations, MigrationScriptsComparator.forModules(modules));
		LOGGER.info("All required migrations (sorted) : " + migrations);
		return migrations;
	}

	public void migrate(String toVersion)
			throws OptimisticLockingConfiguration {

		LOGGER.info("Migrating collections to current version");
		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		for (String collection : collections) {
			LOGGER.info("Migrating collection " + collection + " to current version");
			migrate(collection, toVersion);
		}
	}

	public void migrate(String collection, String toVersion)
			throws OptimisticLockingConfiguration {

		List<Migration> migrations = getAllMigrationsFor(collection);
		LOGGER.info("Detected migrations : " + migrations);

		List<Migration> filteredMigrations = filterRunnedMigration(collection, migrations);
		LOGGER.info("Filtered migrations : " + filteredMigrations);

		for (Migration migration : filteredMigrations) {
			if (toVersion == null || VersionsComparator.isFirstVersionBeforeOrEqualToSecond(migration.getVersion(), toVersion)) {
				migrate(migration);
			}
		}

	}

	List<Migration> filterRunnedMigration(String collection, List<Migration> migrations) {
		if (configManager.exist(VERSION_PROPERTIES_FILE)) {
			String propertyKey = collection + "_completedMigrations";
			String completedMigrations = dataLayerFactory.getConfigManager().getProperties(VERSION_PROPERTIES_FILE)
					.getProperties().get(propertyKey);

			if (completedMigrations == null) {
				return migrations;
			} else {
				List<Migration> filteredMigrations = new ArrayList<>();
				for (Migration migration : migrations) {
					if (!completedMigrations.contains(migration.getMigrationId())) {
						filteredMigrations.add(migration);
					}
				}
				return filteredMigrations;
			}
		} else {
			return migrations;
		}

	}

	void migrate(Migration migration)
			throws OptimisticLockingConfiguration {

		MigrationScript script = migration.getScript();
		LOGGER.info("Running migration script '" + script.getClass().getSimpleName() +
				"' updating to version '" + script.getVersion() + "'");
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		MigrationResourcesProvider migrationResourcesProvider = new MigrationResourcesProvider(
				migration.getModuleId() == null ? "core" : migration.getModuleId(), migration.getVersion(), null, ioServices);
		script.migrate(migration.getCollection(), migrationResourcesProvider, appLayerFactory);
		setCurrentDataVersion(migration.getCollection(), migration.getVersion());
		markMigrationAsCompleted(migration);
	}

	public void setCurrentDataVersion(String collection, String version)
			throws OptimisticLockingConfiguration {
		Map<String, String> properties = new HashMap<String, String>();

		if (dataLayerFactory.getConfigManager().exist(VERSION_PROPERTIES_FILE)) {
			updateVersionOfExistancePropertiesFile(collection, version);

		} else {
			addPropertiesFileWithVersion(collection, version, properties);
		}
	}

	public void markMigrationAsCompleted(final Migration migration)
			throws OptimisticLockingConfiguration {
		final String propertyKey = migration.getCollection() + "_completedMigrations";

		dataLayerFactory.getConfigManager().updateProperties(VERSION_PROPERTIES_FILE, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				String completedMigrations = properties.get(propertyKey);
				properties.put(propertyKey, completedMigrations + "," + migration.getMigrationId());
			}
		});
	}

	private void updateVersionOfExistancePropertiesFile(String collection, String version)
			throws OptimisticLockingConfiguration {
		Map<String, String> properties;
		properties = dataLayerFactory.getConfigManager().getProperties(VERSION_PROPERTIES_FILE).getProperties();
		properties.put(collection + "_version", version);
		dataLayerFactory.getConfigManager().update(VERSION_PROPERTIES_FILE,
				dataLayerFactory.getConfigManager().getProperties(VERSION_PROPERTIES_FILE).getHash(), properties);
	}

	private ConstellioPluginManager getPluginManager() {
		return constellioPluginManager;
	}

	private ConstellioModulesManagerImpl getModulesManager() {
		return constellioModulesManager;
	}

}
