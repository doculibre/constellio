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
package com.constellio.app.ui.pages.management.email;

import com.constellio.app.ui.entities.EmailServerConfigVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.conf.email.EmailServerConfigurationRuntimeException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.emails.EmailServices;
import com.constellio.model.services.emails.EmailServicesException;
import com.sun.mail.smtp.SMTPMessage;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


import static com.constellio.app.ui.i18n.i18n.$;

public class EmailServerConfigPresenter extends BasePresenter<EmailServerConfigView> {
	public static Logger LOGGER = Logger.getLogger(EmailServerConfigPresenter.class);

	public EmailServerConfigPresenter(EmailServerConfigView view) {
		super(view);
	}

	public void saveButtonClicked(EmailServerConfigVO emailServerConfigVO) {
		try{
			modelLayerFactory.getEmailConfigurationsManager().updateEmailServerConfiguration(emailServerConfigVO, collection);
			view.showMessage($("EmailServerConfigView.configSaved"));
		}catch(EmailServerConfigurationRuntimeException.UnknownServerConfigurationRuntimeException e ){
			view.showMessage($("EmailServerConfigView.invalidServerConfig"));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_EMAIL_SERVER).globally();
	}

	public EmailServerConfiguration getEmailServerConfiguration() {
		return modelLayerFactory.getEmailConfigurationsManager().getEmailConfiguration(collection);
	}

	public String getTestServerMessage(EmailServerConfigVO emailServerConfigVO, String testEmail) {
		EmailServices emailServices = new EmailServices();
		try {
			Session session = emailServices.openSession(emailServerConfigVO);
			Message message = testMessage(session, testEmail, emailServerConfigVO.getDefaultSenderEmail());
			emailServices.sendEmail(message);
			return($("EmailServerConfigView.results.success"));
		} catch (EmailServicesException.EmailServerException e) {
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (EmailServicesException.EmailTempException e) {
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (EmailServicesException.EmailPermanentException e) {
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch(AddressException e){
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail.invalidEmail") + "\n" + e.getMessage());
		} catch(MessagingException e){
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		} catch (Exception e) {
			LOGGER.warn(e);
			return($("EmailServerConfigView.results.fail") + "\n" + e.getMessage());
		}
	}

	private Message testMessage(Session session, String testEmail, String defaultSenderAdress) throws MessagingException {
		Message message = new SMTPMessage(session);
		InternetAddress internetAddress = new InternetAddress(testEmail, true);
		InternetAddress fromAddress = new InternetAddress(defaultSenderAdress, true);
		message.setFrom(fromAddress);
		message.setRecipient(Message.RecipientType.TO, internetAddress);
		message.setText("Test");
		return message;
	}

	public void backButtonClick() {
		view.navigateTo().adminModule();
	}
}
