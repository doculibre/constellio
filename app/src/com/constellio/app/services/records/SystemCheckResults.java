package com.constellio.app.services.records;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class SystemCheckResults {

	private static final String BROKEN_LINK_ERROR = "brokenLinkParam";

	LocalDateTime dateTime = new LocalDateTime();

	int logicallyDeletedUsers = 0;

	int checkedReferences = 0;
	int brokenReferences = 0;

	int recordsRepaired = 0;


	ValidationErrors errors = new ValidationErrors();

	public void addBrokenLink(String recordId, String brokenLinkRecordId, Metadata referenceMetadata) {
		brokenReferences++;
		Map<String, Object> params = new HashMap<>();

		params.put("metadataCode", referenceMetadata.getCode());
		params.put("record", recordId);
		params.put("brokenLinkRecordId", brokenLinkRecordId);

		errors.add(SystemCheckResults.class, BROKEN_LINK_ERROR, params);
	}
}
