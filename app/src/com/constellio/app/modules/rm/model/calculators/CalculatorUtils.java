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
package com.constellio.app.modules.rm.model.calculators;

import static org.joda.time.Days.daysBetween;

import java.util.List;

import org.joda.time.LocalDate;

public class CalculatorUtils {

	public static LocalDate toNextEndOfYearDateIfNotAlready(LocalDate date, String yearEndStr, int requiredDaysBeforeYearEnd) {
		if (isEndOfYear(date, yearEndStr)) {
			return date;
		} else {
			return toNextEndOfYearDate(date, yearEndStr, requiredDaysBeforeYearEnd);
		}
	}

	public static LocalDate toNextEndOfYearDate(LocalDate date, String yearEndStr, int requiredDaysBeforeYearEnd) {
		if (date == null) {
			return null;
		}
		LocalDate yearEndDate;
		int indexOfSep = yearEndStr.indexOf("/");
		int yearEndMonth = Integer.parseInt(yearEndStr.substring(0, indexOfSep));
		int yearEndDay = Integer.parseInt(yearEndStr.substring(indexOfSep + 1));

		yearEndDate = new LocalDate(date.getYear(), yearEndMonth, yearEndDay);

		if (yearEndDate.isBefore(date)) {
			yearEndDate = yearEndDate.plusYears(1);
		}

		int daysBetweenDateAndYearEndDate = daysBetween(date, yearEndDate).getDays();

		if (daysBetweenDateAndYearEndDate < requiredDaysBeforeYearEnd) {
			yearEndDate = yearEndDate.plusYears(1);
		}
		return yearEndDate;
	}

	public static LocalDate getSmallestDate(List<LocalDate> dates) {

		LocalDate smallest = null;
		for (LocalDate date : dates) {
			if (smallest == null || (date != null && date.isBefore(smallest))) {
				smallest = date;
			}
		}

		return smallest;
	}

	public static boolean isEndOfYear(LocalDate date, String yearEndStr) {
		if (date == null) {
			return false;
		}
		int indexOfSep = yearEndStr.indexOf("/");
		int yearEndMonth = Integer.parseInt(yearEndStr.substring(0, indexOfSep));
		int yearEndDay = Integer.parseInt(yearEndStr.substring(indexOfSep + 1));
		return date.getDayOfMonth() == yearEndDay && date.getMonthOfYear() == yearEndMonth;
	}
}
