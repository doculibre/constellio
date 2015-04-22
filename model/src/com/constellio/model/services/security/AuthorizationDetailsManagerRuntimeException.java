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
