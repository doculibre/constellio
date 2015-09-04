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
package com.constellio.model.conf.email;

import java.util.Map;

public class SmtpServerConfiguration implements EmailServerConfiguration{
    String username;
    String password;
    private Map<String, String> properties;
    private String defaultSenderEmail;

    public SmtpServerConfiguration(String username, String password, String defaultSenderEmail, Map<String, String> properties) {
        this.username = username;
        this.defaultSenderEmail = defaultSenderEmail;
        this.password = password;
        this.properties = properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDefaultSenderEmail() {
        return defaultSenderEmail;
    }

}
