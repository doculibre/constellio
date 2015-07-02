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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.agent.exceptions.AgentDisabledException;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class SocketAgentServlet<T> extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		InputStream input = request.getInputStream();
		OutputStream output = response.getOutputStream();
		T responseResult = null;
		Throwable throwable = null;
		try {
			if (getRMConfigs().isAgentEnabled()) {
				ObjectInputStream ois = new ObjectInputStream(input);
				Map<String, Object> inParams = (Map<String, Object>) ois.readObject();
				responseResult = respond(inParams);
			} else {
				throw new AgentDisabledException();
			}
		} catch (Throwable e) {
			throwable = e;
		} finally {
			IOUtils.closeQuietly(input);
		}
		if (responseResult != null || throwable != null) {
			Map<String, Object> outParams = new HashMap<>();
			ObjectOutputStream oos = new ObjectOutputStream(output);
			if (responseResult != null) {
				outParams.put("result", responseResult);
			} else {
				String exceptionClassName = throwable.getClass().getName();
				String exceptionMessage = throwable.getMessage();
				String exceptionStackTrace = ExceptionUtils.getFullStackTrace(throwable);
				outParams.put("throwableClassName", exceptionClassName);
				outParams.put("throwableMessage", exceptionMessage);
				outParams.put("throwableStackTrace", exceptionStackTrace);
			}
			oos.writeObject(outParams);
		}
	}
	
	protected RMConfigs getRMConfigs() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		return new RMConfigs(systemConfigurationsManager);
	}
	
	protected String getHash(InputStream in) {
		try {
			return DigestUtils.sha1Hex(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	protected abstract T respond(Map<String, Object> inParams) throws Exception;

}
