package com.constellio.app.modules.rm.model.labelTemplate;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldHorizontalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldVerticalAlignment;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Schemas;
import com.itextpdf.text.Font.FontFamily;

public class DefaultLabelsTemplate {

	public static LabelTemplate createFolderLeftAvery5159() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 0, 0, 14, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderIdField = new BarCodeLabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 15, 0, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 3, 29, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new
				LabelTemplate("FOLDER_LEFT_AVERY_5159", $("LabelsButton.labelFormat.FOLDER_LEFT_AVERY_5159"),
				LabelsReportLayout.AVERY_5159,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderRightAvery5159() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField folderIdField = new BarCodeLabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 0, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 15, 0, 14, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 3, 29, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_RIGHT_AVERY_5159", $("LabelsButton.labelFormat.FOLDER_RIGHT_AVERY_5159"),
				LabelsReportLayout.AVERY_5159,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderLeftAvery5161() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 0, 0, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderIdField = new BarCodeLabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 15, 0, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 4, 29, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new
				LabelTemplate("FOLDER_LEFT_AVERY_5161", $("LabelsButton.labelFormat.FOLDER_LEFT_AVERY_5161"),
				LabelsReportLayout.AVERY_5161,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderRightAvery5161() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField folderIdField = new BarCodeLabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 0, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 15, 0, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 4, 29, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_RIGHT_AVERY_5161", $("LabelsButton.labelFormat.FOLDER_RIGHT_AVERY_5161"),
				LabelsReportLayout.AVERY_5161,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderLeftAvery5162() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 10;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 1, 1, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 15, 1, 9, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		//FIXME add plugin for larger title size
		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 1, 4, 28, 3, FontFamily.HELVETICA.name(), 12.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 7, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 20, 7, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new
				LabelTemplate("FOLDER_LEFT_AVERY_5162", $("LabelsButton.labelFormat.FOLDER_LEFT_AVERY_5162"),
				LabelsReportLayout.AVERY_5162,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderRightAvery5162() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 10;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 1, 1, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 6, 1, 14, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 1, 5, 28, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 7, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 20, 7, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_RIGHT_AVERY_5162", $("LabelsButton.labelFormat.FOLDER_RIGHT_AVERY_5162"),
				LabelsReportLayout.AVERY_5162,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderLeftAvery5163() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 13;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 1, 0, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 21, 0, 8, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 1, 4, 29, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField administrativeUnitCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_CODE,
				null, 1, 10, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 16, 10, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 10, 8, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(administrativeUnitCodeField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new
				LabelTemplate("FOLDER_LEFT_AVERY_5163", $("LabelsButton.labelFormat.FOLDER_LEFT_AVERY_5163"),
				LabelsReportLayout.AVERY_5163,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createFolderRightAvery5163() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 17;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 1, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 15, 1, 14, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 60,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 8, 29, 3, FontFamily.HELVETICA.name(), 8.0f, true, true, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField administrativeUnitCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_CODE,
				null, 0, 15, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 15, 1, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 1,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 20, 15, 9, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(administrativeUnitCodeField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_RIGHT_AVERY_5163", $("LabelsButton.labelFormat.FOLDER_RIGHT_AVERY_5163"),
				LabelsReportLayout.AVERY_5163,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	public static LabelTemplate createContainerAvery5159() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField containerIdField = new BarCodeLabelTemplateField(
				ContainerRecord.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 0, 5, 4, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		LabelTemplateField containerTitleField = new LabelTemplateField(
				ContainerRecord.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 15, 0, 14, 2, FontFamily.HELVETICA.name(), 8.0f, true, true, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER, null, null, null);

		labelTemplateFields.add(containerIdField);
		labelTemplateFields.add(containerTitleField);

		return new LabelTemplate("CONTAINER_AVERY_5159", $("LabelsButton.labelFormat.CONTAINER_AVERY_5159"),
				LabelsReportLayout.AVERY_5159,
				Folder.SCHEMA_TYPE, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

}
