package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListValidationTest extends ConstellioTest {
	public static final String THE_USER = "theUserId";

	DecomListValidationFactory factory;
	DecomListValidation validation;

	@Before
	public void setUp() {
		factory = new DecomListValidationFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		validation = new DecomListValidation();
		assertThat(validation.isDirty()).isFalse();

		validation = new DecomListValidation();
		validation.setUserId(THE_USER);
		assertThat(validation.isDirty()).isTrue();

		validation = new DecomListValidation();
		validation.setRequestDate(TimeProvider.getLocalDate());
		assertThat(validation.isDirty()).isTrue();

		validation = new DecomListValidation();
		validation.setValidationDate(TimeProvider.getLocalDate());
		assertThat(validation.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		validation = new DecomListValidation()
				.setUserId(THE_USER)
				.setRequestDate(TimeProvider.getLocalDate().minusDays(1))
				.setValidationDate(TimeProvider.getLocalDate());

		String stringValue = factory.toString(validation);
		DecomListValidation deserialized = (DecomListValidation) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(validation);
		assertThat(deserialized.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {
		validation = new DecomListValidation();

		String stringValue = factory.toString(validation);
		DecomListValidation deserialized = (DecomListValidation) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(validation);
	}
}
