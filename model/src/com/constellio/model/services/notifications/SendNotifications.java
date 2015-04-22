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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.entities.notifications.Notification;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.protocols.EmailServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

public class SendNotifications implements Runnable {

	NotificationsServices notificationsServices;
	EmailServices emailServices;
	LoggingServices loggingServices;
	UserServices userServices;
	EventEmailBuilder eventEmailBuilder;
	RecordServices recordServices;
	ModelLayerFactory modelLayerFactory;

	public SendNotifications(ModelLayerFactory modelLayerFactory, EventEmailBuilder eventEmailBuilder) {
		this.modelLayerFactory = modelLayerFactory;
		this.notificationsServices = modelLayerFactory.newNotificationsServices();
		this.emailServices = modelLayerFactory.newEmailServices();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.userServices = modelLayerFactory.newUserServices();
		this.eventEmailBuilder = eventEmailBuilder;
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void run() {
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		Map<String, List<Event>> userEventsMap = createUserEventsMap(notifications);
		for (Entry<String, List<Event>> userEventMap : userEventsMap.entrySet()) {
			UserCredential userCredential = userServices.getUser(userEventMap.getKey());
			Email email = createNotificationEmail(userCredential, userEventMap.getValue());
			emailServices.sendEmail(email);
			markNotificationsAsSend(userEventMap);
		}
	}

	Map<String, List<Event>> createUserEventsMap(List<Notification> notifications) {
		Map<String, List<Event>> userEventsMap = new HashMap<>();
		for (Notification notification : notifications) {
			String user = notification.getUser();

			Record eventRecord = recordServices.getDocumentById(notification.getIdEvent());
			Event event = new SchemasRecordsServices(eventRecord.getCollection(), modelLayerFactory).wrapEvent(eventRecord);
			if (userEventsMap.containsKey(user)) {
				List<Event> events = new ArrayList<>();
				events.addAll(userEventsMap.get(user));
				events.add(event);
				userEventsMap.put(user, events);
			} else {
				userEventsMap.put(user, Arrays.asList(event));
			}
		}
		return userEventsMap;
	}

	void markNotificationsAsSend(Entry<String, List<Event>> userNotificationMap) {
		for (Event event : userNotificationMap.getValue()) {
			Notification notificationToMarkAsSent = notificationsServices.getNotificationByUserAndEvent(
					userNotificationMap.getKey(), event.getId());
			notificationsServices.markAsSent(notificationToMarkAsSent);
		}
	}

	Email createNotificationEmail(UserCredential userCredential, List<Event> events) {
		EmailBuilder emailBuilder = new EmailBuilder();

		Map<String, String> from = new HashMap<>();
		from.put(emailServices.getSmtpServerConfig().getEmail(), emailServices.getSmtpServerConfig().getUser());
		Map<String, String> to = new HashMap<>();
		to.put(userCredential.getEmail(), userCredential.getUsername());
		EmailBuilder.setTo(emailBuilder, to);
		EmailBuilder.setFrom(emailBuilder, from);
		eventEmailBuilder.buildSubject(emailBuilder, events);
		eventEmailBuilder.buildContent(emailBuilder, events);
		return emailBuilder.build();
	}
}