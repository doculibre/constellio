package com.constellio.app.services.extensions.plugins;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_BlankId;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_ExistingId;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_NonAlphaNumeric;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_InvalidJar;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_InvalidManifest;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoCode;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoVersion;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NonExistingFile;
import com.constellio.app.services.extensions.plugins.PluginServices.PluginsReplacementException;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils;
import com.constellio.app.services.extensions.plugins.utils.PluginManagementUtils.NewPluginsInNewWar;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.entities.modules.ConstellioPlugin;
import com.constellio.model.entities.modules.Module;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.ID_MISMATCH;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_EXISTING_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_ID_FORMAT;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_MANIFEST;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_MIGRATION_SCRIPT;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_START;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.IO_EXCEPTION;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.JAR_NOT_FOUND;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.JAR_NOT_SAVED_CORRECTLY;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_VERSION;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.DISABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

public class JSPFConstellioPluginManager implements StatefulService, ConstellioPluginManager {
	private static final Logger LOGGER = LogManager.getLogger(JSPFConstellioPluginManager.class);
	public static final String PREVIOUS_PLUGINS = "previousPlugins";

	public static List<InstallableModule> availablePluginsForTestOnly = new ArrayList<>();

	private final File pluginsDirectory;
	private PluginManager pluginManager;
	private final ConstellioPluginConfigurationManager pluginConfigManger;
	private Map<String, InstallableModule> registeredModules = new HashMap<>();
	private Map<String, InstallableModule> validUploadedPlugins = new HashMap<>();
	private IOServices ioServices;
	private final File pluginsManagementOnStartupFile;

	public JSPFConstellioPluginManager(File pluginsDirectory, File pluginsManagementOnStartupFile,
									   IOServices ioServices,
									   ConstellioPluginConfigurationManager pluginConfigManger) {
		this.pluginConfigManger = pluginConfigManger;
		this.pluginsDirectory = pluginsDirectory;
		this.pluginsManagementOnStartupFile = pluginsManagementOnStartupFile;
		if (pluginsDirectory != null && pluginsDirectory.isDirectory()) {
			//FIXME see Cis
			File saveOldPluginsDestination = new File(pluginsDirectory, PREVIOUS_PLUGINS);
			if (!saveOldPluginsDestination.exists()) {
				try {
					FileUtils.forceMkdir(saveOldPluginsDestination);
				} catch (IOException e) {
					LOGGER.error("Error when trying to create old plugins, please replace them manually", e);
					throw new RuntimeException(e);
				}
			}
		}
		this.ioServices = ioServices;
		pluginConfigManger.createConfigFileIfNotExist();
	}

	public void detectPlugins() {
		detectPluginsInDirectory(pluginsDirectory);
	}

	public void detectPluginsInDirectory(File pluginsDirectory) {
		initialize();
		this.pluginManager = PluginManagerFactory.createPluginManager();
		if (pluginsDirectory != null && pluginsDirectory.isDirectory()) {
			try {
				newPluginServices().replaceOldPluginVersionsByNewOnes(pluginsDirectory,
						new File(pluginsDirectory, PREVIOUS_PLUGINS));
			} catch (PluginsReplacementException e) {
				for (String pluginId : e.getPluginsWithReplacementExceptionIds()) {
					if (pluginConfigManger.getPluginInfo(pluginId) != null) {
						pluginConfigManger.invalidateModule(pluginId, IO_EXCEPTION, e);
					}
				}
			}

			for (ConstellioPluginInfo pluginInfo : getPlugins(ENABLED, DISABLED, READY_TO_INSTALL)) {
				LOGGER.info("Detected plugin for tenant " + TenantUtils.getTenantId() + " : " + pluginInfo.getCode());
				installValidPlugin(pluginInfo);
			}
			//handlePluginsDependency();

			//Be aware of order : invalid are last plugins to be handled
			for (ConstellioPluginInfo pluginInfo : getPlugins(INVALID)) {
				installInvalidPlugin(pluginInfo.getCode());
			}
		}

	}

//	//TODO in future version
//	private void handlePluginsDependency() {
//		for (InstallableModule pluginModule : validUploadedPlugins.values()) {
//			//1. ensure no module depend on invalid module otherwise it is also invalid
//			List<String> dependencies = pluginModule.getDependencies();
//			if (dependencies != null) {
//				for (String dependency : dependencies) {
//					if (!registeredModules.keySet().contains(dependency)) {
//						InstallableModule dependOnModule = validUploadedPlugins.get(dependency);
//						if (dependOnModule == null
//							|| pluginConfigManger.getPluginInfo(dependency).getPluginStatus() == DISABLED) {
//							//TODO disable and remove from validUploadedPlugins see avec Cis
//						}
//					}
//				}
//			}
//			//2. cyclic dependency is not supported hence save only oldest plugins
//		}
//	}

