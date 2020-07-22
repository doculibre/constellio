package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.EmailMessageParams;
import com.constellio.app.api.extensions.params.ParseEmailMessageParams;
import com.constellio.model.services.emails.EmailServices.EmailMessage;

import javax.mail.internet.MimeMessage;
import java.io.IOException;

public class EmailExtension {

	public EmailMessage newEmailMessage(EmailMessageParams params) {
		return null;
	}

	public MimeMessage parseEmailMessage(ParseEmailMessageParams params) throws IOException {
		return null;
	}

}
