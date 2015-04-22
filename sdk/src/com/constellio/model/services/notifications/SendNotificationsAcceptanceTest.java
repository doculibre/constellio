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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.mockito.Mock;

import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.notifications.Email;
import com.constellio.model.entities.notifications.Notification;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.protocols.EmailServices;
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

public class SendNotificationsAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime().plusHours(1);
	LocalDateTime fourtyTwoMinutesAnd1SecondAfterShishOClock = shishOClock.plusMinutes(42).plusSeconds(1);
	SendNotifications sendNotifications;
	NotificationsServices notificationsServices;
	LoggingServices loggingServices;
	@Mock EmailServices emailServices;
	@Mock SmtpServerConfig smtpServerConfig;
	@Mock Email email;
	UserServices userServices;
	PlainTextEventEmailBuilder plainTextEventEmailBuilder;

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = zeCollectionSetup.new ZeSchemaMetadatas();

	Users users = new Users();

	RecordServices recordServices;
	List<Event> events;
	UserCredential aliceCredential;
	UserCredential bobCredential;

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

		plainTextEventEmailBuilder = new PlainTextEventEmailBuilder();
		recordServices = getModelLayerFactory().newRecordServices();
		loggingServices = getModelLayerFactory().newLoggingServices();
		userServices = getModelLayerFactory().newUserServices();
		notificationsServices = getModelLayerFactory().newNotificationsServices();
		sendNotifications = spy(new SendNotifications(getModelLayerFactory(), plainTextEventEmailBuilder));

		users.setUp(userServices);

		aliceCredential = userServices.getUser(users.alice().getUsername());
		bobCredential = userServices.getUser(users.bob().getUsername());

		userServices.addUserToCollection(users.alice(), zeCollection);
		recordServices.add(users.aliceIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true)
				.getWrappedRecord());

		defineSchemasManager().using(zeCollectionSetup);
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", "taxo", zeCollection, Arrays.asList("zeSchemaType"));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy,
				getModelLayerFactory().getMetadataSchemasManager());
	}

	//@Test //TODO Francis
	public void givenEventsForTwoWhenSendNotificationThenItIsSendTwice()
			throws Exception {

		givenEventsAndNotifications();
		doReturn(email).when(sendNotifications).createNotificationEmail(any(UserCredential.class), any(List.class));

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		List<Notification> notifications = notificationsServices.getNotificationsToSend();
		assertThat(notifications).hasSize(4);

		sendNotifications.run();

		verify(emailServices, times(2)).sendEmail(email);
		recordServices.flush();
		List<Notification> retrievedNotifications = notificationsServices.getNotificationsToSend();
		assertThat(retrievedNotifications).isEmpty();
	}

	//@Test //TODO Francis
	public void givenEventsForTwoWhenSendNotificationTwiceThenItIsSendOnlyTwice()
			throws Exception {
		givenEventsAndNotifications();
		when(emailServices.getSmtpServerConfig()).thenReturn(smtpServerConfig);
		when(smtpServerConfig.getEmail()).thenReturn("noreply.doculibre@gmail.com");
		when(smtpServerConfig.getUser()).thenReturn("Doculibre");

		givenTimeIs(fourtyTwoMinutesAnd1SecondAfterShishOClock);
		sendNotifications.run();
		recordServices.flush();
		sendNotifications.run();

		verify(emailServices, times(2)).sendEmail(any(Email.class));
	}

	private List<Event> givenEventsAndNotifications()
			throws RecordServicesException {
		givenTimeIs(shishOClock);
		Record record1 = new TestRecord(zeSchema, "record1");
		addFollowersTo(record1);
		Record record2 = new TestRecord(zeSchema, "record2");
		addFollowersTo(record2);
		Transaction transaction = new Transaction().setUser(users.aliceIn(zeCollection));
		transaction.add(record1);
		transaction.add(record2);
		recordServices.execute(transaction);
		recordServices.flush();
		events = getAllEvents();
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