	private void installInvalidPlugin(String pluginId) {
		PluginServices helperService = newPluginServices();
		File pluginJar = helperService.getPluginJar(pluginsDirectory, pluginId);
		if (pluginJar == null) {
			LOGGER.error("Invalid plugin " + pluginId + " not found in plugins directory");
		} else {
			try {
				pluginManager.addPluginsFrom(pluginJar.toURI());
			} catch (Throwable e) {
				LOGGER.error("Error when trying to load invalid plugin " + pluginId, e);
			}
		}
	}

	void installValidPlugin(ConstellioPluginInfo existingInfo) {
		PluginServices helperService = newPluginServices();
		String pluginId = existingInfo.getCode();
		File pluginJar = helperService.getPluginJar(pluginsDirectory, pluginId);
		if (pluginJar == null) {
			String error = "Plugin " + pluginId + " not found in plugins directory";
			LOGGER.error(error);
			pluginConfigManger.invalidateModule(pluginId, JAR_NOT_FOUND, null);
		} else {
			try {
				PluginActivationFailureCause failure = registerPlugin(pluginJar, pluginId);
				if (existingInfo.getPluginStatus().equals(READY_TO_INSTALL)) {
					LOGGER.info("Plugin " + pluginId + " was ready to installed, now marked as enabled");
					pluginConfigManger.markPluginAsEnabled(pluginId);
				}
				if (failure != null) {
					LOGGER.info("Plugin " + pluginId + " failed : " + failure);
					pluginConfigManger.invalidateModule(existingInfo.getCode(), failure, null);
				}
			} catch (Throwable e) {
				LOGGER.error("Error when trying to register plugin " + existingInfo.getCode(), e);
				pluginConfigManger.invalidateModule(existingInfo.getCode(), JAR_NOT_FOUND, e);
			}
		}
	}

	private PluginActivationFailureCause registerPlugin(File pluginJar, String pluginId) {
		PluginManagerUtil util = new PluginManagerUtil(pluginManager);
		Collection<InstallableModule> pluginsBefore = util.getPlugins(InstallableModule.class);

		pluginManager.addPluginsFrom(pluginJar.toURI());
		util = new PluginManagerUtil(pluginManager);
		Collection<InstallableModule> pluginsAfter = util.getPlugins(InstallableModule.class);

		if (pluginsAfter.size() == pluginsBefore.size()) {
			return NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR;
		} else if (pluginsAfter.size() > pluginsBefore.size() + 1) {
			return MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR;
		}
		int pluginsWithPluginIdCount = 0;
		InstallableModule newInstallableModule = null;
		for (InstallableModule plugin : pluginsAfter) {
			if (plugin.getId() != null && plugin.getId().equals(pluginId)) {
				newInstallableModule = plugin;
				pluginsWithPluginIdCount++;
			}
		}
		if (pluginsWithPluginIdCount != 1) {
			return ID_MISMATCH;
		} else {
			validUploadedPlugins.put(pluginId, newInstallableModule);
		}
		return null;
	}

	@Override
	public void registerModule(InstallableModule plugin)
			throws InvalidId {
		if (plugin != null) {
			validateId(plugin.getId());
			registeredModules.put(plugin.getId(), plugin);
		}
	}

	public void unregisterModule(InstallableModule plugin)
			throws InvalidId {
		if (plugin != null) {
			registeredModules.remove(plugin.getId());
		}
	}

	@Override
	public void registerPluginOnlyForTests(InstallableModule plugin)
			throws InvalidId {
		if (plugin != null) {
			validateId(plugin.getId());
			this.pluginConfigManger.addOrUpdatePlugin(
					new ConstellioPluginInfo().setCode(plugin.getId()).setPluginStatus(ENABLED).setTitle(plugin.getName()));
			validUploadedPlugins.put(plugin.getId(), plugin);
		}
	}

	void validateId(String id)
			throws InvalidId {
		if (StringUtils.isBlank(id)) {
			throw new InvalidId_BlankId(id);
		}
		if (registeredModules.keySet().contains(id) || validUploadedPlugins.containsValue(id)) {
			throw new InvalidId_ExistingId(id);
		}
		Pattern pattern = Pattern.compile("(\\w)*");
		Matcher matcher = pattern.matcher(id);
		if (!matcher.matches()) {
			throw new InvalidId_NonAlphaNumeric(id);
		}
	}

