package com.constellio.app.ui.pages.management.plugin;

import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.DISABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.apache.hadoop.util.StringUtils;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
import com.constellio.app.services.extensions.plugins.PluginActivationFailureCause;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.modules.ConstellioPlugin;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;

public class PluginManagementPresenter extends BasePresenter<PluginManagementView> {
	transient ConstellioPluginManager pluginManager;

	public PluginManagementPresenter(PluginManagementView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION);
	}

	public String getDetectedPluginsNames(File file) {
		PluginManager pm = PluginManagerFactory.createPluginManager();
		file.renameTo(file = new File(file.getAbsolutePath() + ".jar"));
		pm.addPluginsFrom(file.toURI());
		PluginManagerUtil util = new PluginManagerUtil(pm);

		Collection<ConstellioPlugin> plugins = util.getPlugins(ConstellioPlugin.class);
		return StringUtils.join("\n", plugins);
	}

	public PluginActivationFailureCause installPlugin(File tempFile) {
		PluginActivationFailureCause cause = pluginManager().prepareInstallablePlugin(tempFile);
		if (cause == null) {
			appLayerFactory.getSystemGlobalConfigsManager().setRestartRequired(true);
		}
		return cause;
	}

	boolean isRestartMessageVisible() {
		return appLayerFactory.getSystemGlobalConfigsManager().isRestartRequired();
	}

	private ConstellioPluginManager pluginManager() {
		if (pluginManager == null) {
			pluginManager = appLayerFactory.getPluginManager();
		}
		return pluginManager;
	}

	public List<ConstellioPluginInfo> getAllPlugins() {
		return pluginManager().getPlugins(DISABLED, ENABLED, READY_TO_INSTALL, INVALID);
	}

	public void enablePlugin(ConstellioPluginInfo info, boolean value) {
		if (value == true) {
			pluginManager().markPluginAsEnabled(info.getCode());
		} else {
			pluginManager().markPluginAsDisabled(info.getCode());
		}
	}

	//TODO test me
	public boolean isEnableOrDisablePossible(ConstellioPluginInfo info) {
		if (!info.getPluginStatus().equals(ENABLED) && !info.getPluginStatus().equals(DISABLED)) {
			return false;
		} else {
			ConstellioModulesManager modulesManager = appLayerFactory.getModulesManager();
			try {
				Module module = modulesManager.getInstalledModule(info.getCode());
				if (module != null && modulesManager.isInstalled(module)) {
					return false;
				} else {
					return true;
				}
			} catch (ConstellioPluginManagerRuntimeException_NoSuchModule e) {
				return true;
			}
		}
	}

	public Boolean isEnabled(ConstellioPluginInfo info) {
		return info.getPluginStatus().equals(ENABLED);
	}
}
