package com.constellio.model.entities.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class EmailAddressFactoryTest extends ConstellioTest {

	EmailAddressFactory factory;
	String name = "Chuck Norris";
	String email = "chuck@gmail.com";

	@Before
	public void setUp()
			throws Exception {
		factory = new EmailAddressFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		EmailAddress emailAddress = new EmailAddress();
		assertThat(emailAddress.isDirty()).isFalse();

		emailAddress = new EmailAddress();
		emailAddress.setEmail(email);
		assertThat(emailAddress.isDirty()).isTrue();

		emailAddress = new EmailAddress();
		emailAddress.setName(name);
		assertThat(emailAddress.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setName(name);
		emailAddress.setEmail(email);

		String stringValue = factory.toString(emailAddress);
		EmailAddress builtEmailAddress = (EmailAddress) factory.build(stringValue);
		String stringValue2 = factory.toString(builtEmailAddress);

		assertThat(builtEmailAddress).isEqualTo(emailAddress);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtEmailAddress.isDirty()).isFalse();
		assertThat(builtEmailAddress.getEmail()).isEqualTo(email);

	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setName(null);
		emailAddress.setEmail(null);

		String stringValue = factory.toString(emailAddress);
		EmailAddress builtEmailAddress = (EmailAddress) factory.build(stringValue);
		String stringValue2 = factory.toString(builtEmailAddress);

		assertThat(builtEmailAddress).isEqualTo(emailAddress);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtEmailAddress.isDirty()).isFalse();
		assertThat(builtEmailAddress.getEmail()).isNull();
	}

	@Test
	public void whenConvertingStructureWithoutSetValuesThenRemainsEqual()
			throws Exception {

		EmailAddress emailAddress = new EmailAddress();

		String stringValue = factory.toString(emailAddress);
		EmailAddress builtEmailAddress = (EmailAddress) factory.build(stringValue);
		String stringValue2 = factory.toString(builtEmailAddress);

		assertThat(builtEmailAddress).isEqualTo(emailAddress);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtEmailAddress.isDirty()).isFalse();
		assertThat(builtEmailAddress.getEmail()).isNull();
	}

	@Test
	public void whenToAddressThenOk()
			throws Exception {
		EmailAddress emailAddress = new EmailAddress(name, email);

		String stringValue = factory.toAddress(emailAddress);

		assertThat(stringValue).isEqualTo(name + " <" + email + ">");
	}
}