	@Override
	public void initialize() {
		pluginConfigManger.createConfigFileIfNotExist();
		markNewPluginsInNewWarAsInstalled(new FoldersLocator());
	}

	@Override
	public List<InstallableModule> getRegistredModulesAndActivePlugins() {
		ensureStarted();
		List<InstallableModule> plugins = new ArrayList<>();

		Collection<InstallableModule> registeredModulesValues = registeredModules.values();
		LOGGER.info("Registered module values for tenant '" + TenantUtils.getTenantId() + "' are : " +
					registeredModulesValues.stream().map(InstallableModule::getId).collect(Collectors.toList()));
		plugins.addAll(registeredModulesValues);

		Collection<InstallableModule> activePluginModules = getActivePluginModules();
		LOGGER.info("Active plugin modules for tenant '" + TenantUtils.getTenantId() + "' are : " +
					activePluginModules.stream().map(InstallableModule::getId).collect(Collectors.toList()));
		plugins.addAll(activePluginModules);
		return plugins;
	}

	@Override
	public List<InstallableModule> getRegisteredModules() {
		ensureStarted();
		List<InstallableModule> plugins = new ArrayList<>();
		plugins.addAll(registeredModules.values());
		return plugins;
	}


	@Override
	public List<InstallableModule> getActivePluginModules() {
		List<InstallableModule> returnList = new ArrayList<>();
		List<String> activePluginModulesIds = pluginConfigManger.getActivePluginsIds();
		if (new FoldersLocator().getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			Set<String> foundPlugins = new HashSet<>();
			for (InstallableModule pluginModule : availablePluginsForTestOnly) {
				if (activePluginModulesIds.contains(pluginModule.getId())) {
					returnList.add(pluginModule);
					foundPlugins.add(pluginModule.getId());
				}
			}
			for (InstallableModule pluginModule : validUploadedPlugins.values()) {
				if (activePluginModulesIds.contains(pluginModule.getId())
					&& !foundPlugins.contains(pluginModule.getId())) {
					returnList.add(pluginModule);
				}
			}

		} else {
			for (InstallableModule pluginModule : validUploadedPlugins.values()) {
				if (activePluginModulesIds.contains(pluginModule.getId())) {
					returnList.add(pluginModule);
				}
			}
		}
		return returnList;
	}

	@Override
	public PluginActivationFailureCause prepareInstallablePlugin(File jarFile) {
		PluginServices helperService = newPluginServices();
		ConstellioPluginInfo newPluginInfo;
		try {
			newPluginInfo = helperService.extractPluginInfo(jarFile);
			validateId(newPluginInfo.getCode());
		} catch (InvalidPluginJarException e) {
			return getAdequateFailureCause(e);
		} catch (InvalidId_BlankId | InvalidId_NonAlphaNumeric e) {
			return INVALID_ID_FORMAT;
		} catch (InvalidId_ExistingId e3) {
			return INVALID_EXISTING_ID;
		}
		ConstellioPluginInfo existingPluginInfo = pluginConfigManger.getPluginInfo(newPluginInfo.getCode());
		PluginActivationFailureCause failure = helperService.validatePlugin(newPluginInfo, existingPluginInfo);
		if (failure != null) {
			return failure;
		}

		try {
			helperService.saveNewPlugin(pluginsDirectory, jarFile, newPluginInfo.getCode());
			pluginConfigManger.installPlugin(newPluginInfo.getCode(), newPluginInfo.getTitle(),
					newPluginInfo.getVersion(), newPluginInfo.getRequiredConstellioVersion());
			addPluginToManageOnStartupList(newPluginInfo.getCode());
		} catch (IOException e) {
			LOGGER.error("Exception when saving new plugin", e);
			return JAR_NOT_SAVED_CORRECTLY;
		}
		return null;
	}

