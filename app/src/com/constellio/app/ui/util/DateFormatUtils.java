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
package com.constellio.app.ui.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class DateFormatUtils implements Serializable {

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static String formatDate(Date date) {
		return date != null ? new SimpleDateFormat(DATE_FORMAT).format(date) : null;
	}

	public static String formatDateTime(Date dateTime) {
		return dateTime != null ? new SimpleDateFormat(DATE_TIME_FORMAT).format(dateTime) : null;
	}

	public static String format(LocalDate date) {
		return date != null ? date.toString(DATE_FORMAT) : null;
	}

	public static String format(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.toString(DATE_TIME_FORMAT) : null;
	}

}
