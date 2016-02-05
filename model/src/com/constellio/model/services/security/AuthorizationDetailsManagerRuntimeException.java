package com.constellio.model.services.security;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

@SuppressWarnings("serial")
public class AuthorizationDetailsManagerRuntimeException extends RuntimeException {

	public AuthorizationDetailsManagerRuntimeException(String message) {
		super(message);
	}

	public AuthorizationDetailsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationDetailsManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AuthorizationDetailsAlreadyExists extends AuthorizationDetailsManagerRuntimeException {

		public AuthorizationDetailsAlreadyExists(String code) {
			super("Authorization " + code + " already exists");
		}
	}

	public static class AuthorizationDetailsDoesntExist extends AuthorizationDetailsManagerRuntimeException {

		public AuthorizationDetailsDoesntExist(String code) {
			super("Authorization " + code + " doesn't exist");
		}
	}

	public static class StartDateGreaterThanEndDate extends AuthorizationDetailsManagerRuntimeException {

		public StartDateGreaterThanEndDate(LocalDate startDate, LocalDate endDate) {
			super("start date " + startDate.toString() + " is greater than end date" + endDate.toString());
		}
	}

	public static class EndDateLessThanCurrentDate extends AuthorizationDetailsManagerRuntimeException {

		public EndDateLessThanCurrentDate(String endDate) {
			super("end date " + endDate + " is less than current date" + new LocalDateTime().toString());
		}
	}
}
