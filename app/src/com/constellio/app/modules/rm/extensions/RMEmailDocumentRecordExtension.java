package com.constellio.app.modules.rm.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
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
	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(UserDocument.SCHEMA_TYPE)) {
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
			ParsedContent parsedContent = modelLayerFactory.getContentManager().getParsedContentParsingIfNotYetDone(hash);
			String subject = asString(parsedContent.getNormalizedProperty("Subject"));
			if (subject != null) {
//				FIXME Move this in the Outlook plugin's code				
//				String filename = subject.replaceAll("[^a-zA-Z0-9.-]", "_");
//				content.renameCurrentVersion(filename + ".msg");
//				userDocument.setTitle(subject);
			}
		} catch (ContentManagerRuntimeException e) {
			LOGGER.error("Cannot populate fields of user document '" + userDocument.getId() + "'", e);
		}
	}

	private String asString(Object value) {
		return value == null ? null : value.toString().trim();
	}
}
