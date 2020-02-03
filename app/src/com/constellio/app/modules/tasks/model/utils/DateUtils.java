package com.constellio.app.modules.tasks.model.utils;

import com.constellio.workflows.model.enums.CalendarCountry;
import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.joda.time.LocalDate;

import java.net.URL;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public class DateUtils {

	public static LocalDate addWorkingDays(LocalDate date, int days, CalendarCountry countryCode) {
		LocalDate result = date;
		int addedDays = 0;
		while (addedDays < days) {
			result = result.plusDays(1);
			java.time.LocalDate convertedDate = java.time.LocalDate.of(result.getYear(), result.getMonthOfYear(), result.getDayOfMonth());
			if (!getHolidayManager(countryCode.getCountry()).isHoliday(convertedDate, countryCode.getCountryAndState()) && !isWeekend(convertedDate, countryCode)) {
				++addedDays;
			}
		}
		return result;
	}

	private static boolean isWeekend(java.time.LocalDate date, CalendarCountry country) {
		switch (country) {
			case AE:
				return date.getDayOfWeek() == FRIDAY || date.getDayOfWeek() == SATURDAY;
			case CAQC:
				return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
			default:
				return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
		}
	}

	private static HolidayManager getHolidayManager(String suffix) {
		String path = "holidays/Holidays_" + suffix + ".xml";
		URL resource = DateUtils.class.getClassLoader().getResource(path);
		ManagerParameter params = ManagerParameters.create(resource);
		return HolidayManager.getInstance(params);
	}

}
