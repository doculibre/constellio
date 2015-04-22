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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.notifications.EmailBuilder;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.notifications.HtmlEventEmailBuilderRuntimeException.HtmlEventEmailBuilderRuntimeException_IOException;

public class HtmlEventEmailBuilder implements EventEmailBuilder {

	private final FoldersLocator foldersLocator;

	public HtmlEventEmailBuilder(FoldersLocator foldersLocator) {
		super();
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void buildContent(EmailBuilder emailBuilder, List<Event> events) {

		File eventNotificationMail = null;

		for (File htmlFile : foldersLocator.getSmtpMailFolder().listFiles()) {
			if (htmlFile.getName().equals("eventNotificationMail.html")) {
				eventNotificationMail = htmlFile;
			}
		}
		String htmlContent;
		try {
			Document doc = Jsoup.parse(eventNotificationMail, "UTF-8");
			htmlContent = doc.toString();
		} catch (IOException e) {
			throw new HtmlEventEmailBuilderRuntimeException_IOException(e);
		}

		StringBuffer content = new StringBuffer();
		content.append("<div>");
		for (Event event : events) {
			content.append(event.getId() + " " + event.getRecordId() + " "
					+ event.getType() + " on " + event.getCreatedOn() + " by " + event.getUsername()
					+ " in " + event.getCollection());
			content.append("\n\n");

		}
		content.append("</div>");

		htmlContent = htmlContent.replace("${content}", content.toString());

		EmailBuilder.setContent(emailBuilder, htmlContent);

	}

	@Override
	public void buildSubject(EmailBuilder emailBuilder, List<Event> events) {
		String subject = "Notification(s) d'event(s): ";
		for (Event event : events) {
			subject += event.getType() + " " + event.getRecordId();
			subject += " ";
		}
		EmailBuilder.setSubject(emailBuilder, subject);
	}
}
