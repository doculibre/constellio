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

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailServerConfigurationFactory {
    private static final String SMTP_SERVER_KEY = "mail.smtp.auth";

    public EmailServerConfiguration getServerConfiguration(String username, String defaultSenderEmail,String password, Map<String, String> serverProperties) {
        if(serverProperties.isEmpty()){
            throw new InvalidPropertiesRuntimeException();
        }
        if(StringUtils.isNotBlank(defaultSenderEmail)){
            if(invalidAddress(defaultSenderEmail)){
                throw new InvalidEmailAddressRuntimeException(defaultSenderEmail);
            }
        }
        if(StringUtils.isBlank(username)){
            throw new InvalidBlankUsernameRuntimeException();
        }
        if(StringUtils.isBlank(password)){
            throw new InvalidBlankPasswordRuntimeException();
        }
        EmailServerConfiguration serverConfiguration = getSmtpServerProperties(username, defaultSenderEmail, password, serverProperties);
        if(serverConfiguration == null){
            throw new UnknownServerConfigurationRuntimeException();
        }
        return serverConfiguration;
    }

    private boolean invalidAddress(String defaultSenderEmail) {
        try {
            new InternetAddress(defaultSenderEmail, true);
            return false;
        } catch (AddressException e) {
            return true;
        }
    }

    private EmailServerConfiguration getSmtpServerProperties(String username, String defaultSenderEmail, String password, Map<String, String> serverProperties) {
        String isSmtpConfig = serverProperties.get(SMTP_SERVER_KEY);
        if(isSmtpConfig == null || !isSmtpConfig.equalsIgnoreCase("true")){
            return null;
        }
        String host = serverProperties.get("mail.smtp.host");
        if(StringUtils.isBlank(host)){
            throw new InvalidBlankHostRuntimeException();
        }
        String port = serverProperties.get("mail.smtp.port");
        if(StringUtils.isBlank(port)){
            throw new InvalidBlankPortRuntimeException();
        }
        return new SmtpServerConfiguration(username, password, defaultSenderEmail, serverProperties);
    }

}
