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
package com.constellio.model.services.emails;

import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.sdk.SDKPasswords;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

public class SmtpServerTestConfig implements EmailServerConfiguration {
    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        /*
        mail.smtp.host=smtp.gmail.com
mail.smtp.socketFactory.port=465
mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
mail.smtp.auth=true
mail.smtp.port=465
        */

        properties.put("mail.smtp.host", "smtp.gmail.com");//-relay
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        
        return properties;
    }

    @Override
    public String getUsername() {
        return SDKPasswords.testSMTPServerUsername();
    }

    @Override
    public String getPassword() {
        return SDKPasswords.testSMTPServerPassword();
    }

    @Override
    public String getDefaultSenderEmail() {
        return SDKPasswords.testEmailAccount();
    }

    /*public static void main(String[] args) throws EmailServicesException.EmailServerException, MessagingException {
        EmailServerConfiguration serverTestConfig = new SmtpServerTestConfig();

        EmailServices emailServices = new EmailServices();
        Session session = emailServices.openSession(serverTestConfig);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SDKPasswords.testEmailAccount()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testEmailAccount()));
        message.setSubject("subject");
        message.setText("body");
        Transport.send(message);

    }*/
}
