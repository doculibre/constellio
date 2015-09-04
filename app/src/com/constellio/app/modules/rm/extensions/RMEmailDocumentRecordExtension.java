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
package com.constellio.app.modules.rm.extensions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationEvent;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

public class RMEmailDocumentRecordExtension extends RecordExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(RMEmailDocumentRecordExtension.class);

	private static String OUTLOOK_MSG_MIMETYPE = "application/vnd.ms-outlook";

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	public RMEmailDocumentRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public void recordInCreation(RecordInCreationEvent event) {
		if (event.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(event.getRecord());
			Content content = document.getContent();
			if (content != null && OUTLOOK_MSG_MIMETYPE.equals(content.getCurrentVersion().getMimetype())) {
				event.getRecord().changeSchemaTo("email");
				Email email = rm.wrapEmail(event.getRecord());
				populateFields(email, content);

			}
		} else if (event.isSchemaType(UserDocument.SCHEMA_TYPE)) {
			UserDocument userDocument = rm.wrapUserDocument(event.getRecord());
			Content content = userDocument.getContent();
			if (content != null && OUTLOOK_MSG_MIMETYPE.equals(content.getCurrentVersion().getMimetype())) {
				populateFields(userDocument, content);

			}
		}
	}

	private void populateFields(UserDocument userDocument, Content content) {
		String hash = content.getCurrentVersion().getHash();

		try {
			ParsedContent parsedContent = modelLayerFactory.getContentManager().getParsedContent(hash);
			String subject = asString(parsedContent.getNormalizedProperty("Subject"));
			if (subject != null) {
				String filename = subject.replaceAll("[^a-zA-Z0-9.-]", "_");
				content.renameCurrentVersion(filename + ".msg");
				userDocument.setTitle(subject);
			}
		} catch (ContentManagerRuntimeException e) {
			LOGGER.error("Cannot populate fields of user document '" + userDocument.getId() + "'", e);
		}
	}

	private void populateFields(Email email, Content content) {
		String hash = content.getCurrentVersion().getHash();

		try {
			ParsedContent parsedContent = modelLayerFactory.getContentManager().getParsedContent(hash);
			String subject = asString(parsedContent.getNormalizedProperty("Subject"));
			if (subject != null) {
				email.setTitle(subject);
				email.setEmailObject(subject);
			}
			email.setEmailFrom(asString(parsedContent.getNormalizedProperty("From")));
			email.setEmailBCCTo(asStringList(parsedContent.getNormalizedProperty("BCC")));
			email.setEmailCCTo(asStringList(parsedContent.getNormalizedProperty("CC")));
			email.setEmailTo(asStringList(parsedContent.getNormalizedProperty("To")));

		} catch (ContentManagerRuntimeException e) {
			LOGGER.error("Cannot populate fields of email '" + email.getId() + "'", e);
		}

	}

	private List<String> asStringList(Object value) {
		if (value == null) {
			return new ArrayList<>();
		} else {
			List<String> values = new ArrayList<>();

			for (String aValue : value.toString().split(";")) {
				values.add(aValue.replace("'", "").trim());
			}

			return values;
		}
	}

	private String asString(Object value) {
		return value == null ? null : value.toString().trim();
	}
}
