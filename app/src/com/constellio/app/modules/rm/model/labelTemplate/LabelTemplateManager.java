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
package com.constellio.app.modules.rm.model.labelTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate.SchemaType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldHorizontalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldVerticalAlignment;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.model.entities.schemas.Schemas;
import com.itextpdf.text.Font.FontFamily;

//TODO Thiago optimize
public class LabelTemplateManager {

	private static final String LABELS_TEMPLATES_FOLDER = "labelTemplates";
	private final ConfigManager configManager;
	Map<String, LabelTemplate> labelTemplateMap;

	public LabelTemplateManager(ConfigManager configManager) {
		this.configManager = configManager;
		labelTemplateMap = new HashMap<>();
	}

	public LabelTemplate getLabelTemplate(String code) {

		if (labelTemplateMap != null && labelTemplateMap.containsKey(code)) {
			return labelTemplateMap.get(code);
		}
		String path = LABELS_TEMPLATES_FOLDER + "/" + code;
		if (configManager.exist(path)) {
			//TODO Thiago cache
			LabelTemplateReader reader = new LabelTemplateReader(configManager.getXML(path).getDocument());
			try {
				return reader.createLabelTemplate();
			} catch (Exception e) {
				//TODO Thiago
				throw new RuntimeException(e);
			}
		} else {
			//TODO Thiago
			throw new RuntimeException("TODO");
		}
	}

	private LabelTemplate addToConfigManager(String code) {
		String path = LABELS_TEMPLATES_FOLDER + "/" + code;
		SAXBuilder saxBuilder = new SAXBuilder();
		File file = new File(path);
		Document document;
		try {
			document = saxBuilder.build(file);
		} catch (JDOMException | IOException e) {
			//TODO Thiago Exception
			throw new RuntimeException(e);
		}
		LabelTemplateReader reader = new LabelTemplateReader(document);
		if (configManager.exist(path)) {
			XMLConfiguration xmlConfiguration = configManager.getXML(path);
			try {
				configManager.update(path, xmlConfiguration.getHash(), xmlConfiguration.getDocument());
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}
		} else {
			configManager.add(path, document);
		}
		try {
			return reader.createLabelTemplate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<LabelTemplate> listTemplates(String schemaType) {
		List<LabelTemplate> labelTemplates = new ArrayList<>();
		List<String> templateCodes = configManager.list(LABELS_TEMPLATES_FOLDER);
		for (String templateCode : templateCodes) {
			if (templateCode.toLowerCase().endsWith("xml")) {
				LabelTemplate labelTemplate = addToConfigManager(templateCode);
				if (schemaType.equals(labelTemplate.getSchemaType().name())) {
					labelTemplates.add(labelTemplate);
				}
			}
		}
		if (labelTemplates.isEmpty()) {
			if (SchemaType.FOLDER.name().equals(schemaType) || Folder.SCHEMA_TYPE.equals(schemaType)) {
				addDefaultFolderLabelTemplates(labelTemplates);
			} else if (SchemaType.CONTAINER.name().equals(schemaType) || ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
				addDefaultContainerLabelTemplates(labelTemplates);
			} else {
				labelTemplates.add(new LabelTemplate());
			}
		}
		for (LabelTemplate labelTemplate : labelTemplates) {
			labelTemplateMap.put(labelTemplate.getKey(), labelTemplate);
		}
		return labelTemplates;
	}

	private void addDefaultContainerLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate containerAvery5159Template = createContainerAvery5159LabelTemplate();
		labelTemplates.add(containerAvery5159Template);
	}

	private void addDefaultFolderLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate folderAvery5159LeftTemplate = createFolderLeftAvery5159LabelTemplate();
		LabelTemplate folderAvery5159RightTemplate = createFolderRightAvery5159LabelTemplate();
		labelTemplates.add(folderAvery5159LeftTemplate);
		labelTemplates.add(folderAvery5159RightTemplate);

	}

	private LabelTemplate createFolderLeftAvery5159LabelTemplate() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 0, 0, 14, 2, FontFamily.HELVETICA.name(), 0.8f, 60,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 15, 0, 15, 2, FontFamily.HELVETICA.name(), 0.8f, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 3, 29, 2, FontFamily.HELVETICA.name(), 0.8f, 130,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField filingSpaceCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.FILING_SPACE_CODE,
				null, 0, 9, 9, 2, FontFamily.HELVETICA.name(), 0.8f, 120,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 0.8f, 1,
				LabelTemplateFieldHorizontalAlignment.LEFT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 0.8f, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(filingSpaceCodeField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_LEFT_AVERY_5159", "FOLDER_LEFT_AVERY_5159",
				LabelsReportLayout.AVERY_5159,
				SchemaType.FOLDER, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	private LabelTemplate createFolderRightAvery5159LabelTemplate() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField folderIdField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 0, 15, 2, FontFamily.HELVETICA.name(), 0.8f, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField categoryCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.CATEGORY_CODE,
				null, 15, 0, 14, 2, FontFamily.HELVETICA.name(), 0.8f, 60,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField folderTitleField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 0, 3, 29, 2, FontFamily.HELVETICA.name(), 0.8f, 130,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField filingSpaceCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.FILING_SPACE_CODE,
				null, 0, 9, 9, 2, FontFamily.HELVETICA.name(), 0.8f, 120,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField copyStatusCodeField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS,
				null, 14, 9, 1, 2, FontFamily.HELVETICA.name(), 0.8f, 1,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField openDateField = new LabelTemplateField(
				Folder.DEFAULT_SCHEMA + "_" + Folder.OPENING_DATE,
				null, 21, 9, 9, 2, FontFamily.HELVETICA.name(), 0.8f, 37,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		labelTemplateFields.add(folderIdField);
		labelTemplateFields.add(categoryCodeField);
		labelTemplateFields.add(folderTitleField);
		labelTemplateFields.add(filingSpaceCodeField);
		labelTemplateFields.add(copyStatusCodeField);
		labelTemplateFields.add(openDateField);

		return new LabelTemplate("FOLDER_RIGHT_AVERY_5159", "FOLDER_RIGHT_AVERY_5159",
				LabelsReportLayout.AVERY_5159,
				SchemaType.FOLDER, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}

	private LabelTemplate createContainerAvery5159LabelTemplate() {
		final int COLUMNS_NUMBER = 30;
		final int LINES_NUMBER = 11;

		List<LabelTemplateField> labelTemplateFields = new ArrayList<>();

		LabelTemplateField containerIdField = new LabelTemplateField(
				ContainerRecord.DEFAULT_SCHEMA + "_" + Schemas.IDENTIFIER,
				null, 0, 0, 15, 2, FontFamily.HELVETICA.name(), 0.8f, 62,
				LabelTemplateFieldHorizontalAlignment.CENTER,
				LabelTemplateFieldVerticalAlignment.CENTER);

		LabelTemplateField containerTitleField = new LabelTemplateField(
				ContainerRecord.DEFAULT_SCHEMA + "_" + Schemas.TITLE_CODE,
				null, 15, 0, 14, 2, FontFamily.HELVETICA.name(), 0.8f, 62,
				LabelTemplateFieldHorizontalAlignment.RIGHT,
				LabelTemplateFieldVerticalAlignment.CENTER);

		labelTemplateFields.add(containerIdField);
		labelTemplateFields.add(containerTitleField);

		return new LabelTemplate("CONTAINER_AVERY_5159", "CONTAINER_AVERY_5159",
				LabelsReportLayout.AVERY_5159,
				SchemaType.FOLDER, COLUMNS_NUMBER, LINES_NUMBER, labelTemplateFields);
	}
}
