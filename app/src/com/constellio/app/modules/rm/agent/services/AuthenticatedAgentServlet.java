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

import java.util.Map;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

public abstract class AuthenticatedAgentServlet<T> extends SocketAgentServlet<T> {

	@Override
	protected T respond(Map<String, Object> inParams) throws Exception {
		byte[] tokenBytes = (byte[]) inParams.get("token");
		String token = tokenBytes != null ? new String(tokenBytes, "UTF-8") : null;
	
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();

		String serviceKey = userServices.getServiceKeyByToken(token);
		String username = userServices.getTokenUser(serviceKey, token);
		
		return doAuthenticated(inParams, username);
	}

	protected abstract T doAuthenticated(Map<String, Object> inParams, String username) throws Exception ;

}
