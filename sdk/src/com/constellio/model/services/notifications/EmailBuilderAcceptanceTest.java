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
package com.constellio.model.services.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.services.notifications.EmailBuilderRuntimeException.EmailBuilderRuntimeException_InvalidEmail;
import com.constellio.model.services.notifications.EmailBuilderRuntimeException.EmailBuilderRuntimeException_MissingAttribute;
import com.constellio.sdk.tests.ConstellioTest;

public class EmailBuilderAcceptanceTest extends ConstellioTest {

	EmailBuilder emailBuilder;
	Email email;
	Map<String, String> from;
	Map<String, String> invalidTo;
	Map<String, String> to;

	@Before
	public void setup()
			throws Exception {

		from = new HashMap<>();
		from.put("doculibre@doculibre.com", "Doculibre");
		invalidTo = new HashMap<>();
		invalidTo.put("doculibredoculibre.com", "Doculibre");
		to = new HashMap<>();
		to.put("client@doculibre.com", "Client");
		emailBuilder = new EmailBuilder();
	}

	@Test
	public void whenBuildEmailThenOk()
			throws Exception {
		emailBuilder = EmailBuilder.setFrom(emailBuilder, from);
		emailBuilder = EmailBuilder.setTo(emailBuilder, to);
		emailBuilder = EmailBuilder.setSubject(emailBuilder, "Subject test");
		emailBuilder = EmailBuilder.setContent(emailBuilder, "Message");

		email = emailBuilder.build();

		assertThat(email.getFrom()).isEqualTo(emailBuilder.getFrom());
		assertThat(email.getTo()).isEqualTo(emailBuilder.getTo());
		assertThat(email.getSubject()).endsWith(emailBuilder.getSubject());
		assertThat(email.getContent()).isEqualTo(emailBuilder.getContent());
	}

	@Test
	public void givenMissingInformationWhenBuildEmailThenException()
			throws Exception {
		emailBuilder = EmailBuilder.setFrom(emailBuilder, from);
		emailBuilder = EmailBuilder.setTo(emailBuilder, to);
		emailBuilder = EmailBuilder.setSubject(emailBuilder, "Subject test");

		try {
			email = emailBuilder.build();
		} catch (EmailBuilderRuntimeException_MissingAttribute e) {
			assertThat(e.getMessage()).isEqualTo("content is missing");
		}

	}

	@Test
	public void givenInvalidEmailFormatWhenBuildEmailThenException()
			throws Exception {
		emailBuilder = EmailBuilder.setFrom(emailBuilder, from);
		emailBuilder = EmailBuilder.setTo(emailBuilder, invalidTo);
		emailBuilder = EmailBuilder.setSubject(emailBuilder, "Subject test");
		emailBuilder = EmailBuilder.setContent(emailBuilder, "Message");

		try {
			email = emailBuilder.build();
		} catch (EmailBuilderRuntimeException_InvalidEmail e) {
			assertThat(e.getMessage()).isEqualTo("doculibredoculibre.com is not valid");
		}
	}

}
