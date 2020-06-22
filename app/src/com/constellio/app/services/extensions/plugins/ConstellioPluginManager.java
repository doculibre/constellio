package com.constellio.app.services.extensions.plugins;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.modules.Module;

import java.io.File;
import java.util.List;

public interface ConstellioPluginManager extends StatefulService {

	void detectPlugins();

	void registerModule(InstallableModule plugin)
			throws InvalidId;

	void registerPluginOnlyForTests(InstallableModule plugin)
			throws InvalidId;

	List<InstallableModule> getRegistredModulesAndActivePlugins();

	List<InstallableModule> getRegisteredModules();

	List<InstallableModule> getActivePluginModules();

	PluginActivationFailureCause prepareInstallablePlugin(File file);

	PluginActivationFailureCause prepareInstallablePluginInNextWebapp(File file, File nextWebappFolder);

	void markNewPluginsInNewWarAsInstalled(FoldersLocator foldersLocator);

	void markPluginAsDisabled(String pluginId);

	void markPluginAsEnabled(String pluginId);

	void handleModuleNotStartedCorrectly(Module module, String collection, Throwable throwable);

	boolean isPluginModule(Module module);

	void handleModuleNotMigratedCorrectly(String moduleId, String collection, Throwable throwable);

	List<ConstellioPluginInfo> getPlugins(ConstellioPluginStatus... statuses);

	List<String> getPluginsOfEveryStatus();

	boolean isRegistered(String id);

	void copyPluginResourcesToPluginsResourceFolder(String moduleId);

	<T> Class<T> getModuleClass(String name)
			throws ClassNotFoundException;

	void removePlugin(String code);

	void configure();
}
