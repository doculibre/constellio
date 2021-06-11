package com.constellio.app.ui.pages.management.schemas.metadata.reports;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;

public class UniqueMetadataDuplicateExcelReportModel {

	private final Map<String, ExcelSheet> sheets;
	private final Locale locale;
	private final Metadata metadata;

	public UniqueMetadataDuplicateExcelReportModel(UniqueMetadataDuplicateExcelReportParameters parameters) {
		locale = parameters.getLocale();
		metadata = parameters.getMetadata();

		sheets = Stream.of(
				buildDuplicateValueSheet(parameters.getDuplicatedValuesRecordMap())
		)
				.collect(Collectors.toMap(ExcelSheet::getTitle, excelSheet -> excelSheet));

	}

	private ExcelSheet buildDuplicateValueSheet(Map<String, List<Record>> duplicatedValuesRecordMap) {

		return new ExcelSheet($("UniqueMetadataDuplicateExcelReport.duplicatedValues.sheetName")) {
			@Override
			public List<String> getColumnsTitles() {
				Language language = Language.withLocale(locale);
				String metadataLabel = metadata.getLabel(language);
				String schemaTitle = metadata.getSchema().getLabel(language);

				return Arrays.asList(
						$("UniqueMetadataDuplicateExcelReport.duplicatedValues.duplicatedValue.header", metadataLabel),
						$("UniqueMetadataDuplicateExcelReport.duplicatedValues.recordId.header", schemaTitle),
						$("UniqueMetadataDuplicateExcelReport.duplicatedValues.recordTitle.header", schemaTitle));
			}

			@Override
			public List<List<Object>> getContent() {
				return duplicatedValuesRecordMap.entrySet().stream().flatMap(entry -> {
					final String key = entry.getKey();
					return entry.getValue().stream().map(record -> Arrays.asList((Object) key, record.getId(), record.getTitle()));
				}).collect(Collectors.toList());
			}
		};
	}

	public Locale getLocale() {
		return locale;
	}

	public Set<String> getSheetNames() {
		return sheets.keySet();
	}

	public boolean hasHeader(String sheetName) {
		return sheets.containsKey(sheetName) && sheets.get(sheetName).hasHeader();
	}

	public List<String> getColumnsTitles(String sheetName) {
		return Collections.unmodifiableList(sheets.containsKey(sheetName) ? sheets.get(sheetName).getColumnsTitles() : Collections.emptyList());
	}

	public boolean hasContent(String sheetName) {
		return sheets.containsKey(sheetName) && sheets.get(sheetName).hasContent();
	}

	public List<List<Object>> getContent(String sheetName) {
		return Collections.unmodifiableList(sheets.containsKey(sheetName) ? sheets.get(sheetName).getContent() : Collections.emptyList());
	}

	private class ExcelSheet {
		private final String title;

		private ExcelSheet(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public boolean hasHeader() {
			List<String> columnsTitles = getColumnsTitles();
			return columnsTitles != null && !getColumnsTitles().isEmpty();
		}

		public List<String> getColumnsTitles() {
			return Collections.emptyList();
		}

		public boolean hasContent() {
			List<List<Object>> content = getContent();
			return content != null && !content.isEmpty();
		}

		public List<List<Object>> getContent() {
			return Collections.emptyList();
		}
	}
}
