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