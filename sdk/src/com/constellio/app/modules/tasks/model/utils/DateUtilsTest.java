package com.constellio.app.modules.tasks.model.utils;

import com.constellio.workflows.model.enums.CalendarCountry;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

	@Test
	public void whenAddingTwoWorkingDaysToAWednesdayInQCCalendarThenCalculatedDateIsFriday() {
		LocalDate calculatedDate = DateUtils.addWorkingDays(new LocalDate(2020, 01, 22), 2, CalendarCountry.CAQC);

		assertThat(calculatedDate).isEqualTo(new LocalDate(2020, 01, 24));
	}

	@Test
	public void whenAddingFiveWorkingDaysToAWednesdayInQCCalendarThenCalculatedDateIsNextWednesday() {
		LocalDate calculatedDate = DateUtils.addWorkingDays(new LocalDate(2020, 01, 22), 5, CalendarCountry.CAQC);

		assertThat(calculatedDate).isEqualTo(new LocalDate(2020, 01,
				29));
	}

	@Test
	public void whenAddingTwoWorkingDaysToAWednesdayInAECalendarThenCalculatedDateIsSunday() {
		LocalDate calculatedDate = DateUtils.addWorkingDays(new LocalDate(2020, 01, 22), 2, CalendarCountry.AE);

		assertThat(calculatedDate).isEqualTo(new LocalDate(2020, 01, 26));
	}

	@Test
	public void whenAddingThreeWorkingDaysToAMondayWithANewYearSHolidayInWednesdayInAECalendarThenCalculatedDateIsMonday() {
		LocalDate calculatedDate = DateUtils.addWorkingDays(new LocalDate(2019, 12, 30), 3, CalendarCountry.AE);

		assertThat(calculatedDate).isEqualTo(new LocalDate(2020, 01, 5));
	}

	@Test
	public void whenAddingThreeWorkingDaysToAMondayWithANewYearSHolidayInWednesdayInQCCalendarThenCalculatedDateIsFriday() {
		LocalDate calculatedDate = DateUtils.addWorkingDays(new LocalDate(2019, 12, 30), 3, CalendarCountry.CAQC);

		assertThat(calculatedDate).isEqualTo(new LocalDate(2020, 01, 3));
	}

}
