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

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.constellio.model.conf.FoldersLocator;

public class GetLatestVersionJarAgentServlet extends SocketAgentServlet<byte[]> {

	@Override
	protected byte[] respond(Map<String, Object> inParams) throws Exception {
		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getModuleResourcesFolder("rm");
		File agentFolder = new File(resourcesFolder, "agent");
		File agentJarFile = new File(agentFolder, "constellio-agent.jar.rename");
		return FileUtils.readFileToByteArray(agentJarFile);
	}

}
