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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.services.notifications.HtmlEventEmailBuilderRuntimeException.HtmlEventEmailBuilderRuntimeException_IOException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class HtmlEventEmailBuilderAcceptanceTest extends ConstellioTest {

	HtmlEventEmailBuilder htmlEventEmailBuilder;
	FoldersLocator foldersLocator;
	EmailBuilder emailBuilder;
	Email email;
	Map<String, String> from;
	Map<String, String> invalidTo;
	Map<String, String> to;
	List<Event> events;
	Event event;
	LocalDateTime currentLocalDateTime;

	SchemasRecordsServices schemasRecordsServices;

	@Before
	public void setup()
			throws Exception {

		givenCollection(zeCollection);

		foldersLocator = getModelLayerFactory().getFoldersLocator();
		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		currentLocalDateTime = TimeProvider.getLocalDateTime();
		//event = schemasRecordsServices.newEvent().setRecordId("recordId").setRecordSchema("zeSchemaType_default")
		//		.setType(EventType.CREATE_RECORD).setUsername("bob");
		event = schemasRecordsServices.newEvent().setRecordId("recordId")
				.setType(EventType.CREATE_DOCUMENT).setUsername("bob");
		events = new ArrayList<>();
		events.add(event);

		from = new HashMap<>();
		from.put("noreply.doculibre@gmail.com", "Doculibre");
		invalidTo = new HashMap<>();
		invalidTo.put("doculibredoculibre.com", "Doculibre");
		to = new HashMap<>();
		to.put("noreply.doculibre2@gmail.com", "Client");
		emailBuilder = new EmailBuilder();
		htmlEventEmailBuilder = new HtmlEventEmailBuilder(foldersLocator);

	}

	@Test
	public void whenBuildEmailThenOk()
			throws Exception {

		File eventNotificationMail = null;

		for (File htmlFile : foldersLocator.getSmtpMailFolder().listFiles()) {
			if (htmlFile.getName().equals("eventNotificationMail.html")) {
				eventNotificationMail = htmlFile;
			}
		}
		String htmlContent;
		try {
			Document doc = Jsoup.parse(eventNotificationMail, "UTF-8");
			htmlContent = doc.toString();
		} catch (IOException e) {
			throw new HtmlEventEmailBuilderRuntimeException_IOException(e);
		}

		StringBuffer content = new StringBuffer();
		content.append("<div>");
		for (Event event : events) {
			content.append(event.getId() + " " + event.getRecordId() + " "
					+ event.getType() + " on " + event.getCreatedOn() + " by " + event.getUsername()
					+ " in " + event.getCollection());
			content.append("\n\n");

		}
		content.append("</div>");

		htmlContent = htmlContent.replace("${content}", content.toString());

		EmailBuilder.setFrom(emailBuilder, from);
		EmailBuilder.setTo(emailBuilder, to);
		htmlEventEmailBuilder.buildContent(emailBuilder, events);
		htmlEventEmailBuilder.buildSubject(emailBuilder, events);

		email = emailBuilder.build();

		assertThat(email.getFrom()).isEqualTo(emailBuilder.getFrom()).isEqualTo(from);
		assertThat(email.getTo()).isEqualTo(emailBuilder.getTo()).isEqualTo(to);
		assertThat(email.getSubject()).endsWith(emailBuilder.getSubject()).endsWith(
				"Notification(s) d'event(s): create_document recordId ");
		assertThat(email.getContent()).isEqualTo(emailBuilder.getContent()).isEqualTo(htmlContent);
	}
}
