package com.constellio.app.services.importExport.systemStateExport;

import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemStateExporterTest extends ConstellioTest {

	@Test
	public void whenExtractingTimeFromFilenameThenOk() {

		LocalDateTime dateTime = SystemStateExporter.parseTlogFilename("2020-06-04T15-12-58-589");
		assertThat(dateTime.getYear()).isEqualTo(2020);
		assertThat(dateTime.getMonthOfYear()).isEqualTo(6);
		assertThat(dateTime.getDayOfMonth()).isEqualTo(4);

		assertThat(dateTime.getHourOfDay()).isEqualTo(15);
		assertThat(dateTime.getMinuteOfHour()).isEqualTo(12);
		assertThat(dateTime.getSecondOfMinute()).isEqualTo(58);

		assertThat(dateTime.getMillisOfSecond()).isEqualTo(589);
	}
}
