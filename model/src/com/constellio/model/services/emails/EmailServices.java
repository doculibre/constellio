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
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.services.emails.EmailServicesException.EmailPermanentException;
import com.constellio.model.services.emails.EmailServicesException.EmailServerException;
import com.constellio.model.services.emails.EmailServicesException.EmailTempException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import java.net.UnknownHostException;
import java.util.Properties;

public class EmailServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailQueueManager.class);

    public void sendEmail(Message message) throws Exception {
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            Exception exception = throwAppropriateException(e);
            throw exception;
        }
    }

    public Exception throwAppropriateException(MessagingException e) throws EmailServerException, EmailTempException, EmailPermanentException {
        LOGGER.warn(e.getMessage(), e);
        if(e instanceof AuthenticationFailedException ||
                e instanceof NoSuchProviderException){
            return new EmailServerException(e);
        } else if (e instanceof MessageRemovedException
                || e instanceof MethodNotSupportedException
                || e instanceof ReadOnlyFolderException
                || e instanceof FolderNotFoundException
                || e instanceof SendFailedException) {
            return new EmailPermanentException(e);
        }else {//FolderClosedException, MailConnectException, ParseException, SendFailedException,
                // StoreClosedException, IllegalWriteException, SearchException, SMTPAddressSucceededException
            Throwable cause = e.getCause();
            if(cause != null && cause instanceof UnknownHostException){
                return new EmailServerException(e);
            }
            return new EmailTempException(e);
        }
    }

    public Session openSession(EmailServerConfiguration configuration) throws EmailServerException {
        if(configuration == null){
            throw new EmailServerException(new EmailServicesException.NullEmailServerException());
        }
        final String username = configuration.getUsername();
        final String password = configuration.getPassword();

        Properties props = new Properties();
        props.putAll(configuration.getProperties());

        return Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    public void closeSession(Session session) {
    }

}
