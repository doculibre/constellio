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
import java.util.UUID;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.notifications.Notification;
import com.constellio.model.services.search.query.logical.criteria.CriteriaUtils;

public class NotificationsServices {

	RecordDao notificationDao;
	UniqueIdGenerator uniqueIdGenerator;
	ModelLayerConfiguration modelLayerConfiguration;

	public NotificationsServices(RecordDao notificationDao, UniqueIdGenerator uniqueIdGenerator,
			ModelLayerConfiguration modelLayerConfiguration) {
		super();
		this.notificationDao = notificationDao;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.modelLayerConfiguration = modelLayerConfiguration;
	}

	public void markAsSeen(Notification notification) {
		updateNotificationDateField(notification, "seenOn_dt");
	}

	public void markAsSent(Notification notification) {
		updateNotificationDateField(notification, "sentOn_dt");
	}

	public void createNotifications(String idEvent, List<String> usernames) {
		List<RecordDTO> notifications = new ArrayList<>();
		for (String username : usernames) {
			Notification notification = new Notification(username, idEvent, TimeProvider.getLocalDateTime());
			notifications.add(newNotificationRecordDTO(notification, username));
		}
		try {
			notificationDao.execute(new TransactionDTO(RecordsFlushing.WITHIN_SECONDS(5)).withNewRecords(notifications));
		} catch (OptimisticLocking optimisticLocking) {
			throw new ImpossibleRuntimeException("Only adding records, so this exception is impossible");
		}
	}

	public void createNotifications(RecordDTO eventRecordDTO, List<String> usernames) {

		List<RecordDTO> notifications = new ArrayList<>();
		for (String username : usernames) {
			Notification notification = new Notification(username, eventRecordDTO.getId(), TimeProvider.getLocalDateTime());
			notifications.add(newNotificationRecordDTO(notification, username));
		}
		try {
			notificationDao.execute(new TransactionDTO(RecordsFlushing.WITHIN_SECONDS(5)).withNewRecords(notifications));
		} catch (OptimisticLocking optimisticLocking) {
			throw new ImpossibleRuntimeException("Only adding records, so this exception is impossible");
		}
	}

	public List<Notification> getNotificationsToSend() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		int minutes = modelLayerConfiguration.getDelayBeforeSendingNotificationEmailsInMinutes();
		String nullDate = getNullDate();

		LocalDateTime xMinutesBefore = TimeProvider.getLocalDateTime().minusMinutes(minutes);

		params.set("q", "createdOn_dt:[* TO " + xMinutesBefore + "Z] AND seenOn_dt:\"" + nullDate + "\" AND sentOn_dt:\""
				+ nullDate + "\"");
		params.set("fq", "type_s:notification");
		params.set("sort", "createdOn_dt asc, id asc");
		params.set("start", 0);
		params.set("rows", 10000000);

		List<RecordDTO> recordDTOs = notificationDao.searchQuery(params);
		List<Notification> notifications = new ArrayList<>();
		for (RecordDTO recordDTO : recordDTOs) {
			notifications.add(toNotification(recordDTO));
		}
		return notifications;
	}

	public Notification getNotificationById(String id) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "type_s:notification AND id:" + id);
		params.set("start", 0);
		params.set("rows", 1);

		List<RecordDTO> recordDTOs = notificationDao.searchQuery(params);
		if (!recordDTOs.isEmpty()) {
			return toNotification(recordDTOs.get(0));
		}
		return null;
	}

	public Notification getNotificationByUserAndEvent(String username, String eventId) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "type_s:notification AND username_s:" + username + " AND event_s:" + eventId);
		params.set("start", 0);
		params.set("rows", 1);

		List<RecordDTO> recordDTOs = notificationDao.searchQuery(params);
		if (!recordDTOs.isEmpty()) {
			return toNotification(recordDTOs.get(0));
		}
		return null;
	}

	public List<Notification> getUnseenAndUnsentNotificationByUser(String username) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		String nullDate = getNullDate();
		params.set("q", "type_s:notification AND username_s:" + username + " AND seenOn_dt:\"" + nullDate + "\" AND sentOn_dt:\""
				+ nullDate + "\"");
		params.set("start", 0);
		params.set("rows", 10000000);

		List<RecordDTO> recordDTOs = notificationDao.searchQuery(params);
		List<Notification> notifications = new ArrayList<>();
		if (!recordDTOs.isEmpty()) {
			for (RecordDTO recordDTO : recordDTOs) {
				notifications.add(toNotification(recordDTO));
			}
		}
		return notifications;
	}

	private void updateNotificationDateField(Notification notification, String dateField) {
		RecordDTO recordDTO = newNotificationRecordDTO(notification, notification.getUser());
		Map<String, Object> modifiedFields = new HashMap<>();
		modifiedFields.put(dateField, TimeProvider.getLocalDateTime());
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(recordDTO, modifiedFields, recordDTO.getFields());

		try {
			notificationDao.execute(
					new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.WITHIN_SECONDS(5),
							new ArrayList<RecordDTO>(), Arrays.asList(recordDeltaDTO)));
		} catch (OptimisticLocking e) {
			throw new ImpossibleRuntimeException("Only adding records, so this exception is impossible");
		}
	}

	private Notification toNotification(RecordDTO recordDTO) {
		String id = recordDTO.getId();
		String username = (String) recordDTO.getFields().get("username_s");
		String idEvent = (String) recordDTO.getFields().get("event_s");
		LocalDateTime seenOn = (LocalDateTime) recordDTO.getFields().get("seenOn_dt");
		LocalDateTime sentOn = (LocalDateTime) recordDTO.getFields().get("sentOn_dt");
		LocalDateTime createdOn = (LocalDateTime) recordDTO.getFields().get("createdOn_dt");
		return new Notification(id, username, idEvent, createdOn, seenOn, sentOn);
	}

	private RecordDTO newNotificationRecordDTO(Notification notification, String username) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("username_s", username);
		fields.put("event_s", notification.getIdEvent());
		fields.put("type_s", "notification");
		fields.put("createdOn_dt", notification.getCreatedOn());
		fields.put("seenOn_dt", notification.getSeenOn());
		fields.put("sentOn_dt", notification.getSentOn());
		String id;
		if (notification.getId() == null) {
			id = uniqueIdGenerator.next();
		} else {
			id = notification.getId();
		}
		return new RecordDTO(id, fields);
	}

	private String getNullDate() {
		return CriteriaUtils.getNullDateValue();
	}
}
