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

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

public class EmailServicesRuntimeException extends RuntimeException{
    public EmailServicesRuntimeException(String message, Exception e) {
        super(message, e);
    }

    public EmailServicesRuntimeException(MessagingException e) {
        super(e);
    }

    public static class EmailServicesRuntimeException_CannotGetStore extends EmailServicesRuntimeException {
        public EmailServicesRuntimeException_CannotGetStore(String imaps, NoSuchProviderException e) {
            super(imaps, e);
        }
    }

    public static class EmailServicesRuntimeException_MessagingException extends EmailServicesRuntimeException {
        public EmailServicesRuntimeException_MessagingException(MessagingException e) {
            super(e);
        }
    }
}
