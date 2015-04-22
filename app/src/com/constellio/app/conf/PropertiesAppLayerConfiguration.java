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
package com.constellio.app.conf;

import java.io.File;
import java.util.Map;

import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;

public class PropertiesAppLayerConfiguration extends PropertiesConfiguration implements AppLayerConfiguration {

	private FoldersLocator foldersLocator;

	private final ModelLayerConfiguration modelLayerConfiguration;

	public PropertiesAppLayerConfiguration(Map<String, String> configs, ModelLayerConfiguration modelLayerConfiguration,
			FoldersLocator foldersLocator) {
		super(configs);
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void validate() {

	}

	@Override
	public File getTempFolder() {
		return modelLayerConfiguration.getTempFolder();
	}

	@Override
	public File getPluginsFolder() {
		return null;
	}

	@Override
	public File getSetupProperties() {
		return foldersLocator.getConstellioSetupProperties();
	}

}
