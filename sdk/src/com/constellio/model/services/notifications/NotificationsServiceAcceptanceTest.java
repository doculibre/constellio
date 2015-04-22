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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.notifications.Notification;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;

public class NotificationsServiceAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime().plusHours(1);
	LocalDateTime oneSecondAfterShishOClock = new LocalDateTime().plusSeconds(1);
	LocalDateTime tockOClock = shishOClock.plusHours(1);
	LocalDateTime fourtyOneMinutesAfterShishOClock = shishOClock.plusMinutes(41);
	LocalDateTime fourtyOneMinutesAnd59SecondsAfterShishOClock = shishOClock.plusMinutes(41).plusSeconds(59);
	LocalDateTime fourtyTwoMinutesAfterShishOClock = shishOClock.plusMinutes(42);
	LocalDateTime fourtyTwoMinutesAnd1SecondAfterShishOClock = shishOClock.plusMinutes(42).plusSeconds(1);
	LocalDateTime fourtyThreeMinutesAfterShishOClock = shishOClock.plusMinutes(43);

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = zeCollectionSetup.new ZeSchemaMetadatas();

	Users users = new Users();

	RecordServices recordServices;
	LoggingServices loggingServices;
	NotificationsServices notificationsServices;
	List<Event> events;
	Event recordEvent;

	@Before
	public void setUp()
			throws Exception {

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {
				when(configuration.getDelayBeforeSendingNotificationEmailsInMinutes()).thenReturn(42);

			}
		});

		givenCollection(zeCollection);

		recordServices = getModelLayerFactory().newRecordServices();
		loggingServices = getModelLayerFactory().newLoggingServices();
		UserServices userServices = getModelLayerFactory().newUserServices();
		notificationsServices = getModelLayerFactory().newNotificationsServices();
		users.setUp(userServices);
		userServices.addUserToCollection(users.alice(), zeCollection);
		recordServices.add(users.aliceIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true)
				.getWrappedRecord());

		defineSchemasManager().using(zeCollectionSetup);
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", "taxo", zeCollection, Arrays.asList("zeSchemaType"));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy,
				getModelLayerFactory().getMetadataSchemasManager());
	}

	@Test
	public void givenEventWhenCreateNotificationThenItIsCreatedForEachFollowerOfRecord()
			throws Exception {
		events = givenEventsAndNotificationsForWithFollowers("record1");
		recordEvent = (Event) events.get(0);

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(2);
		assertThat(notifications.get(0).getCreatedOn()).isEqualTo(shishOClock);
		assertThat(notifications.get(0).getIdEvent()).isEqualTo(recordEvent.getId());
		assertThat(notifications.get(0).getSeenOn()).isNull();
		assertThat(notifications.get(0).getSentOn()).isNull();
		assertThat(notifications.get(0).getUser()).isEqualTo(users.alice().getUsername());
		assertThat(notifications.get(1).getCreatedOn()).isEqualTo(
				shishOClock);
		assertThat(notifications.get(1).getIdEvent()).isEqualTo(recordEvent.getId());
		assertThat(notifications.get(1).getSeenOn()).isNull();
		assertThat(notifications.get(1).getSentOn()).isNull();
		assertThat(notifications.get(1).getUser()).isEqualTo(users.bob().getUsername());

		assertThat(notifications.get(0)).isEqualToIgnoringGivenFields(notifications.get(1), "id", "user");
	}

	@Test
	public void givenFiveEventsWhenCreateNotificationThenItIsCreatedTenTimes()
			throws Exception {

		events = givenEventsAndNotificationsForWithFollowers("record1", "record2", "record3", "record4", "record5");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(10);
	}

	@Test
	public void givenEventWhenCreateNotificationWithoutUserThenNoNotificationsAreCreated()
			throws Exception {

		givenEventsAndNotificationsForRecordsWithoutFollowers("record1");

		givenTimeIs(fourtyOneMinutesAnd59SecondsAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).isEmpty();
	}

	@Test
	public void given41MinutesOldEventWhenGetNotificationsToSendThenItIsNotRetrieved()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyOneMinutesAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).isEmpty();
	}

	@Test
	public void given41MinutesAnd59SecondsOldEventWhenGetNotificationsToSendThenItIsNotRetrieved()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyOneMinutesAnd59SecondsAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).isEmpty();
	}

	@Test
	public void given42MinutesOldEventWhenGetNotificationsToSendThenItIsNotRetrieved()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyTwoMinutesAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(2);
	}

	@Test
	public void given42MinutesAnd1SecondOldEventWhenGetNotificationsToSendThenItIsNotRetrieved()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(2);
	}

	@Test
	public void given43MinutesAnd1SecondOldEventWhenGetNotificationsToSendThenItIsNotRetrieved()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyThreeMinutesAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(2);
	}

	@Test
	public void whenMarkAsSentThenItIsMarked()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		Notification notification1 = notifications.get(0);
		Notification notification2 = notifications.get(1);

		notificationsServices.markAsSent(notification1);
		notificationsServices.markAsSent(notification2);
		recordServices.flush();

		notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).isEmpty();

	}

	@Test
	public void whenMarkAsSeenThenItIsMarked()
			throws Exception {

		givenTimeIs(shishOClock);
		givenEventsAndNotificationsForWithFollowers("record1");
		givenTimeIs(fourtyThreeMinutesAfterShishOClock);

		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		Notification notification1 = notifications.get(0);
		Notification notification2 = notifications.get(1);

		notificationsServices.markAsSeen(notification1);
		notificationsServices.markAsSeen(notification2);
		recordServices.flush();

		notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).isEmpty();
	}

	@Test
	public void whengetNotificationByUserAndEventIdThenItIsReturned()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		Notification notification1 = notifications.get(0);
		String eventId = notification1.getIdEvent();
		String username = notification1.getUser();

		Notification notificationByUserAndEvent = notificationsServices.getNotificationByUserAndEvent(username, eventId);

		assertThat(notification1).isEqualToComparingFieldByField(notificationByUserAndEvent);
	}

	@Test
	public void whenMarkAsSeenAndAsSentAtSameTimeThenOk()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		List<Notification> notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		Notification notification1 = notifications.get(0);

		givenTimeIs(shishOClock);
		notificationsServices.markAsSeen(notification1);

		givenTimeIs(oneSecondAfterShishOClock);
		notificationsServices.markAsSent(notification1);
		recordServices.flush();

		notification1 = notificationsServices.getNotificationById(notification1.getId());
		assertThat(notification1.getSeenOn()).isEqualTo(shishOClock);
		assertThat(notification1.getSentOn()).isEqualTo(oneSecondAfterShishOClock);

	}

	@Test
	public void whenMarkAsSeenTwiceThenOk()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		List<Notification> notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		Notification notification1 = notifications.get(0);

		notificationsServices.markAsSeen(notification1);

		givenTimeIs(tockOClock);
		notificationsServices.markAsSeen(notification1);
		recordServices.flush();

		notification1 = notificationsServices.getNotificationById(notification1.getId());
		assertThat(notification1.getSeenOn()).isEqualTo(tockOClock);

	}

	@Test
	public void whenNotificationCreatedThenUnseenAndUnsent()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		Notification notification1 = notifications.get(0);

		notification1 = notificationsServices.getNotificationById(notification1.getId());
		assertThat(notification1.getSeenOn()).isNull();
		assertThat(notification1.getSentOn()).isNull();

	}

	@Test
	public void givenNotificationsWhenGetUnseenAndUnsentNotificationToUserThenReturnOne()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		List<Notification> notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		assertThat(notifications).hasSize(1);
	}

	@Test
	public void givenNotificationsAndMarkItAsSeenWhenGetUnseenAndUnsentNotificationToUserThenReturnNone()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		List<Notification> notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		notificationsServices.markAsSeen(notifications.get(0));
		recordServices.flush();

		notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		assertThat(notifications).isEmpty();
	}

	@Test
	public void givenNotificationsAndMarkItAsSentWhenGetUnseenAndUnsentNotificationToUserThenReturnNone()
			throws Exception {

		givenEventsAndNotificationsForWithFollowers("record1");

		List<Notification> notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		notificationsServices.markAsSent(notifications.get(0));
		recordServices.flush();

		notifications = notificationsServices.getUnseenAndUnsentNotificationByUser(users.bob().getUsername());
		assertThat(notifications).isEmpty();
	}

	private List<Event> givenEventsAndNotificationsForRecordsWithoutFollowers(String... records)
			throws RecordServicesException {
		return givenEventsAndNotificationsFor(records, false);
	}

	private List<Event> givenEventsAndNotificationsForWithFollowers(String... records)
			throws RecordServicesException {
		return givenEventsAndNotificationsFor(records, true);
	}

	private List<Event> givenEventsAndNotificationsFor(String[] records, boolean followers)
			throws RecordServicesException {
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction = new Transaction().setUser(users.aliceIn(zeCollection));

		for (String recordId : records) {
			Record record = new TestRecord(zeSchema, recordId);
			transaction.add(record);
			if (followers) {
				addFollowersTo(record);
			}
		}
		recordServices.execute(transaction);
		recordServices.flush();
		List<Event> events = getAllEvents();
		return events;
	}

	private List<Event> getAllEvents() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(schemas.eventSchema()).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.sortDesc(Schemas.CREATED_ON);
		return schemas.wrapEvents(searchServices.search(query));

	}

	private void addFollowersTo(Record record1) {
		List<String> usernames = new ArrayList<>();
		usernames.add(users.alice().getUsername());
		usernames.add(users.bob().getUsername());
		record1.set(Schemas.FOLLOWERS, usernames);
	}
}
