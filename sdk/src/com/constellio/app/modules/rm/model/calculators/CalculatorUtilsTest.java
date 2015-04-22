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

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2013, 4, 1), "04/30", 30)).isEqualTo(new LocalDate(2014, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 30), "04/30", 30)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 3, 31), "04/30", 30)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 29), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 30), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 29)).isEqualTo(new LocalDate(2012, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 2), "04/30", 29)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "04/30", 30)).isEqualTo(new LocalDate(2013, 4, 30));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 4, 1), "05/31", 30)).isEqualTo(new LocalDate(2012, 5, 31));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 1), "05/31", 30)).isEqualTo(new LocalDate(2012, 5, 31));

		assertThat(toNextEndOfYearDate(new LocalDate(2012, 5, 2), "05/31", 30)).isEqualTo(new LocalDate(2013, 5, 31));

		assertThat(toNextEndOfYearDate(null, "05/31", 30)).isNull();

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



