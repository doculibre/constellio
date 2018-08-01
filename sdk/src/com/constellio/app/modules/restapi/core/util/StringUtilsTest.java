package com.constellio.app.modules.restapi.core.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

	@Test
	public void testConcat() {
		String expectedResult = "abcABCdefDEF";

		String result = StringUtils.concat("abc", "", "ABC", null, "def", "", "", "DEF");
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testIsUnsignedInteger() {
		boolean result = StringUtils.isUnsignedInteger("123");
		assertThat(result).isTrue();
	}

	@Test
	public void testIsUnsignedIntegerWithDouble() {
		boolean result = StringUtils.isUnsignedInteger("12.55");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedIntegerWithSignedInteger() {
		boolean result = StringUtils.isUnsignedInteger("-12");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedIntegerWithInvalidValue() {
		boolean result = StringUtils.isUnsignedInteger("a1");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedDouble() {
		boolean result = StringUtils.isUnsignedDouble("1.2");
		assertThat(result).isTrue();
	}

	@Test
	public void testIsUnsignedDoubleWithInteger() {
		boolean result = StringUtils.isUnsignedDouble("5");
		assertThat(result).isTrue();
	}

	@Test
	public void testIsUnsignedDoubleWithLeadingDot() {
		boolean result = StringUtils.isUnsignedDouble(".45");
		assertThat(result).isTrue();
	}

	@Test
	public void testIsUnsignedDoubleWithSignedDouble() {
		boolean result = StringUtils.isUnsignedDouble("-1.2");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedDoubleWithInvalidValue() {
		boolean result = StringUtils.isUnsignedDouble("a.45");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedLong() {
		boolean result = StringUtils.isUnsignedLong("123");
		assertThat(result).isTrue();
	}

	@Test
	public void testIsUnsignedLongWithDouble() {
		boolean result = StringUtils.isUnsignedLong("12.55");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedLongWithSignedInteger() {
		boolean result = StringUtils.isUnsignedLong("-12");
		assertThat(result).isFalse();
	}

	@Test
	public void testIsUnsignedLongWithInvalidValue() {
		boolean result = StringUtils.isUnsignedLong("a1");
		assertThat(result).isFalse();
	}

}
