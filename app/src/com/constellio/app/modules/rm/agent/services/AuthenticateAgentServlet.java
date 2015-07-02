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
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;

public class AuthenticateAgentServlet extends SocketAgentServlet<String> {

	@Override
	protected String respond(Map<String, Object> inParams) throws Exception {
		String username = (String) inParams.get("username");
		String password = new String((byte[]) inParams.get("password"), "UTF-8");
		String serviceKey = "agent_" + username;
	
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(username);
		
		String token;
		if (userCredential != null) {
			userCredential = userServices.getUserCredential(username).withServiceKey(serviceKey);
			userServices.addUpdateUserCredential(userCredential);
			if (userCredential.getStatus() == UserCredentialStatus.ACTIVE) {
				token = userServices.getToken(serviceKey, username, password);
			} else {
				throw new UserServicesRuntimeException_InvalidUserNameOrPassword(username);
			}
		} else {
			throw new UserServicesRuntimeException_InvalidUserNameOrPassword(username);
		}
		return token;
	}

}
