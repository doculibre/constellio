package com.constellio.model.services.emails;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EmailBuilderAcceptTest extends ConstellioTest {

	private static final String TEMPLATE_TITLE = "templateTitle";
	private static final String TEMPLATE_TITLE_VALUE_WIHT_HTML_CODE = "templateTitle &acirc;&eacute;";
	private static final String TEMPLATE_TITLE_FINAL_VALUE = "templateTitle âé";
	private static final String CONTENT = "content";
	private static final String SIGNATURE = "signature";
	private static final String CONTENT_VALUE = "contentValue";
	private static final String SIGNATURE_VALUE = "signatureValue";
	public static final String TEST_SUBJECT = "test subject";
	EmailBuilder builder;
	@Mock EmailTemplatesManager emailTemplatesManagerMock;
	EmailTemplatesManager emailTemplatesManager;
	String html =
			"<html> <title>${" + TEMPLATE_TITLE + "}</title> <body><table><tr><td background=\"cid:"
			+ EmailTemplatesManager.BACKGROUND_ID
			+ "\" align=\"center\" valign=\"top\">\n"
			+ "\"<img src=\"cid" + EmailTemplatesManager.LOGO_ID + "\"> <p style=\"color:#088A08;\">${" + CONTENT + "}" +
			" ${" + CONTENT + "}</p> ${" + SIGNATURE + "}</p> </td></tr>"
			+ "<tr><td><div><a href=\"${completeTask}\" style=\"text-decoration:none; color:inherit;\">Completer</a></div></td></tr>"
			+ "</table> </body> </html>";
	String finalHtml =
			"<html> <title>" + TEMPLATE_TITLE_VALUE_WIHT_HTML_CODE + "</title> <body><table><tr><td background=\"cid:"
			+ EmailTemplatesManager.BACKGROUND_ID
			+ "\" align=\"center\" valign=\"top\">\n"
			+ "\"<img src=\"cid" + EmailTemplatesManager.LOGO_ID + "\"> <p style=\"color:#088A08;\">" + CONTENT_VALUE +
			" " + CONTENT_VALUE + "</p> " + SIGNATURE_VALUE + "</p> </td></tr>"
			+ "<tr><td><div><a href=\"http://localhost:7070/constellio/\" style=\"text-decoration:none; color:inherit;\">Completer</a></div></td></tr>"
			+ "</table> </body> </html>";
	EmailToSend emailToSend;
	RecordServices recordServices;
	MetadataSchemasManager metadataSchemasManager;
	static final String TEMPLATE = RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID;
	static final String CONSTELLIO = "Constellio";

	//	<a href="${completeTask}"
	//	style="text-decoration:none; color:inherit;">Completer</a>

	EmailAddress emailAddressChuck;
	List<EmailAddress> emailAddressesTo, emailAddressesBcc, emailAddressesCc;
	List<String> parameters;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers());

		recordServices = getModelLayerFactory().newRecordServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		configureEmailFields();

		emailToSend = newEmailToSend();

		emailTemplatesManager = getModelLayerFactory().getEmailTemplatesManager();
		when(emailTemplatesManagerMock.getCollectionTemplate(any(String.class), any(String.class))).thenReturn(html);
		builder = new EmailBuilder(emailTemplatesManager, getModelLayerFactory().getSystemConfigurationsManager(), getModelLayerFactory().getContentManager());
		//		builder = new EmailBuilder(emailTemplatesManagerMock);
	}

	@Test
	public void whenBuildMessageThenOk()
			throws Exception {

		builder = new EmailBuilder(emailTemplatesManagerMock, getModelLayerFactory().getSystemConfigurationsManager(), getModelLayerFactory().getContentManager());

		Properties props = System.getProperties();

		Session session = Session.getDefaultInstance(props, null);
		Message message = builder.build(emailToSend, session, null);

		assertThat(message.getSubject()).isEqualTo(TEST_SUBJECT);

		InternetAddress[] from = (InternetAddress[]) message.getFrom();
		InternetAddress[] to = (InternetAddress[]) message.getRecipients(RecipientType.TO);
		InternetAddress[] cc = (InternetAddress[]) message.getRecipients(RecipientType.CC);
		InternetAddress[] bcc = (InternetAddress[]) message.getRecipients(RecipientType.BCC);

		assertThat(from.length).isEqualTo(1);
		assertThat(from[0].getAddress()).isEqualTo(emailToSend.getFrom().getEmail());
		assertThat(from[0].getPersonal()).isEqualTo(emailToSend.getFrom().getName());

		assertThat(to.length).isEqualTo(2);
		assertThat(to[0].getAddress()).isEqualTo(emailToSend.getTo().get(0).getEmail());
		assertThat(to[0].getPersonal()).isEqualTo(emailToSend.getTo().get(0).getName());
		assertThat(to[1].getAddress()).isEqualTo(emailToSend.getTo().get(1).getEmail());
		assertThat(to[1].getPersonal()).isEqualTo(emailToSend.getTo().get(1).getName());

		assertThat(cc.length).isEqualTo(1);
		assertThat(cc[0].getAddress()).isEqualTo(emailToSend.getCC().get(0).getEmail());
		assertThat(cc[0].getPersonal()).isEqualTo(emailToSend.getCC().get(0).getName());

		assertThat(bcc.length).isEqualTo(1);
		assertThat(bcc[0].getAddress()).isEqualTo(emailToSend.getBCC().get(0).getEmail());
		assertThat(bcc[0].getPersonal()).isEqualTo(emailToSend.getBCC().get(0).getName());

		assertThat(message.getSubject()).isEqualTo(TEST_SUBJECT);
		assertThat(message.getSentDate()).isEqualToIgnoringMillis(emailToSend.getSendOn().toDate());
		assertThat(message.getReplyTo()).isEqualTo(from);
		MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
		assertThat(mimeMultipart.getCount()).isEqualTo(2);
		assertThat(mimeMultipart.getBodyPart(0).getContent()).isEqualTo(finalHtml);
	}

	@Test
	public void whenBuildMessageWithNullFromThenReturnDefaultFrom()
			throws Exception {

		builder = new EmailBuilder(emailTemplatesManagerMock, getModelLayerFactory().getSystemConfigurationsManager(), getModelLayerFactory().getContentManager());

		Properties props = System.getProperties();

		Session session = Session.getDefaultInstance(props, null);
		String defaultFrom = new SmtpServerTestConfig().getDefaultSenderEmail();
		emailToSend.setFrom(null);
		Message message = builder.build(emailToSend, session, defaultFrom);

		assertThat(message.getFrom()[0].toString()).isEqualTo(defaultFrom);
	}

	@Test
	public void whenBuildMessageWithSubjectFromThenReturnTitleTemplateAsSubject()
			throws Exception {

		builder = new EmailBuilder(emailTemplatesManagerMock, getModelLayerFactory().getSystemConfigurationsManager(), getModelLayerFactory().getContentManager());

		Properties props = System.getProperties();

		Session session = Session.getDefaultInstance(props, null);
		String defaultFrom = new SmtpServerTestConfig().getDefaultSenderEmail();
		emailToSend.setSubject(null);
		Message message = builder.build(emailToSend, session, defaultFrom);

		assertThat(message.getSubject()).isEqualTo(TEMPLATE_TITLE_FINAL_VALUE);
	}

	@Test(expected = AddressException.class)
	public void givenInvalidEmailWhenBuildMessageThenException()
			throws Exception {

		final String authEmail = "invalidEmail";
		Properties props = System.getProperties();

		EmailAddress emailAddress = new EmailAddress(CONSTELLIO, authEmail);
		Session session = Session.getDefaultInstance(props, null);
		builder.build(newEmailToSend(emailAddress), session, null);
	}

	@InternetTest
	@SlowTest
	@Test
	public void realSendTest()
			throws Exception {
		SmtpServerTestConfig smtpServerConfig = new SmtpServerTestConfig();

		Properties props = new Properties();
		props.putAll(smtpServerConfig.getProperties());

		Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SDKPasswords.testSMTPUsername(), SDKPasswords.testSMTPPassword());
			}
		};

		builder = new EmailBuilder(getModelLayerFactory().getEmailTemplatesManager(),
				getModelLayerFactory().getSystemConfigurationsManager(), getModelLayerFactory().getContentManager());
		Session session = Session.getInstance(props, auth);
		Message message = builder.build(emailToSend, session, null);

		Transport.send(message);
	}

	//
	private void configureEmailFields() {
		emailAddressChuck = new EmailAddress("Chuck Norris", "chuck@gmail.com");

		EmailAddress emailAddressBob = new EmailAddress("Bob", SDKPasswords.testSMTPUsername());
		EmailAddress emailAddressAlice = new EmailAddress("Bob", "bob@gmail.com");
		EmailAddress emailAddressGandalf = new EmailAddress("Gandalf", "gandalf@gmail.com");
		EmailAddress emailAddressEdouard = new EmailAddress("Edouard", "edouard@gmail.com");

		emailAddressesTo = new ArrayList<>();
		emailAddressesTo.add(emailAddressBob);
		emailAddressesTo.add(emailAddressAlice);

		emailAddressesBcc = new ArrayList<>();
		emailAddressesBcc.add(emailAddressGandalf);

		emailAddressesCc = new ArrayList<>();
		emailAddressesCc.add(emailAddressEdouard);

		parameters = new ArrayList<>();
		String content = CONTENT + EmailToSend.PARAMETER_SEPARATOR + CONTENT_VALUE;
		String signature = SIGNATURE + ":" + SIGNATURE_VALUE;
		String inxestentParameter = "inxestent" + EmailToSend.PARAMETER_SEPARATOR + "Ze inxestent";
		parameters.add(TEMPLATE_TITLE + EmailToSend.PARAMETER_SEPARATOR + TEMPLATE_TITLE_VALUE_WIHT_HTML_CODE);
		parameters.add(content);
		parameters.add(signature);
		parameters.add(inxestentParameter);
		parameters.add("completeTask" + EmailToSend.PARAMETER_SEPARATOR + "http://localhost:7070/constellio/");
		parameters.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + LocalDate.now());
		parameters.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + chuckNorris);
		parameters.add("borrowedRecordTitle" + EmailToSend.PARAMETER_SEPARATOR + "dossier école");
		parameters.add("borrowedRecordType" + EmailToSend.PARAMETER_SEPARATOR + $("SendReturnReminderEmailButton.folder"));
	}

	public EmailToSend newEmailToSend()
			throws RecordServicesException {
		EmailToSend emailToSend = newEmailToSendRecord();

		emailToSend.setFrom(emailAddressChuck);
		emailToSend.setTo(emailAddressesTo);
		emailToSend.setBCC(emailAddressesBcc);
		emailToSend.setCC(emailAddressesCc);
		emailToSend.setParameters(parameters);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime().plusDays(1));
		emailToSend.setTemplate(TEMPLATE);
		emailToSend.setSubject(TEST_SUBJECT);

		recordServices.add(emailToSend);
		return emailToSend;
	}

	public EmailToSend newEmailToSend(EmailAddress emailAddress)
			throws RecordServicesException {
		EmailToSend emailToSend = newEmailToSendRecord();

		emailToSend.setFrom(emailAddress);
		emailToSend.setTo(Arrays.asList(emailAddress));
		emailToSend.setParameters(parameters);
		emailToSend.setSendOn(TimeProvider.getLocalDateTime().plusMinutes(2));
		emailToSend.setTemplate(TEMPLATE);
		emailToSend.setSubject(TEST_SUBJECT);
		emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);

		recordServices.add(emailToSend);
		return emailToSend;
	}

	private EmailToSend newEmailToSendRecord() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}

}