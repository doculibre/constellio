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

import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static com.constellio.model.services.emails.EmailServicesRuntimeException.*;

public class EmailServicesAcceptanceTest extends ConstellioTest {

    EmailServices emailServices;
    SmtpServerTestConfig serverConfig;
    Session session;
    Message email;
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

    @SlowTest
    @Test
    public void whenSendEmailThenOk()
            throws Exception {
        email.setRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testIMAPServerUsername()));
        emailServices.sendEmail(email);

        List<Message> messages = getEmailById(email.getSubject());
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getAllRecipients()).hasSize(1);
        assertThat(messages.get(0).getSubject()).isEqualTo(email.getSubject());
    }

    private void prepareValidEmail(Session session) throws MessagingException {
        subject = "subject " + DateTime.now().getMillis() + ", random " + new Random().nextLong();
        email = new MimeMessage(session);
        email.setSubject(subject);
        email.setFrom(new InternetAddress(SDKPasswords.testSMTPServerUsername()));
        email.setRecipient(Message.RecipientType.TO, new InternetAddress(SDKPasswords.testEmailAccount()));
        email.setText("Message");
    }

    @SlowTest
    @Test(expected = EmailServicesException.EmailServerException.class)
    public void givenInvalidServerParametersWhenSendEmailThenEmailServerException()
            throws Exception {
        SmtpServerTestConfig spy = spy(serverConfig);
        doReturn("invalidPassword").when(spy).getPassword();
        session = emailServices.openSession(spy);
        prepareValidEmail(session);
        emailServices.sendEmail(email);
    }

    @SlowTest
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

    @SlowTest
    @Test(expected = EmailServicesException.EmailPermanentException.class)
    public void givenInvalidEmailWhenSendEmailThenEmailPermanentException()
            throws Exception {
        email.setRecipient(Message.RecipientType.TO, new InternetAddress("invalidEmail"));
        emailServices.sendEmail(email);
    }

    private List<Message> getEmailById(final String id) {
        List<Message> returnList = new ArrayList<>();
        Properties pop = new Properties();

        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        pop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop.setProperty("mail.pop3.port", "995");
        pop.setProperty("mail.pop3.socketFactory.port", "995");

        Session session = Session.getDefaultInstance(pop, null);
        Store store;
        Folder inbox;
        long tryCount = 0;
        try {
            store = session.getStore("imaps");
            store.connect("imap.gmail.com", SDKPasswords.testIMAPServerUsername(), SDKPasswords.testPOP3Server());//
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            while(returnList.isEmpty() && tryCount < MAX_TRY) {
                FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

                Message[] messages = inbox.search(unseen);
                System.out.println(tryCount + ", " + messages.length);
                for(Message message: messages){
                    if(message.getSubject().startsWith(id)){
                        returnList.add(message);
                    }
                }
                tryCount ++;
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

}
