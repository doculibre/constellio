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
