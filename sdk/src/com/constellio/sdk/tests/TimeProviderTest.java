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
package com.constellio.sdk.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateMidnight;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.data.utils.TimeProvider;

//Order is important to test this test mecanism
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeProviderTest extends ConstellioTest {

	LocalDate nowDate;
	LocalDateTime nowDateTime;

	LocalDateTime tenYearsBeforeDateTime = new LocalDateTime().minusYears(10);
	LocalDate tenYearsBeforeDate = new LocalDate().minusYears(10);
	LocalDateTime tenYearsBeforeMidnightDateTime = new DateMidnight().toDateTime().toLocalDateTime().minusYears(10);

	LocalDateTime fiveYearsBeforeDateTime = new LocalDateTime().minusYears(5);
	LocalDate fiveYearsBeforeDate = new LocalDate().minusYears(5);
	LocalDateTime fiveYearsBeforeMidnightDateTime = new DateMidnight().toDateTime().toLocalDateTime().minusYears(5);

	@BeforeClass
	public static void printSystemTime() {
		System.out.println("Current time is '" + new LocalDateTime().toString() + "'");
	}

	@Before
	public void setUp()
			throws Exception {

		nowDate = new LocalDate();
		nowDateTime = new LocalDateTime();
	}

	@Test
	public void test1_givenNoFixedTimeThenWorkNormally() {
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), nowDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(nowDate);
	}

	@Test
	public void test2_givenFixedDateTimeThenDateTimeIsFixed() {
		givenTimeIs(tenYearsBeforeDateTime);

		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), tenYearsBeforeDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(tenYearsBeforeDate);

	}

	@Test
	public void test2_givenFixedDateThenDateIsFixed() {
		givenTimeIs(tenYearsBeforeDate);

		assertThat(TimeProvider.getLocalDate()).isEqualTo(tenYearsBeforeDate);
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), tenYearsBeforeMidnightDateTime);

	}

	@Test
	public void test4_givenNoFixedTimeThenNowWorkNormally() {
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), nowDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(nowDate);
	}

	@Test
	public void test5_givenFixedChangeDuringTestThenCorrectTimes() {
		givenTimeIs(tenYearsBeforeDateTime);
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), tenYearsBeforeDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(tenYearsBeforeDate);

		givenTimeIs(fiveYearsBeforeDate);
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), fiveYearsBeforeMidnightDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(fiveYearsBeforeDate);

		givenActualTime();
		assertTimeEqualOrNearEqual(TimeProvider.getLocalDateTime(), nowDateTime);
		assertThat(TimeProvider.getLocalDate()).isEqualTo(nowDate);
	}

	private void assertTimeEqualOrNearEqual(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
		int second = Math.abs(Seconds.secondsBetween(localDateTime1, localDateTime2).getSeconds());
		assertThat(second <= 10).isTrue();
	}

}
