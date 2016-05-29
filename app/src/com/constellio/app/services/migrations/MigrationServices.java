package com.constellio.app.services.migrations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.FastMigrationScript;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.Migration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.locators.ModuleResourcesLocator;
import com.constellio.app.entities.modules.locators.PropertiesLocatorFactory;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

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
	ModuleResourcesLocator moduleResourcesLocator;

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
		this.moduleResourcesLocator = PropertiesLocatorFactory.get();
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
		ConstellioModulesManagerImpl modulesManager = getModulesManager();
		List<Migration> migrations = new ArrayList<>();

		List<InstallableModule> enabledModules, requiredDependentModules;
		enabledModules = modulesManager.getEnabledModules(collection);
		requiredDependentModules = modulesManager.getRequiredDependentModulesToInstall(collection);
		List<InstallableModule> modules = new ArrayList<>(enabledModules);

		for (InstallableModule installableModule : requiredDependentModules) {

			if (!modulesManager.isInstalled(installableModule)) {
				modulesManager.markAsInstalled(installableModule,
						appLayerFactory.getModelLayerFactory().getCollectionsListManager());
			}
			if (!modulesManager.isModuleEnabled(collection, installableModule)) {
				modulesManager.markAsEnabled(installableModule, collection);
			}
			modules.add(installableModule);
		}

		for (InstallableModule module : modules) {
			for (MigrationScript script : getMigrationScripts(module)) {
				migrations.add(new Migration(collection, module.getId(), script));
			}
		}

		for (MigrationScript script : constellioEIM.getMigrationScripts()) {
			migrations.add(new Migration(collection, null, script));
		}

		Collections.sort(migrations, MigrationScriptsComparator.forModules(modules));
		return migrations;
	}

	private List<MigrationScript> getMigrationScripts(InstallableModule module) {
		List<MigrationScript> returnList = new ArrayList<>();
		try {
			returnList = module.getMigrationScripts();
		} catch (Throwable e) {
			LOGGER.warn("Error when trying get module " + module.getId() + " migration scripts");
		}
		return returnList;
	}

	public Set<String> migrate(String toVersion)
			throws OptimisticLockingConfiguration {
		Set<String> modulesNotMigratedCorrectly = new HashSet<>();

		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		for (String collection : collections) {
			modulesNotMigratedCorrectly.addAll(migrate(collection, toVersion));
		}
		return modulesNotMigratedCorrectly;
	}

	private Set<String> migrateModules(String collection, String toVersion)
			throws OptimisticLockingConfiguration {
		Set<String> modulesNotMigratedCorrectly = new HashSet<>();

		if (isNewCollection(collection) && appLayerFactory.getAppLayerConfiguration().isFastMigrationsEnabled()) {
			migrateWithoutException(new CoreMigrationCombo(), null, collection);
		}

		List<Migration> migrations = getAllMigrationsFor(collection);

		List<Migration> filteredMigrations = filterRunnedMigration(collection, migrations);

		boolean firstMigration = true;

		for (Migration migration : filteredMigrations) {
			if (toVersion == null || VersionsComparator.isFirstVersionBeforeOrEqualToSecond(migration.getVersion(), toVersion)) {

				if (firstMigration) {
					ensureSchemasHaveCommonMetadata(collection);
					firstMigration = false;
				}
				boolean exceptionWhenMigrating = migrateWithoutException(migration, collection);
				if (exceptionWhenMigrating) {
					modulesNotMigratedCorrectly.add(migration.getMigrationId());
				}
			}
		}
		return modulesNotMigratedCorrectly;
	}

	private boolean migrateWithoutException(FastMigrationScript migration, String moduleId, String collection) {
		boolean exceptionWhenMigrating = false;
		try {
			migrate(collection, moduleId, migration);
		} catch (Throwable e) {
			constellioPluginManager
					.handleModuleNotMigratedCorrectly(moduleId, collection, e);
			exceptionWhenMigrating = true;
		}
		return exceptionWhenMigrating;
	}

	private boolean migrateWithoutException(Migration migration, String collection) {
		boolean exceptionWhenMigrating = false;
		try {
			migrate(migration);
		} catch (Throwable e) {
			constellioPluginManager
					.handleModuleNotMigratedCorrectly(migration.getModuleId(), collection, e);
			exceptionWhenMigrating = true;
		}
		return exceptionWhenMigrating;
	}

	public Set<String> migrate(String collection, String toVersion)
			throws OptimisticLockingConfiguration {
		return migrateModules(collection, toVersion);
	}

	boolean isNewCollection(String collection) {
		if (configManager.exist(VERSION_PROPERTIES_FILE)) {
			String propertyKey = collection + "_completedMigrations";
			String completedMigrations = configManager.getProperties(VERSION_PROPERTIES_FILE).getProperties().get(propertyKey);
			return StringUtils.isBlank(completedMigrations);
		} else {
			return true;
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

	private void ensureSchemasHaveCommonMetadata(String collection) {
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = manager.modify(collection);
		new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(types);
		try {
			manager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking e) {
			ensureSchemasHaveCommonMetadata(collection);
		}
	}

	void migrate(Migration migration)
			throws OptimisticLockingConfiguration {

		MigrationScript script = migration.getScript();
		LOGGER.info("Running migration script '" + script.getClass().getSimpleName() +
				"' updating to version '" + script.getVersion() + "'");
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		Language language = Language.withCode(modelLayerFactory.getConfiguration().getMainDataLanguage());
		String moduleId = migration.getModuleId() == null ? "core" : migration.getModuleId();
		String version = migration.getVersion();
		MigrationResourcesProvider migrationResourcesProvider = new MigrationResourcesProvider(moduleId, language,
				version, ioServices, moduleResourcesLocator);

		try {
			script.migrate(migration.getCollection(), migrationResourcesProvider, appLayerFactory);
		} catch (Exception e) {
			throw new RuntimeException("Error when migrating collection '" + migration.getCollection() + "'", e);
		}
		setCurrentDataVersion(migration.getCollection(), migration.getVersion());
		markMigrationAsCompleted(migration);
	}

	String getHighestVersion(FastMigrationScript script) {
		List<String> versions = new ArrayList<>();

		if (script.getVersions().isEmpty()) {
			return "1.0";
		}

		for (MigrationScript migration : script.getVersions()) {
			versions.add(migration.getVersion());
		}

		Collections.sort(versions, new VersionsComparator());
		return versions.get(versions.size() - 1);
	}

	void migrate(String collectionId, String moduleId, FastMigrationScript fastMigrationScript)
			throws OptimisticLockingConfiguration {

		String highestVersion = getHighestVersion(fastMigrationScript);
		LOGGER.info("Running migration script '" + fastMigrationScript.getClass().getSimpleName() +
				"' updating to version '" + highestVersion + "'");
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		Language language = Language.withCode(modelLayerFactory.getConfiguration().getMainDataLanguage());
		moduleId = moduleId == null ? "core" : moduleId;

		MigrationResourcesProvider migrationResourcesProvider = new MigrationResourcesProvider(moduleId, language,
				"combo", ioServices, moduleResourcesLocator);

		try {
			fastMigrationScript.migrate(collectionId, migrationResourcesProvider, appLayerFactory);
		} catch (Exception e) {
			throw new RuntimeException("Error when migrating collection '" + collectionId + "'", e);
		}
		setCurrentDataVersion(collectionId, highestVersion);
		for (MigrationScript migrationScript : fastMigrationScript.getVersions()) {
			markMigrationAsCompleted(new Migration(collectionId, moduleId, migrationScript));
		}
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

	public static class InvalidPluginModule extends Exception {
		final private List<InstallableModule> invalidModules;

		public InvalidPluginModule(InstallableModule... invalidModules) {
			this.invalidModules = new ArrayList<>();
			for (InstallableModule invalidModule : invalidModules) {
				this.invalidModules.add(invalidModule);
			}
		}

		public List<InstallableModule> getInvalidModules() {
			return invalidModules;
		}
	}
}
