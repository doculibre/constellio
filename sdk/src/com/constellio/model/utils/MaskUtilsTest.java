package com.constellio.model.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MaskUtilsTest {

	@Test
	public void whenValidatingValidValueThenTrue()
			throws Exception {

		assertThat(MaskUtils.isValid("###-###-####", "123-666-4242")).isTrue();
		assertThat(MaskUtils.isValid("###-###-####", "123-666-424")).isFalse();
		assertThat(MaskUtils.isValid("###-###-####", "123-666-42424")).isFalse();
		assertThat(MaskUtils.isValid("### ### ####", "123-666-4242")).isFalse();

		assertThat(MaskUtils.isValid("(###) ###-####", "(123) 666-4242")).isTrue();
		assertThat(MaskUtils.isValid("(###) ###-####", "123-666-4242")).isFalse();

		assertThat(MaskUtils.isValid("(###) ###-#### p'####", "(123) 666-4242 p#777")).isTrue();
		assertThat(MaskUtils.isValid("(###) ###-#### p'####", "(123) 666-4242")).isFalse();
		assertThat(MaskUtils.isValid("(###) ###-#### p'####", "(123) 666-4242 P#777")).isFalse();

		assertThat(MaskUtils.isValid("U#U #U#", "G1N 2C9")).isTrue();
		assertThat(MaskUtils.isValid("U#U #U#", "G1N2C9")).isFalse();
		assertThat(MaskUtils.isValid("U#U #U#", "G1N 2C9 ")).isFalse();
		assertThat(MaskUtils.isValid("U#U #U#", " G1N 2C9")).isFalse();
		assertThat(MaskUtils.isValid("U#U #U#", "G1N 209")).isFalse();
		assertThat(MaskUtils.isValid("U#U #U#", "G1N 2c9")).isFalse();
		assertThat(MaskUtils.isValid("U#U #U#", "G1N 2CY")).isFalse();

		assertThat(MaskUtils.isValid("L#L #L#", "g1n 2c9")).isTrue();
		assertThat(MaskUtils.isValid("L#L #L#", "g1n 209")).isFalse();
		assertThat(MaskUtils.isValid("L#L #L#", "g1n 2C9")).isFalse();
		assertThat(MaskUtils.isValid("L#L #L#", "g1n 2cy")).isFalse();
		assertThat(MaskUtils.isValid("L#L #L#", "g1n 2cY")).isFalse();
	}

	@Test
	public void whenFormatCompatibleValueThenFormatted()
			throws Exception {

		assertThat(MaskUtils.format("###-###-####", "1236664242")).isEqualTo("123-666-4242");
		assertThat(MaskUtils.format("(###) ###-####", "1236664242")).isEqualTo("(123) 666-4242");
		assertThat(MaskUtils.format("(###) ###-#### p'####", "1236664242777")).isEqualTo("(123) 666-4242 p#777");

	}

	@Test
	public void whenFormatAFormattedValueThenReturnSameValue()
			throws Exception {

		assertThat(MaskUtils.format("###-###-####", "123-666-4242")).isEqualTo("123-666-4242");
		assertThat(MaskUtils.format("(###) ###-####", "(123) 666-4242")).isEqualTo("(123) 666-4242");
		assertThat(MaskUtils.format("(###) ###-#### p'####", "(123) 666-4242 p#777")).isEqualTo("(123) 666-4242 p#777");
	}

	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	public void whenFormatValueWithMissingCharactersThenException()
			throws Exception {

		String phoneMask = "###-###-####";
		String phoneNumber = "123666424";

		MaskUtils.format(phoneMask, phoneNumber);

	}

	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	public void whenFormatValueWithInvalidCharactersThenException()
			throws Exception {

		String phoneMask = "###-###-####";
		String phoneNumber = "123-666-4242p";

		MaskUtils.format(phoneMask, phoneNumber);

	}

}
