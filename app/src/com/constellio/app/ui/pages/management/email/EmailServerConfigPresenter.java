package com.constellio.app.ui.pages.management.email;

import static com.constellio.app.ui.i18n.i18n.$;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.constellio.app.ui.entities.EmailServerConfigVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServicesException;
import com.sun.mail.smtp.SMTPMessage;

public class EmailServerConfigPresenter extends BasePresenter<EmailServerConfigView> {
	public static Logger LOGGER = Logger.getLogger(EmailServerConfigPresenter.class);

	public EmailServerConfigPresenter(EmailServerConfigView view) {
		super(view);
	}

	public void saveButtonClicked(EmailServerConfigVO emailServerConfigVO) {
		try {
			modelLayerFactory.getEmailConfigurationsManager().updateEmailServerConfiguration(emailServerConfigVO, collection, true);
			view.showMessage($("EmailServerConfigView.configSaved"));
		} catch (EmailServerConfigurationRuntimeException.UnknownServerConfigurationRuntimeException e) {
			e.printStackTrace();
			view.showMessage($("EmailServerConfigView.invalidServerConfig : " + e.getMessage()));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_EMAIL_SERVER).globally();
	}

	public EmailServerConfiguration getEmailServerConfiguration() {
		return modelLayerFactory.getEmailConfigurationsManager().getEmailConfiguration(collection, true);
	}

	public String getTestServerMessage(EmailServerConfigVO emailServerConfigVO, String testEmail) {
		EmailServices emailServices = new EmailServices();
		try {
			Session session = emailServices.openSession(emailServerConfigVO);
			Message message = testMessage(session, testEmail, emailServerConfigVO.getDefaultSenderEmail());
			emailServices.sendEmail(message);
			return ($("EmailServerConfigView.results.success"));
		} catch (EmailServicesException.EmailServerException e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (EmailServicesException.EmailTempException e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (EmailServicesException.EmailPermanentException e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (AddressException e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail.invalidEmail") + "\n" + e.getMessage());
		} catch (MessagingException e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (Exception e) {
			LOGGER.warn(e);
			return ($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		}
	}

	private Message testMessage(Session session, String testEmail, String defaultSenderAddress)
			throws MessagingException {
		Message message = new SMTPMessage(session);
		InternetAddress internetAddress = new InternetAddress(testEmail, true);
		InternetAddress fromAddress = new InternetAddress(defaultSenderAddress, true);
		message.setFrom(fromAddress);
		message.setRecipient(Message.RecipientType.TO, internetAddress);
		message.setText("Test");
		return message;
	}

	public void backButtonClick() {
		view.navigateTo().adminModule();
	}
}
