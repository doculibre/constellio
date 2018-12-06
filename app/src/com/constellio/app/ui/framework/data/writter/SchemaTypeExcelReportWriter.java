package com.constellio.app.ui.framework.data.writter;

import com.constellio.app.modules.rm.reports.builders.excel.BaseExcelReportWriter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SchemaTypeExcelReportWriter extends BaseExcelReportWriter {
	MetadataSchemaType metadataSchemaType;
	Locale locale;
	AppLayerFactory appLayerFactory;

	public SchemaTypeExcelReportWriter(MetadataSchemaType metadataSchemaType, AppLayerFactory appLayerFactory,
									   Locale locale) {
		this.metadataSchemaType = metadataSchemaType;
		this.locale = locale;
		this.appLayerFactory = appLayerFactory;
	}

	public void write(OutputStream output) {
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(locale);

		WritableWorkbook workbook;
		try {
			workbook = Workbook.createWorkbook(output, wbSettings);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<MetadataSchema> metadataSchemaList = metadataSchemaType.getAllSchemas();


		List<String> titles = new ArrayList<>();
		titles.add(i18n.$("code"));
		titles.add(i18n.$("title"));
		titles.add(i18n.$("SchemaTypeExcelReportWriter.type"));
		titles.add(i18n.$("SchemaTypeExcelReportWriter.multivalue"));
		titles.add(i18n.$("SchemaTypeExcelReportWriter.readonly"));
		titles.add(i18n.$("SchemaTypeExcelReportWriter.obligatoire"));
		titles.add(i18n.$("SchemaTypeExcelReportWriter.activated"));

		for (int i = 0; i < metadataSchemaList.size(); i++) {

			MetadataSchema currentMetadataSchema = metadataSchemaList.get(i);
			WritableSheet excelSheet = workbook.createSheet(currentMetadataSchema.getLabel(Language.withLocale(locale)), i);


			try {
				addHeader(excelSheet, titles);
			} catch (WriteException e) {
				throw new RuntimeException(e);
			}

			List<List<Object>> lines = new ArrayList<>();


			for (Metadata metadata : currentMetadataSchema.getMetadatas()) {
				List<Object> returnList = new ArrayList<>();
				returnList.add(metadata.getCode());
				returnList.add(metadata.getLabel(Language.withLocale(locale)));
				returnList.add(i18n.$(MetadataValueType.getCaptionFor(metadata.getType())));
				returnList.add(metadata.isMultivalue());
				returnList.add(metadata.getDataEntry().getType() != DataEntryType.MANUAL);
				returnList.add(metadata.isDefaultRequirement());
				returnList.add(metadata.isEnabled());

				lines.add(returnList);
			}

			try {
				createContent(excelSheet, lines);
			} catch (WriteException e) {
				throw new RuntimeException(e);
			}
		}


		try {
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
