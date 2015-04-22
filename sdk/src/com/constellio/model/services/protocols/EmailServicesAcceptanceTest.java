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
package com.constellio.model.services.protocols;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.services.notifications.EmailServicesRuntimeException.EmailServicesRuntimeException_CannotGetStore;
import com.constellio.model.services.notifications.EmailServicesRuntimeException.EmailServicesRuntimeException_MessagingException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

public class EmailServicesAcceptanceTest extends ConstellioTest {

	EmailServices emailServices;
	EmailBuilder emailBuilder;
	Email email;
	Map<String, String> from;
	Map<String, String> invalidTo;
	Map<String, String> to;

	@Before
	public void setup()
			throws Exception {

		emailServices = getModelLayerFactory().newEmailServices();
		emailBuilder = new EmailBuilder();

		from = new HashMap<>();
		from.put("noreply.doculibre@gmail.com", "Doculibre");
		invalidTo = new HashMap<>();
		invalidTo.put("doculibredoculibre.com", "Doculibre");
		to = new HashMap<>();
		to.put("noreply.doculibre2@gmail.com", "Client");
	}

	@SlowTest
	@Test
	public void whenSendEmailThenOk()
			throws Exception {

		EmailBuilder.setFrom(emailBuilder, from);
		EmailBuilder.setTo(emailBuilder, to);
		EmailBuilder.setContent(emailBuilder, "<h1>Message<h1>");
		EmailBuilder.setSubject(emailBuilder, "Subject");

		Email email = emailBuilder.build();
		emailServices.sendEmail(email);

		List<Message> messages = getEmailById(email.getId());
		assertThat(messages).hasSize(1);
		assertThat(messages.get(0).getAllRecipients()).hasSize(1);
		Map.Entry<String, String> entry = to.entrySet().iterator().next();
		assertThat(messages.get(0).getAllRecipients()[0].toString())
				.isEqualTo(entry.getValue() + " <" + entry.getKey() + ">");
		assertThat(messages.get(0).getSubject()).isEqualTo(email.getSubject());
		assertThat(messages.get(0).getContent().toString().trim()).isEqualTo(email.getContent().toString().trim());
	}

	private List<Message> getEmailById(final String id) {
		Properties pop = new Properties();

		String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		pop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
		pop.setProperty("mail.pop3.socketFactory.fallback", "false");
		pop.setProperty("mail.pop3.port", "995");
		pop.setProperty("mail.pop3.socketFactory.port", "995");

		Session session = Session.getDefaultInstance(pop, null);
		Store store;
		Folder inbox;
		Message messages[] = new Message[0];
		try {
			do {
				store = session.getStore("imaps");
				store.connect("imap.gmail.com", "noreply.doculibre2@gmail.com", "ncix123$");
				inbox = store.getFolder("Inbox");
				inbox.open(Folder.READ_WRITE);
				SearchTerm searchTerm = new SearchTerm() {
					@Override
					public boolean match(Message msg) {
						try {
							if (!msg.getSubject().startsWith(id)) {
								return false;
							} else {
								return true;
							}
						} catch (MessagingException e) {
							throw new RuntimeException(e);
						}
					}
				};
				messages = inbox.search(searchTerm);
			} while (messages.length == 0);
		} catch (NoSuchProviderException e) {
			throw new EmailServicesRuntimeException_CannotGetStore("imaps", e);
		} catch (MessagingException e) {
			throw new EmailServicesRuntimeException_MessagingException(e);
		}
		return new ArrayList<Message>(Arrays.asList(messages));
	}
}
