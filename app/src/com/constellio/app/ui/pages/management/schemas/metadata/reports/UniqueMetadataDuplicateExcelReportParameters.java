package com.constellio.app.ui.pages.management.schemas.metadata.reports;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UniqueMetadataDuplicateExcelReportParameters {
	private final Metadata metadata;
	private final Map<String, List<Record>> duplicatedValuesRecordMap;
	private final Locale locale;

	public UniqueMetadataDuplicateExcelReportParameters(Metadata metadata,
														Map<String, List<Record>> duplicatedValuesRecordMap,
														Locale locale) {
		this.metadata = metadata;
		this.duplicatedValuesRecordMap = duplicatedValuesRecordMap;
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public Map<String, List<Record>> getDuplicatedValuesRecordMap() {
		return duplicatedValuesRecordMap;
	}

	public Metadata getMetadata() {
		return metadata;
	}
}