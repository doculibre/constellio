package com.constellio.model.conf.email;

public class EmailServerConfigurationRuntimeException extends RuntimeException{
    public static class InvalidPropertiesRuntimeException extends RuntimeException {
    }

    public static class InvalidBlankUsernameRuntimeException extends RuntimeException {
    }

    public static class InvalidBlankPasswordRuntimeException extends RuntimeException {
    }

    public static class UnknownServerConfigurationRuntimeException extends RuntimeException {
    }

    public static class InvalidBlankHostRuntimeException extends RuntimeException {
    }

    public static class InvalidBlankPortRuntimeException extends RuntimeException {
    }

    public static class InvalidEmailAddressRuntimeException extends RuntimeException {
        public InvalidEmailAddressRuntimeException(String defaultSenderEmail) {
            super("Invalid email address " + defaultSenderEmail);
        }
    }
}
