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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.modules.ConstellioPlugin;
import com.constellio.model.services.factories.ModelLayerFactory;

public class JSPFConstellioPluginManager implements StatefulService, ConstellioPluginManager {

	private final File pluginsDirectory;
	private PluginManager pluginManager;
	private PluginManagerUtil pluginManagerUtil;
	private ModelLayerFactory modelLayerFactory;
	private DataLayerFactory dataLayerFactory;
	private KeyListMap<Class<?>, Object> registeredPlugins = new KeyListMap<>();

	public JSPFConstellioPluginManager(File pluginsDirectory, ModelLayerFactory modelLayerFactory,
			DataLayerFactory dataLayerFactory) {
		this.pluginsDirectory = pluginsDirectory;
		this.modelLayerFactory = modelLayerFactory;
		this.dataLayerFactory = dataLayerFactory;
	}

	public void detectPlugins() {
		this.pluginManager = PluginManagerFactory.createPluginManager();
		this.pluginManagerUtil = new PluginManagerUtil(pluginManager);
		if (pluginsDirectory != null) {
			pluginManager.addPluginsFrom(pluginsDirectory.toURI());
		}
	}

	public void register(Class<?> pluginClass, Object plugin) {
		registeredPlugins.add(pluginClass, plugin);
	}

	@Override
	public void initialize() {
	}

	public <T extends ConstellioPlugin> List<T> getPlugins(Class<T> pluginClass) {
		ensureStarted();
		List<T> plugins = new ArrayList<T>();
		plugins.addAll(pluginManagerUtil.getPlugins(pluginClass));
		plugins.addAll((List) registeredPlugins.get(pluginClass));
		return plugins;
	}

	private void ensureStarted() {
		if (pluginManager == null) {
			throw new ConstellioPluginManagerRuntimeException("Cannot use plugin manager until it has been started");
		}
	}

	@Override
	public void close() {
		if (this.pluginManager != null) {
			this.pluginManager.shutdown();
			this.pluginManager = null;
		}

	}

}
