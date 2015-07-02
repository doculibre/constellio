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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManagerRuntimeException.LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.ConfigManager;

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
			try {
				LabelTemplateReader reader = new LabelTemplateReader(configManager.getXML(path).getDocument());
				return reader.createLabelTemplate();
			} catch (Exception e) {
				throw new LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate(e);
			}
		} else {
			return null;
		}
	}

	public List<LabelTemplate> listTemplates(String schemaType) {
		List<LabelTemplate> labelTemplates = new ArrayList<>();
		List<String> templateCodes = configManager.list(LABELS_TEMPLATES_FOLDER);
		for (String templateCode : templateCodes) {
			if (templateCode.toLowerCase().endsWith("xml")) {
				LabelTemplate labelTemplate = getLabelTemplate(templateCode);
				if (labelTemplate.getSchemaType().equals(schemaType)) {
					labelTemplates.add(labelTemplate);
				}
			}
		}
		if (labelTemplates.isEmpty()) {
			addDefaultLabelTemplates(schemaType, labelTemplates);
		}
		addToCache(labelTemplates);
		return labelTemplates;
	}

	private void addToCache(List<LabelTemplate> labelTemplates) {
		for (LabelTemplate labelTemplate : labelTemplates) {
			labelTemplateMap.put(labelTemplate.getKey(), labelTemplate);
		}
	}

	private void addDefaultLabelTemplates(String schemaType, List<LabelTemplate> labelTemplates) {
		if (Folder.SCHEMA_TYPE.equals(schemaType)) {
			addDefaultFolderLabelTemplates(labelTemplates);
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
			addDefaultContainerLabelTemplates(labelTemplates);
		} else {
			labelTemplates.add(new LabelTemplate());
		}
	}

	private void addDefaultContainerLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate containerAvery5159Template = DefaultLabelsTemplate.createContainerAvery5159();
		labelTemplates.add(containerAvery5159Template);
	}

	private void addDefaultFolderLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate folderAvery5159LeftTemplate = DefaultLabelsTemplate.createFolderLeftAvery5159();
		LabelTemplate folderAvery5159RightTemplate = DefaultLabelsTemplate.createFolderRightAvery5159();
		labelTemplates.add(folderAvery5159LeftTemplate);
		labelTemplates.add(folderAvery5159RightTemplate);

	}
}
