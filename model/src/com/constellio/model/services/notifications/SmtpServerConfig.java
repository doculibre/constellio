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
package com.constellio.model.services.notifications;

import java.util.Properties;

public class SmtpServerConfig {

	final String user;
	final String email;
	final String password;
	final Properties properties;

	public SmtpServerConfig(String email, String user, String password, Properties properties) {
		super();
		this.user = user;
		this.email = email;
		this.password = password;
		this.properties = properties;
	}

	public String getUser() {
		return user;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public Properties getProperties() {
		return properties;
	}

}
