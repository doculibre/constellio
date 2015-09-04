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
package com.constellio.model.services.emails;

import com.constellio.sdk.tests.ConstellioTest;

// TODO Nouha: Fix test
public class EmailQueueManagerAcceptanceTest extends ConstellioTest {
	//	EmailQueueManager emailQueueManager;
	//	@Mock EmailServices emailServices;
	//	private SearchServices searchServices;
	//	private RecordServices recordServices;
	//	LocalDateTime now = new LocalDateTime();
	//	SchemasRecordsServices schemas;
	//	private SchemasRecordsServices businessSchemas;
	//	EmailAddress testEmail;
	//
	//	@Before
	//	public void setUp()
	//			throws Exception {
	//
	//		prepareSystem(
	//				withZeCollection().withConstellioRMModule().withAllTestUsers(),
	//				withCollection(businessCollection).withConstellioRMModule().withAllTestUsers()
	//		);
	//
	//		givenTimeIs(now);
	//		emailQueueManager = spy(new EmailQueueManager(getModelLayerFactory(), emailServices));
	//		searchServices = getModelLayerFactory().newSearchServices();
	//		recordServices = getModelLayerFactory().newRecordServices();
	//		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
	//		businessSchemas = new SchemasRecordsServices(businessCollection, getModelLayerFactory());
	//		testEmail = new EmailAddress("name", SDKPasswords.testEmailAccount());
	//
	//		getModelLayerFactory().getEmailConfigurationsManager().addEmailServerConfiguration(new SmtpServerTestConfig(),
	//				zeCollection);
	//		getModelLayerFactory().getEmailConfigurationsManager().addEmailServerConfiguration(new SmtpServerTestConfig(),
	//				businessCollection);
	//	}
	//
	//	@Test
	//	public void givenRecordWithMaxTryThenWhenNotSentCorrectlyThenDeleted()
	//			throws EmailServerException {
	//		addEmailToSend(EmailQueueManager.MAX_TRY_SEND, now.minusDays(1));
	//		when(emailServices.openSession(any(EmailServerConfiguration.class))).thenThrow(new EmailServerException(new Exception()));
	//		emailQueueManager.sendEmails();
	//		assertNoEmailToSend();
	//	}
	//
	//	@Test
	//	public void givenRecordWithNumberOfTryLowerThanMaxTryThenWhenNotSentCorrectlyThenPostponed()
	//			throws EmailServerException {
	//		addEmailToSend(EmailQueueManager.MAX_TRY_SEND - 1, now.minusDays(1));
	//		when(emailServices.openSession(any(EmailServerConfiguration.class))).thenThrow(new EmailServerException(new Exception()));
	//		emailQueueManager.sendEmails();
	//		assertOneEmailToSendWithDateAndWithNumberOfTry(now.plusDays(1), EmailQueueManager.MAX_TRY_SEND);
	//	}
	//
	//	@Test
	//	public void givenValidRecordWhenSentCorrectlyThenRemoved()
	//			throws Exception {
	//		addEmailToSend(0, now.minusDays(1), SDKPasswords.testEmailAccount());
	//		emailQueueManager.sendEmails();
	//		verify(emailServices, times(1)).sendEmail(any(Message.class));
	//		assertNoEmailToSend();
	//	}
	//
	//	@Test
	//	public void givenTwoValidRecordsInZeCollectionAndThreeValidEmailInBusinessCollectionWhenSentCorrectlyThenRemoved()
	//			throws Exception {
	//		EmailQueueManager.SEND_EMAIL_BATCH = 1;
	//		addEmailToSend(0, now.minusDays(2));
	//		addEmailToSend(1, now.minusDays(1));
	//		addEmailToSendInBusinessCollection(1, now.minusDays(1));
	//		addEmailToSendInBusinessCollection(0, now.minusDays(1));
	//		addEmailToSendInBusinessCollection(1, now.minusDays(3));
	//		assertThat(searchServices.getResultsCount(from(schemas.emailToSend()).returnAll())).isEqualTo(2);
	//		assertThat(searchServices.getResultsCount(from(businessSchemas.emailToSend()).returnAll())).isEqualTo(3);
	//
	//		emailQueueManager.sendEmails();
	//
	//		verify(emailServices, times(5)).sendEmail(any(Message.class));
	//		assertNoEmailToSend();
	//	}
	//
	//	@Test
	//	public void givenEmailWithBlankFromThenRemoved()
	//			throws Exception {
	//		addEmailToSend(0, now.minusDays(1), "");
	//		getModelLayerFactory().getEmailConfigurationsManager().updateEmailServerConfiguration(new SmtpServerTestConfig() {
	//			@Override
	//			public String getDefaultSenderEmail() {
	//				return null;
	//			}
	//		}, zeCollection);
	//		emailQueueManager.sendEmails();
	//		verify(emailServices, times(0)).sendEmail(any(Message.class));
	//		assertNoEmailToSend();
	//	}
	//
	//	@Test
	//	public void givenValidRecordWhenEmailTempExceptionThenPostponed()
	//			throws Exception {
	//		addEmailToSend(0, now.minusDays(1), SDKPasswords.testEmailAccount());
	//		doThrow(new EmailTempException(new Exception())).when(emailServices)
	//				.sendEmail(any(Message.class));
	//		emailQueueManager.sendEmails();
	//		assertOneEmailToSendWithDateAndWithNumberOfTry(now.plusDays(1), 1);
	//	}
	//
	//	@Test
	//	public void givenValidRecordWhenEmailPermanentExceptionThenDeleted()
	//			throws Exception {
	//		addEmailToSend(0, now.minusDays(1));
	//		doThrow(new EmailPermanentException(new Exception())).when(emailServices)
	//				.sendEmail(any(Message.class));
	//		emailQueueManager.sendEmails();
	//		assertNoEmailToSend();
	//	}
	//
	//	private void assertNoEmailToSend() {
	//		LogicalSearchCondition condition = from(schemas.emailToSend()).returnAll();
	//		LogicalSearchQuery query = new LogicalSearchQuery(condition);
	//		assertThat(searchServices.getResultsCount(query)).isEqualTo(0);
	//	}
	//
	//	private void assertOneEmailToSendWithDateAndWithNumberOfTry(LocalDateTime expectedDate, double expectedNumberOfTry) {
	//		assertThatRecord(
	//				searchServices.searchSingleResult(from(schemas.emailToSend()).returnAll()))
	//				.hasMetadataValue(schemas.emailToSend().getMetadata(EmailToSend.SEND_ON), expectedDate)
	//				.hasMetadataValue(schemas.emailToSend().getMetadata(EmailToSend.TRYING_COUNT), expectedNumberOfTry);
	//	}
	//
	//	private EmailToSend addEmailToSend(int numberOfTry, LocalDateTime sendDate, String fromEmail) {
	//		EmailToSend email = schemas.newEmailToSend().setTryingCount(numberOfTry).setSendOn(sendDate)
	//				.setTemplate(EmailTemplatesManager.REMIND_BORROW_TEMPLATE_ID);
	//		if (StringUtils.isNotBlank(fromEmail)) {
	//			EmailAddress from = new EmailAddress(fromEmail, fromEmail);
	//			email = email.setFrom(from);
	//		}
	//
	//		try {
	//			recordServices.add(email);
	//		} catch (RecordServicesException e) {
	//			throw new RuntimeException(e);
	//		}
	//		return email;
	//	}
	//
	//	private EmailToSend addEmailToSend(int numberOfTry, LocalDateTime sendDate) {
	//		return addEmailToSend(numberOfTry, sendDate, null);
	//	}
	//
	//	private EmailToSend addEmailToSendInBusinessCollection(int numberOfTry, LocalDateTime sendDate) {
	//		EmailToSend email = businessSchemas.newEmailToSend().setTryingCount(numberOfTry).setSendOn(sendDate)
	//				.setTemplate(EmailTemplatesManager.REMIND_BORROW_TEMPLATE_ID);
	//		try {
	//			recordServices.add(email);
	//		} catch (RecordServicesException e) {
	//			throw new RuntimeException(e);
	//		}
	//		return email;
	//	}
}
