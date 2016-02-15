package com.constellio.app.services.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.ConstellioModulesManagerRuntimeException_ModuleIsNotInstalled;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.DependencyUtils;

public class ConstellioModulesManagerImpl implements ConstellioModulesManager, StatefulService {
	@SuppressWarnings("unused") private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioModulesManagerImpl.class);
	private static final String FALSE = "false";
	private static final String MODULES_CONFIG_PATH = "/modules.xml";
	private final ConfigManager configManager;
	private final AppLayerFactory appLayerFactory;
	private final Delayed<MigrationServices> migrationServicesDelayed;
	private final ModelLayerFactory modelLayerFactory;
	private final ConstellioPluginManager constellioPluginManager;
	private Set<String> startedModulesInAnyCollections = new HashSet<>();

	public ConstellioModulesManagerImpl(AppLayerFactory appLayerFactory,
			ConstellioPluginManager constellioPluginManager, Delayed<MigrationServices> migrationServicesDelayed) {
		this(appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager(), migrationServicesDelayed,
				appLayerFactory, constellioPluginManager);
	}

	public ConstellioModulesManagerImpl(ConfigManager configManager, Delayed<MigrationServices> migrationServicesDelayed,
			AppLayerFactory appLayerFactory,
			ConstellioPluginManager constellioPluginManager) {
		super();
		this.appLayerFactory = appLayerFactory;
		this.configManager = configManager;
		this.migrationServicesDelayed = migrationServicesDelayed;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.constellioPluginManager = constellioPluginManager;

	}

	@Override
	public void initialize() {
		createModulesConfigFileIfNotExist();
	}

	public List<InstallableModule> getInstalledModules() {
		List<InstallableModule> installedModules = new ArrayList<>();
		XMLConfiguration xmlConfig;
		xmlConfig = configManager.getXML(MODULES_CONFIG_PATH);
		Map<String, Element> moduleElements = parseModulesDocument(xmlConfig.getDocument());
		for (InstallableModule module : getAllModules()) {
			Element moduleElement = moduleElements.get(module.getId());
			if (moduleElement != null) {
				installedModules.add(module);
			}
		}
		return installedModules;
	}

	public List<InstallableModule> getEnabledModules(String collection) {
		List<InstallableModule> enabledModules = new ArrayList<>();

		XMLConfiguration xmlConfig = configManager.getXML(MODULES_CONFIG_PATH);
		Map<String, Element> moduleElements = parseModulesDocument(xmlConfig.getDocument());

		Map<String, Set<String>> dependencies = new HashMap<>();

		for (InstallableModule module : getAllModules()) {
			Element moduleElement = moduleElements.get(module.getId());

			if (moduleElement != null && isEnabled(moduleElement, collection)) {
				enabledModules.add(module);
				dependencies.put(module.getId(), new HashSet<>(module.getDependencies()));
			}
		}

		List<String> moduleIds = new DependencyUtils<String>().sortByDependencyWithoutTieSort(dependencies);

		List<InstallableModule> sortedInstallableModules = new ArrayList<>();
		for (String moduleId : moduleIds) {
			for (InstallableModule enabledModule : enabledModules) {
				if (moduleId.equals(enabledModule.getId())) {
					sortedInstallableModules.add(enabledModule);
				}
			}
		}

		return sortedInstallableModules;
	}

	public List<InstallableModule> getRequiredDependentModulesToInstall(String collection) {
		Set<String> dependentModuleIds = new HashSet<>();

		for (InstallableModule module : getEnabledModules(collection)) {
			dependentModuleIds.addAll(module.getDependencies());
		}

		for (InstallableModule module : getEnabledModules(collection)) {
			dependentModuleIds.remove(module.getId());
		}

		List<InstallableModule> dependentModules = new ArrayList<>();
		for (String dependentModuleId : dependentModuleIds) {
			for (InstallableModule module : getAllModules()) {
				if (module.getId().equals(dependentModuleId)) {
					dependentModules.add(module);
				}
			}
		}

		return dependentModules;
	}

	private boolean isEnabled(Element moduleElement, String collection) {
		boolean enabled = false;
		if (moduleElement != null) {
			String enabledValue = moduleElement.getAttributeValue("enabled_in_collection_" + collection);
			enabled = "true".equals(enabledValue);
		}
		return enabled;
	}

	private Map<String, Element> parseModulesDocument(Document document) {
		Map<String, Element> moduleElements = new HashMap<>();
		for (Element moduleElement : document.getRootElement().getChildren()) {
			moduleElements.put(moduleElement.getAttributeValue("id"), moduleElement);
		}
		return moduleElements;
	}

	public List<InstallableModule> getDisabledModules(String collection) {
		List<InstallableModule> disabledModules = new ArrayList<>();
		XMLConfiguration xmlConfig = configManager.getXML(MODULES_CONFIG_PATH);
		Map<String, Element> moduleElements = parseModulesDocument(xmlConfig.getDocument());
		for (InstallableModule module : getAllModules()) {
			Element moduleElement = moduleElements.get(module.getId());
			if (moduleElement != null && !isEnabled(moduleElement, collection)) {
				disabledModules.add(module);
			}
		}
		return disabledModules;
	}

	public List<InstallableModule> getModulesAvailableForInstallation() {
		List<InstallableModule> availableModules = new ArrayList<>();
		XMLConfiguration xmlConfig = configManager.getXML(MODULES_CONFIG_PATH);
		Map<String, Element> moduleElements = parseModulesDocument(xmlConfig.getDocument());

		for (InstallableModule module : getAllModules()) {
			if (!moduleElements.containsKey(module.getId())) {
				availableModules.add(module);
			}
		}
		return availableModules;
	}

	@Override
	public List<InstallableModule> getAllModules() {
		return constellioPluginManager.getRegistredModulesAndActivePlugins();
	}

	@Override
	public List<InstallableModule> getBuiltinModules() {
		return constellioPluginManager.getRegisteredModules();
	}

	@Override
	public <T> Class<T> getModuleClass(String name)
			throws ClassNotFoundException {
		return constellioPluginManager.getModuleClass(name);
	}

	public void markAsInstalled(final Module module, CollectionsListManager collectionsListManager) {
		for (String dependentModuleId : getDependencies(module)) {
			InstallableModule dependentModule = getInstalledModule(dependentModuleId);
			if (!isInstalled(dependentModule)) {
				addModuleInConfigFile(dependentModule);
			}
		}
		addModuleInConfigFile(module);
	}

	@Override
	public Set<String> installValidModuleAndGetInvalidOnes(final Module module,
			CollectionsListManager collectionsListManager) {
		Set<String> returnList = new HashSet<>();
		markAsInstalled(module, collectionsListManager);
		MigrationServices migrationServices = migrationServicesDelayed.get();
		for (String collection : collectionsListManager.getCollections()) {
			try {
				returnList.addAll(migrationServices.migrate(collection, null));
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}
		}
		return returnList;
	}

	private void addModuleInConfigFile(final Module module) {
		configManager.updateXML(MODULES_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				final Element moduleElement = new Element("module");
				moduleElement.setAttribute("id", module.getId());
				document.getRootElement().addContent(moduleElement);
			}
		});
	}

	private void createModulesConfigFileIfNotExist() {
		if (!configManager.exist(MODULES_CONFIG_PATH)) {
			configManager.add(MODULES_CONFIG_PATH, new Document().setRootElement(new Element("modules")));
		}
	}

	public boolean isModuleEnabled(String collection, Module module) {
		for (InstallableModule enabledModule : getEnabledModules(collection)) {
			if (enabledModule.getId().equals(module.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<String> enableValidModuleAndGetInvalidOnes(String collection, Module module) {
		markAsEnabled(module, collection);
		Set<String> returnList = applyModuleMigrations(collection);
		if (startModule(collection, module)) {
			if (!module.isComplementary()) {
				returnList.addAll(enableComplementaryModules(collection));
			}
		} else {
			returnList.add(module.getId());
		}
		return returnList;
	}

	private Set<String> enableComplementaryModules(String collection) {
		Set<String> returnList = new HashSet<>();
		List<String> enabledModuleIds = new ArrayList<>();
		for (InstallableModule enabledModule : getEnabledModules(collection)) {
			enabledModuleIds.add(enabledModule.getId());
		}

		for (InstallableModule complementaryModule : getComplementaryModules()) {
			if (enabledModuleIds.containsAll(getDependencies(complementaryModule))) {
				if (!isInstalled(complementaryModule)) {
					returnList.addAll(installValidModuleAndGetInvalidOnes(complementaryModule,
							appLayerFactory.getModelLayerFactory().getCollectionsListManager()));
				}
				if (!isModuleEnabled(collection, complementaryModule)) {
					returnList.addAll(enableValidModuleAndGetInvalidOnes(collection, complementaryModule));
				}
			}
		}
		return returnList;
	}

	private List<String> getDependencies(Module module) {
		return PluginUtil.getDependencies(module);
	}

	public List<InstallableModule> getComplementaryModules() {
		List<InstallableModule> complementaryModules = new ArrayList<>();
		LOGGER.info("Complementary modules");
		List<InstallableModule> allModules = getAllModules();
		for (InstallableModule module : allModules) {

			if (module.isComplementary()) {
				LOGGER.info("Complementary " + module.getId());
				complementaryModules.add(module);
			} else {
				LOGGER.info("Not complementary " + module.getId());
			}
		}
		return complementaryModules;
	}

	private Set<String> applyModuleMigrations(String collection) {

		MigrationServices migrationServices = migrationServicesDelayed.get();

		try {
			return migrationServices.migrate(collection, null);
		} catch (OptimisticLockingConfiguration e) {
			// TODO: Handle this
		}
		return new HashSet<>();
	}

	public void disableModule(String collection, final Module module) {
		stopModule(collection, module);
		disableModuleInConfigFile(collection, module);
	}

	private void disableModuleInConfigFile(final String collection, final Module module) {
		configManager.updateXML(MODULES_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Map<String, Element> moduleElements = parseModulesDocument(document);

				moduleElements.get(module.getId()).setAttribute("enabled_in_collection_" + collection, FALSE);
			}
		});
	}

	public boolean startModule(String collection, Module module) {

		if (module instanceof InstallableSystemModule) {
			if (!startedModulesInAnyCollections.contains(module.getId())) {
				((InstallableSystemModule) module).start(appLayerFactory);
				startedModulesInAnyCollections.add(module.getId());
			}
		}

		try {
			((InstallableModule) module).start(collection, appLayerFactory);
		} catch (Throwable e) {
			if (isPluginModule(module)) {
				constellioPluginManager.handleModuleNotStartedCorrectly(module, collection, e);
				return false;
			} else {
				throw new ConstellioModulesManagerRuntimeException.FailedToStart((InstallableModule) module, e);
			}
		}
		return true;
	}

	boolean isPluginModule(Module module) {
		return constellioPluginManager.isPluginModule(module);
	}

	public void stopModule(String collection, Module module) {
		try {
			((InstallableModule) module).stop(collection, appLayerFactory);
		} catch (Throwable e) {
			throw new ConstellioModulesManagerRuntimeException.FailedToStop((InstallableModule) module, e);
		}

		if (module instanceof InstallableSystemModule) {
			if (startedModulesInAnyCollections.contains(module.getId())) {
				((InstallableSystemModule) module).stop(appLayerFactory);
				startedModulesInAnyCollections.remove(module.getId());
			}
		}
	}

	public Set<String> startModules(String collection) {
		Set<String> returnList = new HashSet<>();
		for (Module module : getEnabledModules(collection)) {
			if (!startModule(collection, module)) {
				returnList.add(module.getId());
			}
		}

		return returnList;
	}

	public void stopModules(String collection) {
		for (Module module : getEnabledModules(collection)) {
			stopModule(collection, module);
		}
	}

	public boolean isInstalled(Module module) {
		for (Module anInstalledModule : getInstalledModules()) {
			if (anInstalledModule.getId().equals(module.getId())) {
				return true;
			}
		}
		return false;
	}

	public void removeCollectionFromVersionProperties(final String collection, ConfigManager configManager) {
		configManager.updateProperties("/version.properties", newRemoveCollectionPropertiesAlteration(collection));
	}

	PropertiesAlteration newRemoveCollectionPropertiesAlteration(final String collection) {
		return new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				if (properties.containsKey(collection + "_version")) {
					properties.remove(collection + "_version");
				}
			}
		};
	}

	@Override
	public void close() {

	}

	public InstallableModule getInstalledModule(String id) {
		for (InstallableModule module : getAllModules()) {
			if (module.getId().equals(id)) {
				return module;
			}
		}
		throw new ConstellioPluginManagerRuntimeException_NoSuchModule(id);
	}

	public NavigationConfig getNavigationConfig(String collection) {
		NavigationConfig config = new NavigationConfig();
		new CoreNavigationConfiguration().configureNavigation(config);
		for (InstallableModule module : getEnabledModules(collection)) {
			try {
				module.configureNavigation(config);
			} catch (Throwable e) {
				LOGGER.error("Error when configuring navigation of module " + module.getId() + " in collection " + collection, e);
			}

		}
		return config;
	}

	public List<String> getPermissionGroups(String collection) {
		return new ArrayList<>(getCollectionPermissions(collection).keySet());
	}

	public List<String> getPermissionsInGroup(String collection, String permissionGroupCode) {
		return new ArrayList<>(getCollectionPermissions(collection).get(permissionGroupCode));
	}

	private Map<String, List<String>> getCollectionPermissions(String collection) {
		Map<String, List<String>> permissions = new HashMap<>(CorePermissions.PERMISSIONS.getGrouped());
		for (Module module : getEnabledModules(collection)) {
			permissions.putAll(PluginUtil.getPermissions(module));
		}
		return permissions;
	}

	public void markAsEnabled(final Module module, final String collection) {
		configManager.updateXML(MODULES_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Map<String, Element> moduleElements = parseModulesDocument(document);
				Element moduleElement = moduleElements.get(module.getId());
				if (moduleElement == null) {
					throw new ConstellioModulesManagerRuntimeException_ModuleIsNotInstalled(module);
				}
				moduleElement.setAttribute("enabled_in_collection_" + collection, "true");
			}
		});
	}

}
