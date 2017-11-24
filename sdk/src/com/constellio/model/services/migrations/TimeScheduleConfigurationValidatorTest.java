package com.constellio.model.services.migrations;

import static com.constellio.model.services.migrations.TimeScheduleConfigurationValidator.isCurrentlyInSchedule;
import static com.constellio.model.services.migrations.TimeScheduleConfigurationValidator.isValid;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class TimeScheduleConfigurationValidatorTest extends ConstellioTest {

	@Test
	public void givenScheduleThenInScheduleWhenCurrentHourIsHigherOrEqualThanStartAndLowerThanEnd()
			throws Exception {
		givenTimeIs(new LocalDateTime(2017, 1, 1, 1, 59, 3));
		assertThat(isCurrentlyInSchedule("01-02")).isTrue();
		assertThat(isCurrentlyInSchedule("22-02")).isTrue();
		assertThat(isCurrentlyInSchedule("22-01")).isFalse();
		assertThat(isCurrentlyInSchedule("00-01")).isFalse();
		assertThat(isCurrentlyInSchedule("00-02")).isTrue();
		assertThat(isCurrentlyInSchedule("02-03")).isFalse();
		assertThat(isCurrentlyInSchedule("")).isTrue();
		assertThat(isCurrentlyInSchedule(null)).isTrue();

		givenTimeIs(new LocalDateTime(2017, 1, 1, 2, 0, 3));
		assertThat(isCurrentlyInSchedule("01-02")).isFalse();
		assertThat(isCurrentlyInSchedule("22-02")).isFalse();
		assertThat(isCurrentlyInSchedule("22-01")).isFalse();
		assertThat(isCurrentlyInSchedule("00-01")).isFalse();
		assertThat(isCurrentlyInSchedule("02-03")).isTrue();
		assertThat(isCurrentlyInSchedule("01-03")).isTrue();
		assertThat(isCurrentlyInSchedule("22-03")).isTrue();
		assertThat(isCurrentlyInSchedule("")).isTrue();
		assertThat(isCurrentlyInSchedule(null)).isTrue();

		givenTimeIs(new LocalDateTime(2017, 1, 1, 22, 0, 3));
		assertThat(isCurrentlyInSchedule("20-02")).isTrue();
		assertThat(isCurrentlyInSchedule("22-02")).isTrue();
		assertThat(isCurrentlyInSchedule("23-01")).isFalse();
		assertThat(isCurrentlyInSchedule("")).isTrue();
		assertThat(isCurrentlyInSchedule(null)).isTrue();

	}

	@Test
	public void givenScheduleThenOnlyValidIf4DigitsSeparatedByADash()
			throws Exception {

		assertThat(isValid("01-02")).isTrue();
		assertThat(isValid("22-01")).isTrue();
		assertThat(isValid("22-1")).isFalse();
		assertThat(isValid("1-2")).isFalse();
		assertThat(isValid("2201")).isFalse();
		assertThat(isValid("")).isTrue();
		assertThat(isValid("a")).isFalse();
		assertThat(isValid(null)).isTrue();

	}
}
