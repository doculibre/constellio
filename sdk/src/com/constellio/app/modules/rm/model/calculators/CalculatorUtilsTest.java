package com.constellio.app.modules.rm.model.calculators;

import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.getSmallestDate;
import static com.constellio.app.modules.rm.model.calculators.CalculatorUtils.toNextEndOfYearDate;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;

public class CalculatorUtilsTest {

	@Test
	public void whenGetNextEndOfYearDateThenBasedOnYearEndAndRequiredPeriod()
			throws Exception {

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 1, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 1, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 0, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 0, true)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2013, 4, 1), "04/30", 30, false)).isEqualTo(new LocalDate(2014, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2013, 4, 1), "04/30", 30, true)).isEqualTo(new LocalDate(2014, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 30), "04/30", 30, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 30), "04/30", 30, true)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 31), "04/30", 30, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 31), "04/30", 30, true)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 29), "04/30", 30, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 29), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 30, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "04/30", 30, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 29, false)).isEqualTo(new LocalDate(2012, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 29, true)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 2), "04/30", 29, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 2), "04/30", 29, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, false)).isEqualTo(new LocalDate(2013, 4, 30));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30, true)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "05/31", 30, false)).isEqualTo(new LocalDate(2012, 5, 31));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "05/31", 30, true)).isEqualTo(new LocalDate(2012, 5, 31));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "05/31", 30, false)).isEqualTo(new LocalDate(2012, 5, 31));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "05/31", 30, true)).isEqualTo(new LocalDate(2012, 5, 31));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 2), "05/31", 30, false)).isEqualTo(new LocalDate(2013, 5, 31));
		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 2), "05/31", 30, true)).isEqualTo(new LocalDate(2013, 5, 31));

		assertThat(toNextEndOfYearDate(null, "05/31", 30, false)).isNull();
		assertThat(toNextEndOfYearDate(null, "05/31", 30, true)).isNull();

	}

	@Test
	public void whenTestGetSmallestDateThenReturnSmallestNonNullDate()
			throws Exception {

		assertThat(getSmallestDate(asList(new LocalDate(2012, 4, 1)))).isEqualTo(new LocalDate(2012, 4, 1));
		assertThat(getSmallestDate(asList(null, new LocalDate(2012, 4, 1), null))).isEqualTo(new LocalDate(2012, 4, 1));
		assertThat(getSmallestDate(asList(new LocalDate(2012, 4, 1), new LocalDate(2012, 4, 1)))).isEqualTo(
				new LocalDate(2012, 4, 1));

		assertThat(getSmallestDate(asList(new LocalDate(2012, 7, 2), new LocalDate(2012, 7, 1)))).isEqualTo(
				new LocalDate(2012, 7, 1));

		assertThat(getSmallestDate(asList(new LocalDate(2012, 7, 1), new LocalDate(2012, 7, 2)))).isEqualTo(
				new LocalDate(2012, 7, 1));
	}
}



