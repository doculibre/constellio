package com.constellio.app.services.extensions.plugins;

import java.io.File;
import java.util.List;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.modules.Module;

public interface ConstellioPluginManager extends StatefulService {

	void detectPlugins();

	void register(InstallableModule plugin)
			throws InvalidId;

	List<InstallableModule> getActivePlugins();

	List<InstallableModule> getRegisteredPlugins();

	List<InstallableModule> getActivePluginModules();

	PluginActivationFailureCause prepareInstallablePlugin(File file);

	void markPluginAsDisabled(String pluginId);

	void markPluginAsEnabled(String pluginId);

	void handleModuleNotStartedCorrectly(Module module, String collection, Throwable throwable);

	boolean isPluginModule(Module module);

	void handleModuleNotMigratedCorrectly(String moduleId, String collection, Throwable throwable);

	List<ConstellioPluginInfo> getPlugins(ConstellioPluginStatus... statuses);

	boolean isRegistered(String id);
}
