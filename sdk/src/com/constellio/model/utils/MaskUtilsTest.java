package com.constellio.model.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MaskUtilsTest {

	@Test
	public void whenValidatingValidValueThenTrue()
			throws Exception {

		assertThat(MaskUtils.isValid("***-***-**A9", "123-666-42A2")).isTrue();
		assertThat(MaskUtils.isValid("***-***-**A9", "123-666-42B2")).isTrue();
		assertThat(MaskUtils.isValid("***-***-**A9", "123-666-4242")).isFalse();
		assertThat(MaskUtils.isValid("***-***-**A9", "A23-666-42A2")).isTrue();
		assertThat(MaskUtils.isValid("***-***-**A9", "A23-666-42B2")).isTrue();
		assertThat(MaskUtils.isValid("***-***-**A9", "A23-666-42CC")).isFalse();

		assertThat(MaskUtils.isValid("999-999-9999", "123-666-4242")).isTrue();
		assertThat(MaskUtils.isValid("999-999-9999", "123-666-424")).isFalse();
		assertThat(MaskUtils.isValid("999-999-9999", "123-666-42424")).isFalse();

		assertThat(MaskUtils.isValid("(999) 999-9999", "(123) 666-4242")).isTrue();
		assertThat(MaskUtils.isValid("(999) 999-9999", "123-666-4242")).isFalse();

		assertThat(MaskUtils.isValid("(999) 999-9999 p#\\A\\9999\\*", "(123) 666-4242 p#A9977*")).isTrue();
		assertThat(MaskUtils.isValid("(999) 999-9999 p#\\A\\9999\\*", "(123) 666-4242")).isFalse();
		assertThat(MaskUtils.isValid("(999) 999-9999 p#\\A\\9999\\*", "(123) 666-4242 P#A1998*")).isFalse();
		assertThat(MaskUtils.isValid("(999) 999-9999 p#\\A\\9999\\*", "(123) 666-4242 P#A99989")).isFalse();
		assertThat(MaskUtils.isValid("(999) 999-9999 p#\\A\\9999\\*", "(123) 666-4242 P#B9998*")).isFalse();

		assertThat(MaskUtils.isValid("A9A 9A9", "G1N 2C9")).isTrue();
		assertThat(MaskUtils.isValid("A9A 9A9", "G1N2C9")).isFalse();
		assertThat(MaskUtils.isValid("A9A 9A9", "G1N 2C9 ")).isFalse();
		assertThat(MaskUtils.isValid("A9A 9A9", " G1N 2C9")).isFalse();
		assertThat(MaskUtils.isValid("A9A 9A9", "G1N 209")).isFalse();
		assertThat(MaskUtils.isValid("A9A 9A9", "G1N 2c9")).isTrue();
		assertThat(MaskUtils.isValid("A9A 9A9", "G1N 2CY")).isFalse();
	}

	@Test
	public void whenStrictValidateValidValueThenTrue() {
		assertThat(MaskUtils.strictValidate("A9A 9A9", "G1N2C9"));

		assertThat(MaskUtils.strictValidate("999-999-9999", "1236664242")).isTrue();
		assertThat(MaskUtils.strictValidate("999-999-9999", "123666424")).isFalse();
		assertThat(MaskUtils.strictValidate("999-999-9999", "12366642424")).isFalse();

		assertThat(MaskUtils.strictValidate("(999) 999-9999", "1236664242")).isTrue();
		assertThat(MaskUtils.strictValidate("(999) 999-9999", "123666424A")).isFalse();

		assertThat(MaskUtils.strictValidate("(AAA) 999-9999", "BCD6664240")).isTrue();
		assertThat(MaskUtils.strictValidate("(AAA) 999-9999", "123666424A")).isFalse();

		assertThat(MaskUtils.strictValidate("(AAA) AAA-AAAA", "BCDHGFEDCB")).isTrue();
		assertThat(MaskUtils.strictValidate("(AAA) AAA-AAAA", "BCDHG9EDCB")).isFalse();
	}

	@Test
	public void whenStrictFormatValidValueThenTrue()
			throws MaskUtilsException {
		assertThat(MaskUtils.strictFormat("999-999-9999", "1236664242")).isEqualTo("123-666-4242");
		assertThat(MaskUtils.strictFormat("(999) 999-9999", "1236664242")).isEqualTo("(123) 666-4242");
		assertThat(MaskUtils.strictFormat("(AAA) 999-9999", "BCD6664240")).isEqualTo("(BCD) 666-4240");
		assertThat(MaskUtils.strictFormat("(AAA) AAA-AAAA", "BCDEFGHILH")).isEqualTo("(BCD) EFG-HILH");
	}

	@Test
	public void whenstrictFormatWithMissingValuesThenTrue()
			throws MaskUtilsException {
		assertThat(MaskUtils.strictFormatWithMissingValues("(AAA) AAA-AAAA","BCDEFGH")).isEqualTo("(BCD) EFG-HZZZ");
		assertThat(MaskUtils.strictFormatWithMissingValues("999-999-9999", "1236664")).isEqualTo("123-666-4000");
		assertThat(MaskUtils.strictFormatWithMissingValues("(AAA) 999-9999", "AB")).isEqualTo("(ABZ) 000-0000");
		assertThat(MaskUtils.strictFormatWithMissingValues("(AAA) 999-9999", "ABC10")).isEqualTo("(ABC) 100-0000");
		assertThat(MaskUtils.strictFormatWithMissingValues("999-999-9999", "5")).isEqualTo("500-000-0000");
		assertThat(MaskUtils.strictFormatWithMissingValues("999-999-9999", "4185454545")).isEqualTo("418-545-4545");
	}

	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	public void whenstrictFormatWithMissingValuesInvalidValueThenException() throws MaskUtilsException {
		MaskUtils.strictFormatWithMissingValues("(AAA) 999-9999", "5");
		MaskUtils.strictFormatWithMissingValues("(999) 999-9999", "A");
	}

	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	public void whenStrictFormatInvalidValueThenException()
			throws MaskUtilsException {
		assertThat(MaskUtils.strictFormat("999-999-9999", "1"));
	}

	@Test
	public void whenFormattingStringToDecimalThenOk()
			throws Exception {

		assertThat(MaskUtils.format("99999", "1")).isEqualTo("00001");
		assertThat(MaskUtils.format("99999", "42")).isEqualTo("00042");
	}

	@Test
	public void whenNoMaskThenValid()
			throws Exception {

		assertThat(MaskUtils.isValid("", "zeString")).isTrue();
		assertThat(MaskUtils.isValid(null, "zeOtherString")).isTrue();

	}

	//	@Test
	//	public void whenFormatCompatibleValueThenFormatted()
	//			throws Exception {
	//
	//		assertThat(MaskUtils.format("###-###-####", "1236664242")).isEqualTo("123-666-4242");
	//		assertThat(MaskUtils.format("(###) ###-####", "1236664242")).isEqualTo("(123) 666-4242");
	//		assertThat(MaskUtils.format("(###) ###-#### p'####", "1236664242777")).isEqualTo("(123) 666-4242 p#777");
	//
	//	}
	//
	//	@Test
	//	public void whenFormatAFormattedValueThenReturnSameValue()
	//			throws Exception {
	//
	//		assertThat(MaskUtils.format("###-###-####", "123-666-4242")).isEqualTo("123-666-4242");
	//		assertThat(MaskUtils.format("(###) ###-####", "(123) 666-4242")).isEqualTo("(123) 666-4242");
	//		assertThat(MaskUtils.format("(###) ###-#### p'####", "(123) 666-4242 p#777")).isEqualTo("(123) 666-4242 p#777");
	//	}

	//	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	//	public void whenFormatValueWithMissingCharactersThenException()
	//			throws Exception {
	//
	//		String phoneMask = "###-###-####";
	//		String phoneNumber = "123666424";
	//
	//		MaskUtils.format(phoneMask, phoneNumber);
	//
	//	}

	//	@Test(expected = MaskUtilsException.MaskUtilsException_InvalidValue.class)
	//	public void whenFormatValueWithInvalidCharactersThenException()
	//			throws Exception {
	//
	//		String phoneMask = "###-###-####";
	//		String phoneNumber = "123-666-4242p";
	//
	//		MaskUtils.format(phoneMask, phoneNumber);
	//
	//	}

}
