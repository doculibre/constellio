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
