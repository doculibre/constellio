package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.InstallableSystemModuleWithRecordMigrations;
import com.constellio.app.entities.modules.Migration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.modules.locators.ModuleResourcesLocator;
import com.constellio.app.entities.modules.locators.PropertiesLocatorFactory;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class MigrationServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigrationServices.class);
	public static final String VERSION_PROPERTIES_FILE = "/version.properties";

	ConfigManager configManager;
	ConstellioEIM constellioEIM;
	AppLayerFactory appLayerFactory;
	ConstellioModulesManagerImpl constellioModulesManager;
	ConstellioPluginManager constellioPluginManager;
	DataLayerFactory dataLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ModuleResourcesLocator moduleResourcesLocator;
	CollectionsManager collectionsManager;

	public MigrationServices(ConstellioEIM constellioEIM, AppLayerFactory appLayerFactory,
							 ConstellioModulesManagerImpl constellioModulesManager,
							 ConstellioPluginManager constellioPluginManager) {
		super();
		this.constellioEIM = constellioEIM;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.dataLayerFactory = modelLayerFactory.getDataLayerFactory();

		this.constellioModulesManager = constellioModulesManager;
		this.constellioPluginManager = constellioPluginManager;
		this.configManager = dataLayerFactory.getConfigManager();
		this.collectionsManager = appLayerFactory.getCollectionsManager();
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

	private List<Migration> getAllMigrationsFor(boolean newCollection, String collection) {
		ConstellioModulesManagerImpl modulesManager = getModulesManager();
		List<Migration> migrations = new ArrayList<>();

		List<InstallableModule> enabledModules, requiredDependentModules;
		enabledModules = modulesManager.getEnabledModules(collection);
		requiredDependentModules = modulesManager.getRequiredDependentModulesToInstall(collection);
		List<InstallableModule> modules = new ArrayList<>(enabledModules);

		LOGGER.warn("Enabled modules to install for tenant '" + TenantUtils.getTenantId() + "' : " +
					enabledModules.stream().map((m) -> m.getId()).collect(toList()));

		LOGGER.warn("Required dependent modules to install for tenant '" + TenantUtils.getTenantId() + "' : " +
					requiredDependentModules.stream().map((m) -> m.getId()).collect(toList()));
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

		LOGGER.warn("Modules to install for tenant '" + TenantUtils.getTenantId() + "' : " +
					modules.stream().map((m) -> m.getId()).collect(toList()));
		for (InstallableModule module : modules) {
			boolean useComboMigration = newCollection && module instanceof ModuleWithComboMigration;
			if (useComboMigration) {
				ComboMigrationScript comboMigrationScript = ((ModuleWithComboMigration) module).getComboMigrationScript();

				List<String> completedMigrations = getCompletedMigrations(collection);
				for (MigrationScript aMigrationScriptIncludedInCombo : comboMigrationScript.getVersions()) {

					if (completedMigrations.contains(
							new Migration(collection, module.getId(), aMigrationScriptIncludedInCombo).getMigrationId())) {
						useComboMigration = false;
						break;
					}
				}
			}
			if (useComboMigration) {
				ComboMigrationScript comboMigrationScript = ((ModuleWithComboMigration) module).getComboMigrationScript();
				migrations.add(new Migration(collection, module.getId(), comboMigrationScript));

				for (MigrationScript migrationScript : getMigrationScripts(module)) {

					boolean found = false;
					for (MigrationScript migrationScriptInCombo : comboMigrationScript.getVersions()) {
						if (migrationScriptInCombo.getClass().equals(migrationScript.getClass())) {
							found = true;
							break;
						}
					}

					if (!found) {
						migrations.add(new Migration(collection, module.getId(), migrationScript));
					}

				}

			} else {
				for (MigrationScript script : getMigrationScripts(module)) {
					migrations.add(new Migration(collection, module.getId(), script));
				}
			}
		}

		for (
				MigrationScript script
				: constellioEIM.getMigrationScripts()) {
			migrations.add(new Migration(collection, null, script));
		}

		Collections.sort(migrations, MigrationScriptsComparator.forModules(modules));
		return migrations;
	}

	public List<RecordMigrationScript> getAllRecordMigrationScripts(String collection) {
		ConstellioModulesManagerImpl modulesManager = getModulesManager();
		List<RecordMigrationScript> migrations = new ArrayList<>();

		List<InstallableModule> enabledModules = modulesManager.getEnabledModules(collection);
		List<InstallableModule> modules = new ArrayList<>(enabledModules);

		for (InstallableModule module : modules) {
			if (module instanceof InstallableSystemModuleWithRecordMigrations) {
				for (RecordMigrationScript script : ((InstallableSystemModuleWithRecordMigrations) module).
						getRecordMigrationScripts(collection, appLayerFactory)) {
					migrations.add(script);
				}
			}
		}

		//		for (MigrationScript script : constellioEIM.getMigrationScripts()) {
		//			migrations.add(new Migration(collection, null, script));
		//		}

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

	public void migrate(String toVersion, boolean newModule)
			throws ConstellioModulesManagerException_ModuleInstallationFailed, OptimisticLockingConfiguration {

		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		if(collections.contains(SYSTEM_COLLECTION)) {
			collections = new ArrayList<>(collections);
			collections.remove(SYSTEM_COLLECTION);
			collections.add(0, SYSTEM_COLLECTION);
		}


		for (String collection : collections) {
			migrate(collection, toVersion, newModule);
		}
	}

	private void migrateModules(String collection, String toVersion, boolean newModule)
			throws ConstellioModulesManagerException_ModuleInstallationFailed, OptimisticLockingConfiguration {
		List<String> collectionCodes = collectionsManager.getCollectionCodesExcludingSystem();
		boolean newCollection = isNewCollection(collection);
		if (newCollection && appLayerFactory.getAppLayerConfiguration().isFastMigrationsEnabled() &&
			(!SYSTEM_COLLECTION.equals(collection) || collectionCodes.isEmpty())) {
			migrate(collection, null, new CoreMigrationCombo());
		}

		List<Migration> migrations;

		boolean firstMigration = true;
		boolean fastMigrationEnabled = appLayerFactory.getAppLayerConfiguration().isFastMigrationsEnabled();
		while (!(migrations = filterRunnedMigration(collection,
				getAllMigrationsFor(newModule && fastMigrationEnabled, collection))).isEmpty()) {

			LOGGER.info("Migrating collection " + collection + " : " + migrations);
			for (Migration migration : migrations) {
				if (toVersion == null || VersionsComparator
						.isFirstVersionBeforeOrEqualToSecond(migration.getVersion(), toVersion)) {

					if (firstMigration) {
						ensureSchemasHaveCommonMetadata(collection, 0);
						firstMigration = false;
					}

					migrateWithoutException(migration, collection);
				}
			}
		}
	}

	private void migrateWithoutException(ComboMigrationScript migration, String moduleId, String collection) {
		try {
			migrate(collection, moduleId, migration);
		} catch (Throwable e) {
			constellioPluginManager
					.handleModuleNotMigratedCorrectly(moduleId, collection, e);
		}
	}

	private void migrateWithoutException(Migration migration, String collection)
			throws ConstellioModulesManagerException_ModuleInstallationFailed {
		try {
			if (migration.getScript() instanceof ComboMigrationScript) {
				migrate(collection, migration.getModuleId(), (ComboMigrationScript) migration.getScript());
			} else {
				migrate(migration);
			}
		} catch (Throwable e) {
			if (!dataLayerFactory.getTransactionLogXmlRecoveryManager().isInRollbackMode()) {
				constellioPluginManager
						.handleModuleNotMigratedCorrectly(migration.getModuleId(), collection, e);

				throw new ConstellioModulesManagerException_ModuleInstallationFailed(migration.getModuleId(), migration.getCollection(), e, true);

			} else {
				throw new ConstellioModulesManagerException_ModuleInstallationFailed(migration.getModuleId(), migration.getCollection(), e, false);
			}

		}
	}

	public void migrate(String collection, String toVersion, boolean newModule)
			throws OptimisticLockingConfiguration, ConstellioModulesManagerException_ModuleInstallationFailed {
		migrateModules(collection, toVersion, newModule);
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

			List<String> runnedMigrations = getCompletedMigrations(collection);

			List<Migration> filteredMigrations = new ArrayList<>();
			for (Migration migration : migrations) {
				if (!runnedMigrations.contains(migration.getMigrationId())) {
					filteredMigrations.add(migration);
				}
			}
			return filteredMigrations;
		} else {
			return migrations;
		}

	}

	private void ensureSchemasHaveCommonMetadata(String collection, int attempt) {
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = manager.modify(collection);

		new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(types);
		try {
			manager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking e) {
			if (attempt == 10) {
				throw new RuntimeException(e);
			} else {
				ensureSchemasHaveCommonMetadata(collection, attempt + 1);
			}
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
		List<Language> languages = Language.withCodes(collectionsManager.getCollectionLanguages(migration.getCollection()));
		MigrationResourcesProvider migrationResourcesProvider = new MigrationResourcesProvider(moduleId, language, languages,
				version, ioServices, moduleResourcesLocator);

		try {
			script.migrate(migration.getCollection(), migrationResourcesProvider, appLayerFactory);
		} catch (Exception e) {
			throw new RuntimeException("Error when migrating collection '" + migration.getCollection() + "'", e);
		}
		setCurrentDataVersion(migration.getCollection(), migration.getVersion());
		markMigrationAsCompleted(migration);
	}

	String getHighestVersion(ComboMigrationScript script) {
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

	void migrate(String collectionId, String moduleId, ComboMigrationScript fastMigrationScript)
			throws OptimisticLockingConfiguration {

		String highestVersion = getHighestVersion(fastMigrationScript);
		LOGGER.info("Running migration script '" + fastMigrationScript.getClass().getSimpleName() +
					"' updating to version '" + highestVersion + "'");
		IOServices ioServices = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices();
		Language language = Language.withCode(modelLayerFactory.getConfiguration().getMainDataLanguage());
		List<Language> languages = Language.withCodes(collectionsManager.getCollectionLanguages(collectionId));
		moduleId = moduleId == null ? "core" : moduleId;

		MigrationResourcesProvider migrationResourcesProvider = new MigrationResourcesProvider(moduleId, language, languages,
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

	public List<String> getCompletedMigrations(String collection) {

		Map<String, String> properties = dataLayerFactory.getConfigManager()
				.getProperties(VERSION_PROPERTIES_FILE).getProperties();
		String completedMigrations = properties.get(collection + "_completedMigrations");
		if (StringUtils.isNotBlank(completedMigrations)) {
			return asList(completedMigrations.split(","));
		} else {
			return Collections.emptyList();
		}

	}

	public void markMigrationAsCompleted(final Migration migration)
			throws OptimisticLockingConfiguration {
		final String propertyKey = migration.getCollection() + "_completedMigrations";

		dataLayerFactory.getConfigManager().updateProperties(VERSION_PROPERTIES_FILE, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				String completedMigrations = properties.get(propertyKey);
				List<String> migrations = new ArrayList<String>();

				if (StringUtils.isNotBlank(completedMigrations)) {
					migrations.addAll(asList(completedMigrations.split(",")));
				}
				migrations.add(migration.getMigrationId());
				Collections.sort(migrations);

				properties.put(propertyKey, StringUtils.join(migrations, ","));
			}
		});
	}

	private void updateVersionOfExistancePropertiesFile(String collection, String version)
			throws OptimisticLockingConfiguration {
		Map<String, String> properties;
		properties = dataLayerFactory.getConfigManager().getProperties(VERSION_PROPERTIES_FILE).getProperties();

		String currentVersion = properties.get(collection + "_version");
		if (currentVersion == null || VersionsComparator.isFirstVersionBeforeSecond(currentVersion, version)) {
			properties.put(collection + "_version", version);
			dataLayerFactory.getConfigManager().update(VERSION_PROPERTIES_FILE,
					dataLayerFactory.getConfigManager().getProperties(VERSION_PROPERTIES_FILE).getHash(), properties);
		}
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
