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
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.extensions.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
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
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioModulesManagerImpl implements ConstellioModulesManager, StatefulService {
	@SuppressWarnings("unused") private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioModulesManagerImpl.class);
	private static final String FALSE = "false";
	private static final String MODULES_CONFIG_PATH = "/modules.xml";
	private final ConfigManager configManager;
	private final AppLayerFactory appLayerFactory;
	private final Delayed<MigrationServices> migrationServicesDelayed;
	private final ModelLayerFactory modelLayerFactory;
	private final ConstellioPluginManager constellioPluginManager;

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

	public void startModules() {
		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		for (String collection : collectionsListManager.getCollections()) {
			startModules(collection);
		}
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
		for (InstallableModule module : getAllModules()) {
			Element moduleElement = moduleElements.get(module.getId());

			if (moduleElement != null && isEnabled(moduleElement, collection)) {
				enabledModules.add(module);
			}
		}
		return enabledModules;
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
		Map<String, Element> moduleElements = new HashMap<String, Element>();
		for (Element moduleElement : document.getRootElement().getChildren()) {
			moduleElements.put(moduleElement.getAttributeValue("id"), moduleElement);
		}
		return moduleElements;
	}

	public List<InstallableModule> getDisabledModules(String collection) {
		List<InstallableModule> disabledModules = new ArrayList<InstallableModule>();
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
		List<InstallableModule> availableModules = new ArrayList<InstallableModule>();
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
		return constellioPluginManager.getPlugins(InstallableModule.class);
	}

	public void markAsInstalled(final Module module, CollectionsListManager collectionsListManager) {
		for (String dependentModuleId : module.getDependencies()) {
			InstallableModule dependentModule = getInstalledModule(dependentModuleId);
			if (!isInstalled(dependentModule)) {
				addModuleInConfigFile(dependentModule);
			}
		}
		addModuleInConfigFile(module);
	}

	public void installModule(final Module module, CollectionsListManager collectionsListManager) {
		markAsInstalled(module, collectionsListManager);
		MigrationServices migrationServices = migrationServicesDelayed.get();
		for (String collection : collectionsListManager.getCollections()) {
			try {
				migrationServices.migrate(collection, null);
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}
		}
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

	public void enableModule(String collection, Module module) {
		markAsEnabled(module, collection);
		applyModuleMigrations(collection);
		startModule(collection, module);
	}

	private void applyModuleMigrations(String collection) {

		MigrationServices migrationServices = migrationServicesDelayed.get();

		try {
			migrationServices.migrate(collection, null);
		} catch (OptimisticLockingConfiguration e) {
			// TODO: Handle this
		}
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

	public void startModule(String collection, Module module) {
		try {
			((InstallableModule) module).start(collection, appLayerFactory);
		} catch (Exception e) {
			throw new ConstellioModulesManagerRuntimeException.FailedToStart((InstallableModule) module, e);
		}
	}

	public void stopModule(String collection, Module module) {
		try {
			((InstallableModule) module).stop(collection, appLayerFactory);
		} catch (Exception e) {
			throw new ConstellioModulesManagerRuntimeException.FailedToStop((InstallableModule) module, e);
		}
	}

	public void startModules(String collection) {
		for (Module module : getEnabledModules(collection)) {
			startModule(collection, module);
		}

	}

	public void stopModules(String collection) {
		for (Module module : getEnabledModules(collection)) {
			stopModule(collection, module);
		}
	}

	public boolean isInstalled(Module module) {
		for (Module anInstalledModule : getInstalledModules()) {
			if (anInstalledModule.getClass().equals(module.getClass())) {
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
			module.configureNavigation(config);
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
			permissions.putAll(module.getPermissions());
		}
		return permissions;
	}

	public void markAsEnabled(final Module module, final String collection) {
		configManager.updateXML(MODULES_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				Map<String, Element> moduleElements = parseModulesDocument(document);
				moduleElements.get(module.getId()).setAttribute("enabled_in_collection_" + collection, "true");
			}
		});
	}
}
