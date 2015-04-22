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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class PlainTextEventEmailBuilderAcceptanceTest extends ConstellioTest {

	PlainTextEventEmailBuilder plainTextEventEmailBuilder;
	EmailBuilder emailBuilder;
	Email email;
	Map<String, String> from;
	Map<String, String> invalidTo;
	Map<String, String> to;
	List<Event> events;
	Event event;
	LocalDateTime shishOClock = new LocalDateTime().minusHours(1);

	String eventId;

	@Before
	public void setup()
			throws Exception {

		givenCollection(zeCollection);
		givenTimeIs(shishOClock);
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		event = schemasRecordsServices.newEvent().setRecordId("recordId")
				.setType(EventType.CREATE_DOCUMENT).setUsername("bob");
		getModelLayerFactory().newRecordServices().add(event.getWrappedRecord());
		eventId = event.getId();
		events = new ArrayList<>();
		events.add(event);

		from = new HashMap<>();
		from.put("noreply.doculibre@gmail.com", "Doculibre");
		invalidTo = new HashMap<>();
		invalidTo.put("doculibredoculibre.com", "Doculibre");
		to = new HashMap<>();
		to.put("noreply.doculibre2@gmail.com", "Client");
		emailBuilder = new EmailBuilder();
		plainTextEventEmailBuilder = new PlainTextEventEmailBuilder();

	}

	@Test
	public void whenBuildEmailThenOk()
			throws Exception {

		emailBuilder = EmailBuilder.setFrom(emailBuilder, from);
		emailBuilder = EmailBuilder.setTo(emailBuilder, to);
		plainTextEventEmailBuilder.buildContent(emailBuilder, events);
		plainTextEventEmailBuilder.buildSubject(emailBuilder, events);

		email = emailBuilder.build();

		assertThat(email.getFrom()).isEqualTo(emailBuilder.getFrom()).isEqualTo(from);
		assertThat(email.getTo()).isEqualTo(emailBuilder.getTo()).isEqualTo(to);
		assertThat(email.getSubject()).endsWith(emailBuilder.getSubject()).endsWith(
				"Notification(s) d'event(s): create_document recordId ");
		assertThat(email.getContent()).isEqualTo(emailBuilder.getContent()).isEqualTo(
				eventId + " recordId create_document on " + shishOClock + " by bob in " + zeCollection
						+ "\n\n");
	}
}
