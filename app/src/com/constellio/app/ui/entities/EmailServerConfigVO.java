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
package com.constellio.app.ui.entities;

import com.constellio.model.conf.email.EmailServerConfiguration;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class EmailServerConfigVO implements Serializable, EmailServerConfiguration {
	String username;
	String password;
	private String defaultEmailServer;

	public Map<String, String> getProperties() {
		return properties;
	}

	public EmailServerConfigVO setProperties(Map<String, String> properties) {
		this.properties = properties;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public EmailServerConfigVO setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getDefaultSenderEmail() {
		return defaultEmailServer;
	}

	public EmailServerConfigVO setDefaultEmailServer(String defaultEmailServer) {
		this.defaultEmailServer = defaultEmailServer;
		return this;
	}

	public EmailServerConfigVO setPassword(String password) {
		this.password = password;
		return this;
	}

	Map<String, String> properties;
}
