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
package com.constellio.app.ui.pages.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public class LogoUtils {
	private static final Logger LOGGER = Logger.getLogger(LogoUtils.class.getName());

	public static Resource getUserLogoResource(ModelLayerFactory modelLayerFactory) {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		StreamFactory<InputStream> streamFactory = manager.getValue(ConstellioEIMConfigs.LOGO);
		InputStream returnStream = null;
		if (streamFactory != null) {
			try {
				returnStream = streamFactory.create("logo_eimUSR");
			} catch (IOException e) {
				LOGGER.warn(e);
				return null;
			}
		}
		if (returnStream == null) {
			return null;
		}

		File file = new File("logo_eimUSR");
		try {
			FileUtils.copyInputStreamToFile(returnStream, file);
			//TODO Francis file created by resource is not removed from file system
			modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newIOServices().closeQuietly(returnStream);
		} catch (IOException e) {
			LOGGER.warn(e);
			return null;
		} finally {
			IOUtils.closeQuietly(returnStream);
		}
		Resource resource = new FileResource(file);
		return resource;
	}

	public static Resource getLogoResource(ModelLayerFactory modelLayerFactory) {
		Resource userResource = getUserLogoResource(modelLayerFactory);
		if (userResource == null) {
			return getDefaultResource();
		} else {
			return userResource;
		}
	}

	private static Resource getDefaultResource() {
		return new ThemeResource("images/logo_eim_203x30.png");
	}

}
