package com.constellio.app.modules.rm.services.sip.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public abstract class SIPMetadataObject {

	public static final String FOLDER_TYPE = "FOLDER";
	public static final String DOCUMENT_TYPE = "DOCUMENT";
	public static final String CATEGORY_TYPE = "CATEGORY";

	private Record record;

	private List<String> metadataIds = new ArrayList<String>();

	private Map<String, String> metadataLabelsMap = new LinkedHashMap<>();

	private Map<String, List<String>> metadataValuesMap = new LinkedHashMap<>();

	private List<Metadata> schemaMetadata;

	public SIPMetadataObject(Record rmObject, List<Metadata> metadatasOfSchema) {
		this.record = rmObject;
		this.schemaMetadata = metadatasOfSchema;
		if (rmObject == null) {
			throw new NullPointerException("metadataSchema is null");
		}

		for (Metadata metadata : metadatasOfSchema) {
			String metadataId = metadata.getCode();
			String metadataLabel = metadata.getLabel(Language.French);
			Object metadataValue = rmObject.get(metadata);
			if (metadataValue != null) {
				String displayValue = rmObject.get(metadata).toString();
				List<String> metadataValues = StringUtils.isNotBlank(displayValue) ?
						Arrays.asList(displayValue) :
						new ArrayList<String>();
				metadataIds.add(metadataId);
				metadataLabelsMap.put(metadataId, metadataLabel);
				metadataValuesMap.put(metadataId, metadataValues);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Record> T getFicheMetadonnees() {
		return (T) record;
	}

	public String getId() {
		return "" + record.getId();
	}

	public List<String> getMetadataIds() {
		return metadataIds;
	}

	public String getMetadataLabel(String metadataId) {
		return metadataLabelsMap.get(metadataId);
	}

	public String getMetadataValue(String metadataId) {
		List<String> metadataValues = getMetadataValues(metadataId);
		return !metadataValues.isEmpty() ? metadataValues.get(0) : null;
	}

	public List<String> getMetadataValues(String metadataId) {
		return metadataValuesMap.get(metadataId);
	}

	protected Record getRecord() {
		return this.record;
	}

	protected List<Metadata> getSchemaMetadata() {
		return schemaMetadata;
	}

}
