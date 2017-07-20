package com.constellio.model.services.emails;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.emails.EmailServicesException.EmailPermanentException;
import com.constellio.model.services.emails.EmailServicesException.EmailServerException;
import com.constellio.model.services.emails.EmailServicesException.EmailTempException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;

// TODO Nouha: Fix test
public class EmailQueueManagerAcceptanceTest extends ConstellioTest {
	EmailQueueManager emailQueueManager;
	@Mock EmailServices emailServices;
	private SearchServices searchServices;
	private RecordServices recordServices;
	LocalDateTime now = new LocalDateTime();
	SchemasRecordsServices schemas;
	private SchemasRecordsServices businessSchemas;
	EmailConfigurationsManager emailConfigurationsManager;
	EmailAddress testEmail;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers(),
				withCollection(businessCollection).withConstellioRMModule().withAllTestUsers()
		);

		givenTimeIs(now);
		emailConfigurationsManager = getModelLayerFactory().getEmailConfigurationsManager();
		emailQueueManager = spy(new EmailQueueManager(getModelLayerFactory(), emailServices));
		searchServices = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		businessSchemas = new SchemasRecordsServices(businessCollection, getModelLayerFactory());
		testEmail = new EmailAddress("name", SDKPasswords.testPOP3Username());

		getModelLayerFactory().getEmailConfigurationsManager().addEmailServerConfiguration(new SmtpServerTestConfig(),
				zeCollection);
		getModelLayerFactory().getEmailConfigurationsManager().addEmailServerConfiguration(new SmtpServerTestConfig(),
				businessCollection);
	}

	@Test
	public void givenRecordWithMaxTryThenWhenNotSentCorrectlyThenDeleted()
			throws EmailServerException {
		addEmailToSend(EmailQueueManager.MAX_TRY_SEND, now.minusDays(1));
		when(emailServices.openSession(any(EmailServerConfiguration.class))).thenThrow(new EmailServerException(new Exception()));
		emailQueueManager.sendEmails();
		assertNoEmailToSend();
	}

	@Test
	public void givenRecordWithNumberOfTryLowerThanMaxTryThenWhenNotSentCorrectlyThenPostponed()
			throws EmailServerException {
		addEmailToSend(EmailQueueManager.MAX_TRY_SEND - 1, now.minusDays(1));
		when(emailServices.openSession(any(EmailServerConfiguration.class))).thenThrow(new EmailServerException(new Exception()));
		emailQueueManager.sendEmails();
		assertOneEmailToSendWithDateAndWithNumberOfTry(now.plusDays(1), EmailQueueManager.MAX_TRY_SEND);
	}

	@Test
	public void givenValidRecordWhenSentCorrectlyThenRemoved()
			throws Exception {
		addEmailToSend(0, now.minusDays(1), SDKPasswords.testPOP3Username());
		emailQueueManager.sendEmails();
		verify(emailServices, times(1)).sendEmail(any(MimeMessage.class));
		assertNoEmailToSend();
	}

	@Test
	public void givenTwoValidRecordsInZeCollectionAndThreeValidEmailInBusinessCollectionWhenSentCorrectlyThenRemoved()
			throws Exception {
		EmailQueueManager.SEND_EMAIL_BATCH = 1;
		addEmailToSend(0, now.minusDays(2));
		addEmailToSend(1, now.minusDays(1));
		addEmailToSendInBusinessCollection(1, now.minusDays(1));
		addEmailToSendInBusinessCollection(0, now.minusDays(1));
		addEmailToSendInBusinessCollection(1, now.minusDays(3));
		assertThat(searchServices.getResultsCount(from(schemas.emailToSend()).returnAll())).isEqualTo(2);
		assertThat(searchServices.getResultsCount(from(businessSchemas.emailToSend()).returnAll())).isEqualTo(3);

		emailQueueManager.sendEmails();

		verify(emailServices, times(5)).sendEmail(any(MimeMessage.class));
		assertNoEmailToSend();
	}

	@Test
	public void givenEmailWithBlankFromThenRemoved()
			throws Exception {
		addEmailToSend(0, now.minusDays(1), "");
		getModelLayerFactory().getEmailConfigurationsManager().updateEmailServerConfiguration(new SmtpServerTestConfig() {
			@Override
			public String getDefaultSenderEmail() {
				return null;
			}
		}, zeCollection, true);
		emailQueueManager.sendEmails();
		verify(emailServices, times(0)).sendEmail(any(MimeMessage.class));
		assertNoEmailToSend();
	}

	@Test
	public void givenValidRecordWhenEmailTempExceptionThenPostponed()
			throws Exception {
		addEmailToSend(0, now.minusDays(1), SDKPasswords.testPOP3Username());
		doThrow(new EmailTempException(new Exception())).when(emailServices)
				.sendEmail(any(MimeMessage.class));
		emailQueueManager.sendEmails();
		assertOneEmailToSendWithDateAndWithNumberOfTry(now.plusDays(1), 1);
	}

	@Test
	public void givenValidRecordWhenEmailPermanentExceptionThenDeleted()
			throws Exception {
		addEmailToSend(0, now.minusDays(1));
		doThrow(new EmailPermanentException(new Exception())).when(emailServices)
				.sendEmail(any(MimeMessage.class));
		emailQueueManager.sendEmails();
		assertNoEmailToSend();
	}

	@Test
	public void givenSmtpServerIsDisabledThenEmailToSendDeletedAndNoInteractionWithEmailServices()
			throws Exception {
		emailConfigurationsManager.updateEmailServerConfiguration(new SmtpServerTestConfig().setEnabled(false), zeCollection, true);

		addEmailToSend(0, now.minusDays(1));
		addEmailToSend(0, now.minusDays(1));
		addEmailToSend(0, now.minusDays(1));

		emailQueueManager.sendEmails();

		assertNoEmailToSend();
		verifyZeroInteractions(emailServices);
	}

	@Test
	public void given19EmailsFailedToBeSentWhenThe20ththrowAPermanentExceptionThenDisableSmtpServer()
			throws Exception {

		doThrow(EmailPermanentException.class).when(emailServices).sendEmail(any(MimeMessage.class));

		for (int i = 0; i < EmailQueueManager.MAXIMUM_FAILURES_BEFORE_DISABLING_SMTP_SERVER - 1; i++) {
			addEmailToSend(0, now.minusDays(1));
		}
		emailQueueManager.sendEmails();

		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isTrue();

		addEmailToSend(0, now.minusDays(1));
		emailQueueManager.sendEmails();
		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isFalse();
	}

	@Test
	public void given19EmailsFailedToBeSentWhenThe20ththrowATemporaryExceptionThenDisableSmtpServer()
			throws Exception {

		doThrow(EmailTempException.class).when(emailServices).sendEmail(any(MimeMessage.class));

		for (int i = 0; i < EmailQueueManager.MAXIMUM_FAILURES_BEFORE_DISABLING_SMTP_SERVER - 1; i++) {
			addEmailToSend(0, now.minusDays(1));
		}
		emailQueueManager.sendEmails();

		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isTrue();

		addEmailToSend(0, now.minusDays(1));
		emailQueueManager.sendEmails();
		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isFalse();
	}

	@Test
	public void given19EmailsFailedToBeSentWhenThe20thSucceedThenSmtpServerStillEnabled()
			throws Exception {

		doThrow(EmailPermanentException.class).when(emailServices).sendEmail(any(MimeMessage.class));

		for (int i = 0; i < EmailQueueManager.MAXIMUM_FAILURES_BEFORE_DISABLING_SMTP_SERVER - 1; i++) {
			addEmailToSend(0, now.minusDays(1));
		}
		emailQueueManager.sendEmails();

		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isTrue();

		doNothing().when(emailServices).sendEmail(any(MimeMessage.class));
		addEmailToSend(0, now.minusDays(1));
		emailQueueManager.sendEmails();
		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isTrue();

		doThrow(EmailPermanentException.class).when(emailServices).sendEmail(any(MimeMessage.class));
		addEmailToSend(0, now.minusDays(1));
		emailQueueManager.sendEmails();
		assertThat(emailConfigurationsManager.getEmailConfiguration(zeCollection, false).isEnabled()).isTrue();
	}

	private void assertNoEmailToSend() {
		LogicalSearchCondition condition = from(schemas.emailToSend()).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		assertThat(searchServices.getResultsCount(query)).isEqualTo(0);
	}

	private void assertOneEmailToSendWithDateAndWithNumberOfTry(LocalDateTime expectedDate, double expectedNumberOfTry) {
		assertThatRecord(
				searchServices.searchSingleResult(from(schemas.emailToSend()).returnAll()))
				.hasMetadataValue(schemas.emailToSend().getMetadata(EmailToSend.SEND_ON), expectedDate)
				.hasMetadataValue(schemas.emailToSend().getMetadata(EmailToSend.TRYING_COUNT), expectedNumberOfTry);
	}

	private EmailToSend addEmailToSend(double numberOfTry, LocalDateTime sendDate, String fromEmail) {
		EmailToSend email = schemas.newEmailToSend().setTryingCount(numberOfTry).setSendOn(sendDate)
				.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		if (StringUtils.isNotBlank(fromEmail)) {
			EmailAddress from = new EmailAddress(fromEmail, fromEmail);
			email = email.setFrom(from);
		}

		try {
			recordServices.add(email);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return email;
	}

	private EmailToSend addEmailToSend(int numberOfTry, LocalDateTime sendDate) {
		return addEmailToSend(numberOfTry, sendDate, null);
	}

	private EmailToSend addEmailToSendInBusinessCollection(double numberOfTry, LocalDateTime sendDate) {
		EmailToSend email = businessSchemas.newEmailToSend().setTryingCount(numberOfTry).setSendOn(sendDate)
				.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);
		try {
			recordServices.add(email);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return email;
	}
}