	@Override
	public PluginActivationFailureCause prepareInstallablePluginInNextWebapp(File jarFile, File nextWebapp) {
		PluginServices helperService = newPluginServices();
		ConstellioPluginInfo newPluginInfo;
		try {
			newPluginInfo = helperService.extractPluginInfo(jarFile);
			validateId(newPluginInfo.getCode());
		} catch (InvalidPluginJarException e) {
			return getAdequateFailureCause(e);
		} catch (InvalidId_BlankId | InvalidId_NonAlphaNumeric e) {
			return INVALID_ID_FORMAT;
		} catch (InvalidId_ExistingId e3) {
			return INVALID_EXISTING_ID;
		}
		ConstellioPluginInfo existingPluginInfo = pluginConfigManger.getPluginInfo(newPluginInfo.getCode());
		PluginActivationFailureCause failure = helperService.validatePlugin(newPluginInfo, existingPluginInfo);
		if (failure != null) {
			return failure;
		}

		File pluginsDirectory = new File(nextWebapp, "WEB-INF" + File.separator + "plugins");
		File libDirectory = new File(nextWebapp, "WEB-INF" + File.separator + "lib");

		File jarfileInNextWarPlugins = new File(pluginsDirectory, newPluginInfo.getCode() + ".jar");
		File jarfileInNextWarLibs = new File(libDirectory, newPluginInfo.getCode() + ".jar");
		jarfileInNextWarPlugins.delete();
		jarfileInNextWarLibs.delete();
		try {
			FileUtils.copyFile(jarFile, jarfileInNextWarPlugins);
			if (TenantUtils.isSupportingTenants()) {
				FileUtils.copyFile(jarFile, jarfileInNextWarLibs);
			} else {
				FileUtils.moveFile(jarFile, jarfileInNextWarLibs);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		PluginManagementUtils.markNewPluginsInNewWar(nextWebapp, jarfileInNextWarPlugins.getName(), TenantUtils.getTenantId());

		return null;
	}

	public void markNewPluginsInNewWarAsInstalled(FoldersLocator foldersLocator) {
		File webapp = foldersLocator.getConstellioWebappFolder();
		File plugins = foldersLocator.getPluginsJarsFolder();
		List<NewPluginsInNewWar> newPluginsFileNames = PluginManagementUtils.getNewPluginsInNewWar(webapp);

		if (!newPluginsFileNames.isEmpty()) {
			for (NewPluginsInNewWar newPluginsInNewWar : newPluginsFileNames) {
				if (LangUtils.isEqual(newPluginsInNewWar.getTenantId(), TenantUtils.getTenantId())) {
					File newPlugin = new File(plugins, newPluginsInNewWar.getFilename());
					PluginServices helperService = newPluginServices();
					ConstellioPluginInfo newPluginInfo;
					try {
						newPluginInfo = helperService.extractPluginInfo(newPlugin);
						validateId(newPluginInfo.getCode());
					} catch (Exception e) {
						throw new ImpossibleRuntimeException(e);
					}

					LOGGER.info("mark plugin " + newPluginsInNewWar.getFilename() + "' in new war '" + webapp.getAbsolutePath() + "' as installed for tenant '" + TenantUtils.getTenantId() + "'");
					pluginConfigManger.installPlugin(newPluginInfo.getCode(), newPluginInfo.getTitle(),
							newPluginInfo.getVersion(), newPluginInfo.getRequiredConstellioVersion());
				}
			}
		}
		PluginManagementUtils.clearNewPluginsInNewWar(webapp);
	}

	void addPluginToManageOnStartupList(String code) {
		PluginManagementUtils utils = new PluginManagementUtils(pluginsDirectory, null, pluginsManagementOnStartupFile);
		try {
			utils.addPluginToMove(code);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private PluginActivationFailureCause getAdequateFailureCause(InvalidPluginJarException e) {
		if (e instanceof InvalidPluginJarException_NonExistingFile) {
			return JAR_NOT_FOUND;
		} else if (e instanceof InvalidPluginJarException_InvalidManifest) {
			return INVALID_MANIFEST;
		} else if (e instanceof InvalidPluginJarException_NoVersion) {
			return NO_VERSION;
		} else if (e instanceof InvalidPluginJarException_NoCode) {
			return NO_ID;
		} else if (e instanceof InvalidPluginJarException_InvalidJar) {
			return INVALID_JAR;
		} else {
			throw new RuntimeException("Unsupported exception ", e);
		}
	}

	@Override
	public void markPluginAsEnabled(String pluginId) {
		pluginConfigManger.markPluginAsEnabled(pluginId);
	}

	@Override
	public void markPluginAsDisabled(String pluginId) {
		pluginConfigManger.markPluginAsDisabled(pluginId);
	}

	@Override
	public void handleModuleNotStartedCorrectly(Module module, String collection, Throwable throwable) {
		if (isPluginModule(module)) {
			pluginConfigManger.invalidateModule(module.getId(), INVALID_START, throwable);
		} else {
			LOGGER.error("module not migrated correctly", throwable);
			throw new RuntimeException(throwable);
		}
	}

	@Override
	public boolean isPluginModule(Module module) {
		return isPluginModule(module.getId());
	}

	private boolean isPluginModule(String id) {
		return !registeredModules.keySet().contains(id);
	}

	@Override
	public void handleModuleNotMigratedCorrectly(String moduleId, String collection, Throwable throwable) {
		if (isPluginModule(moduleId)) {
			if (throwable != null) {
				throwable.printStackTrace();
			}
			pluginConfigManger.invalidateModule(moduleId, INVALID_MIGRATION_SCRIPT, throwable);
		} else {
			LOGGER.error("module not migrated correctly", throwable);
			throw new RuntimeException(throwable);
		}

	}

	@Override
	public List<ConstellioPluginInfo> getPlugins(ConstellioPluginStatus... statuses) {
		List<ConstellioPluginInfo> returnList = new ArrayList<>();
		for (ConstellioPluginStatus status : statuses) {
			returnList.addAll(pluginConfigManger.getPlugins(status));
		}
		return returnList;
	}

	@Override
	public List<String> getPluginsFromAnyTenants(ConstellioPluginStatus... statuses) {
		Set<String> returnList = new HashSet<>();
		if (TenantUtils.isSupportingTenants()) {


			for (TenantProperties tenantProperties : TenantService.getInstance().getTenants()) {
				List<ConstellioPluginInfo> pluginsFromTenant = ConstellioFactories
						.getInstance("" + tenantProperties.getId()).getAppLayerFactory().getPluginManager().getPlugins(statuses);
				pluginsFromTenant.forEach((info) -> returnList.add(info.getCode()));
			}

		} else {
			List<ConstellioPluginInfo> pluginsFromTenant = ConstellioFactories
					.getInstance().getAppLayerFactory().getPluginManager().getPlugins(statuses);
			pluginsFromTenant.forEach((info) -> returnList.add(info.getCode()));
		}
		return new ArrayList<>(returnList);

	}

	@Override
	public List<String> getPluginsOfEveryStatus() {
		return pluginConfigManger.getAllPluginsCodes();
	}

	public List<String> getPluginsOfEveryStatusFromAnyTenants() {
		if (TenantUtils.isSupportingTenants()) {
			Set<String> returnList = new HashSet<>();
			for (TenantProperties tenantProperties : TenantService.getInstance().getTenants()) {
				returnList.addAll(ConstellioFactories
						.getInstance("" + tenantProperties.getId()).getAppLayerFactory().getPluginManager().getPluginsOfEveryStatus());
			}
			return new ArrayList<>(returnList);
		} else {
			return getPluginsOfEveryStatus();
		}
	}

	@Override
	public boolean isRegistered(String id) {
		return registeredModules.keySet().contains(id) || validUploadedPlugins.containsValue(id);
	}

	@Override
	public void copyPluginResourcesToPluginsResourceFolder(String moduleId) {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() != FoldersLocatorMode.PROJECT) {
			File jar = newPluginServices().getPluginJar(pluginsDirectory, moduleId);
			if (jar != null && jar.exists()) {
				File resourceFolder = foldersLocator.getPluginsResourcesFolder();
				newPluginServices().extractPluginResources(jar, moduleId, resourceFolder);
			}
		}

	}

	@Override
	public <T> Class<T> getModuleClass(String name)
			throws ClassNotFoundException {
		for (InstallableModule module : getActivePluginModules()) {
			try {
				return (Class<T>) module.getClass().getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				//OK
			}
		}
		throw new ClassNotFoundException(name);
	}

	@Override
	public void removePlugin(String code) {
		this.validUploadedPlugins.remove(code);
		this.pluginConfigManger.removePlugin(code);
	}

	@Override
	public void configure() {
		if (pluginManager != null) {
			pluginManager.getPluginConfiguration().setConfiguration(ConstellioPlugin.class, "singletonInitializeMode", "true");
		}
	}

	private void ensureStarted() {
		if (pluginManager == null) {
			throw new ConstellioPluginManagerRuntimeException("Cannot use plugin manager until it has been started");
		}
	}

	private PluginServices newPluginServices() {
		return new JSPFPluginServices(ioServices);
	}

	@Override
	public void close() {
		if (this.pluginManager != null) {
			this.pluginManager.shutdown();
			this.pluginManager = null;
		}
	}

}
