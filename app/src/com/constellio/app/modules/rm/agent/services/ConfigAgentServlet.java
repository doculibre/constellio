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
package com.constellio.app.modules.rm.agent.services;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Properties;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConfigAgentServlet extends SocketAgentServlet<byte[]> {

	@Override
	protected byte[] respond(Map<String, Object> inParams) throws Exception {
		String filename = (String) inParams.get("filename");
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		ModelLayerConfiguration modelLayerConfiguration = modelLayerFactory.getConfiguration();
		RMConfigs rmConfigs = getRMConfigs();
		
		String localeCode = modelLayerConfiguration.getMainDataLanguage();
		
		byte[] result;
		if ("constellio-agent.properties".equals(filename)) {
			Properties props = new Properties();
			props.setProperty("changeConstellioURLPossible", "true"); //TODO Remove this config
			props.setProperty("locale", localeCode);
			props.setProperty("switchUserPossible", "" + rmConfigs.isAgentSwitchUserPossible());
			props.setProperty("downloadAllUserContent", "" + rmConfigs.isAgentDownloadAllUserContent());
			props.setProperty("editUserDocuments", "" + rmConfigs.isAgentEditUserDocuments());
			props.setProperty("backupRetentionPeriodInDays", "" + rmConfigs.getAgentBackupRetentionPeriodInDays());
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			props.store(out, null);
			result = out.toByteArray();
		} else {
			result = null;
		}
		return result;
	}

}
