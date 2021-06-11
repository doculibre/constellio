package com.constellio.model.services.emails;

import com.constellio.model.services.emails.EmailServicesRuntimeException.EmailServicesRuntimeException_CannotGetStore;
import com.constellio.model.services.emails.EmailServicesRuntimeException.EmailServicesRuntimeException_MessagingException;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class EmailServicesAcceptanceTest extends ConstellioTest {

	EmailServices emailServices;
	SmtpServerTestConfig serverConfig;
	Session session;
	MimeMessage email;
	String subject;
	Map<String, String> from;
	private long MAX_TRY = 10;

	@Before
	public void setup()
			throws Exception {
		emailServices = new EmailServices();
		serverConfig = new SmtpServerTestConfig();
		session = emailServices.openSession(new SmtpServerTestConfig());
		prepareValidEmail(session);
	}

	@InternetTest
	// Confirm @SlowTest
	@Test
	public void whenSendEmailThenOk()
			throws Exception {
		prepareSystem(withZeCollection());
		email.setRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testIMAPUsername()));
		emailServices.sendEmail(email);

		List<Message> messages = getEmailById(email.getSubject());
		try {
			assertThat(messages).hasSize(1);
			assertThat(messages.get(0).getAllRecipients()).hasSize(1);
			assertThat(messages.get(0).getSubject()).isEqualTo(email.getSubject());
		} finally {
			assertThat(cleanInbox()).isFalse();
		}
	}

	private void prepareValidEmail(Session session)
			throws MessagingException {
		subject = "subject " + DateTime.now().getMillis() + ", random " + new Random().nextLong();
		email = new MimeMessage(session);
		email.setSubject(subject);
		email.setFrom(new InternetAddress(SDKPasswords.testSMTPUsername()));
		email.setRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testPOP3Username()));
		email.setText("Message");
	}

	// Confirm @SlowTest
	@Test(expected = EmailServicesException.EmailServerException.class)
	public void givenInvalidServerParametersWhenSendEmailThenEmailServerException()
			throws Exception {
		SmtpServerTestConfig spy = spy(serverConfig);
		doReturn("invalidPassword").when(spy).getPassword();
		session = emailServices.openSession(spy);
		prepareValidEmail(session);
		emailServices.sendEmail(email);
	}

	// Confirm @SlowTest
	@Test(expected = EmailServicesException.EmailServerException.class)
	public void givenInvalidCredentialsWhenSendEmailThenEmailServerException()
			throws Exception {
		SmtpServerTestConfig spy = spy(serverConfig);
		Map<String, String> invalidProperties = new HashMap<>();
		invalidProperties.put("mail.smtp.host", "invalidHost");
		doReturn(invalidProperties).when(spy).getProperties();
		session = emailServices.openSession(spy);
		prepareValidEmail(session);
		emailServices.sendEmail(email);
	}

	// Confirm @SlowTest
	@Test(expected = EmailServicesException.EmailPermanentException.class)
	public void givenInvalidEmailWhenSendEmailThenEmailPermanentException()
			throws Exception {
		email.setRecipient(Message.RecipientType.TO, new InternetAddress("invalidEmail"));
		emailServices.sendEmail(email);
	}

	@Test
	public void givenUserHasAFullNameThenSetupMessageWithNameInExpeditor() throws IOException, MessagingException {

		MimeMessage message = emailServices.createMimeMessage("joe@acme.com", "Test", "This is a test",
				new ArrayList<>(), getAppLayerFactory().getModelLayerFactory().getSystemConfigs(), "Joe Jo");

		assertThat(message.getFrom()[0].toString()).isEqualTo("Joe Jo <joe@acme.com>");
	}

	private List<Message> getEmailById(final String id) {
		List<Message> returnList = new ArrayList<>();
		Properties props = new Properties();

		String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.pop3.socketFactory.fallback", "false");
		props.put("mail.pop3.socketFactory.port", "995");
		props.put("mail.pop3.port", "995");
		props.put("mail.pop3.host", "pop.gmail.com");
		props.put("mail.pop3.user", SDKPasswords.testPOP3Username());
		props.put("mail.store.protocol", "pop3");

		Session session = Session.getDefaultInstance(props, null);
		Store store;
		Folder inbox;
		long tryCount = 0;
		try {
			store = session.getStore("pop3");
			store.connect("pop.gmail.com", SDKPasswords.testPOP3Username(), SDKPasswords.testPOP3Password());
			inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);

			while (returnList.isEmpty() && tryCount < MAX_TRY) {
				FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

				Message[] messages = inbox.search(unseen);
				System.out.println(tryCount + ", " + messages.length);
				for (Message message : messages) {
					if (message.getSubject() != null && message.getSubject().startsWith(id)) {
						returnList.add(message);
					}
				}
				tryCount++;
				Thread.sleep(100);
			}
		} catch (NoSuchProviderException e) {
			throw new EmailServicesRuntimeException_CannotGetStore("imaps", e);
		} catch (MessagingException e) {
			throw new EmailServicesRuntimeException_MessagingException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return returnList;
	}

	private boolean cleanInbox() {
		AtomicBoolean errorEncountered = new AtomicBoolean(false);

		Store store = null;
		Folder inbox = null;
		try {
			store = session.getStore("imaps");
			store.connect("smtp.gmail.com", SDKPasswords.testIMAPUsername(), SDKPasswords.testIMAPPassword());
			inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);

			FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			Arrays.stream(inbox.search(unseen)).forEach(message -> {
				try {
					message.setFlag(Flag.DELETED, true);
				} catch (MessagingException e) {
					errorEncountered.set(true);
				}
			});

		} catch (Exception e) {
			errorEncountered.set(true);
		} finally {
			try {
				inbox.close(true);
				store.close();
			} catch (Exception ignored) {
			}
		}

		return errorEncountered.get();
	}

}
