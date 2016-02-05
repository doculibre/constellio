package com.constellio.app.services.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class VersionValidatorTest {

	@Test
	public void whenIsValidVersionThenOk() {
		assertThat(VersionValidator.isValidVersion("2.3.lol")).isFalse();
		assertThat(VersionValidator.isValidVersion("2.3-1lol")).isFalse();
		assertThat(VersionValidator.isValidVersion("2.3-lol")).isFalse();
		assertThat(VersionValidator.isValidVersion("2.3lol")).isFalse();
		assertThat(VersionValidator.isValidVersion("29999.3.00.99.11-23-")).isFalse();
		assertThat(VersionValidator.isValidVersion("29999.3.00.99.11-")).isFalse();
		assertThat(VersionValidator.isValidVersion("")).isFalse();
		assertThat(VersionValidator.isValidVersion("12.")).isFalse();

		assertThat(VersionValidator.isValidVersion("2.3")).isTrue();
		assertThat(VersionValidator.isValidVersion("2.3-1")).isTrue();
		assertThat(VersionValidator.isValidVersion("29999.3.00.99.11")).isTrue();
		assertThat(VersionValidator.isValidVersion("29999.3.00.99.11-23")).isTrue();
	}
}
