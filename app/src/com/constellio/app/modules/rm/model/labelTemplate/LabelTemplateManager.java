package com.constellio.app.modules.rm.model.labelTemplate;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManagerRuntimeException.LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.utils.comparators.AbstractTextComparator;
import org.jdom2.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelTemplateManager {

	private static final String LABELS_TEMPLATES_FOLDER = "labelTemplates";
	private final ConfigManager configManager;
	private final AppLayerFactory appLayerFactory;
	Map<String, LabelTemplate> labelTemplateMap;

	public LabelTemplateManager(ConfigManager configManager, AppLayerFactory appLayerFactory) {
		this.configManager = configManager;
		this.appLayerFactory = appLayerFactory;
		labelTemplateMap = new HashMap<>();
	}

	public void addUpdateLabelTemplate(Document document) {
		LabelTemplate template = new LabelTemplateReader(document).createLabelTemplate();

		String path = LABELS_TEMPLATES_FOLDER + "/" + template.getKey() + ".xml";

		if (configManager.exist(path)) {
			XMLConfiguration configuration = configManager.getXML(path);
			try {
				configManager.update(path, configuration.getHash(), document);
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new RuntimeException(optimisticLockingConfiguration);
			}
		} else {
			configManager.add(path, document);
		}
		labelTemplateMap.put(template.getKey(), template);
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

	public List<LabelTemplate> listExtensionTemplates(String schemaType) {
		List<LabelTemplate> labelTemplates = new ArrayList<>();
		addToCache(labelTemplates);
		Collections.sort(labelTemplates, new AbstractTextComparator<LabelTemplate>() {
			@Override
			protected String getText(LabelTemplate object) {
				return object.getName();
			}
		});
		return labelTemplates;
	}

	public List<LabelTemplate> listTemplates(String schemaType) {
		List<LabelTemplate> labelTemplates = new ArrayList<>();
		List<String> templateCodes = new ArrayList<>(configManager.list(LABELS_TEMPLATES_FOLDER));

		for (String templateCode : templateCodes) {
			if (templateCode.toLowerCase().endsWith("xml")) {
				LabelTemplate labelTemplate = getLabelTemplate(templateCode);
				if (labelTemplate.getSchemaType().equals(schemaType)) {
					labelTemplates.add(labelTemplate);
				}
			}
		}

		addDefaultLabelTemplates(schemaType, labelTemplates);

		addToCache(labelTemplates);
		Collections.sort(labelTemplates, new AbstractTextComparator<LabelTemplate>() {
			@Override
			protected String getText(LabelTemplate object) {
				return object.getName();
			}
		});
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
		}
	}

	private void addDefaultContainerLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate containerAvery5159Template = DefaultLabelsTemplate.createContainerAvery5159();
		labelTemplates.add(containerAvery5159Template);
	}

	private void addDefaultFolderLabelTemplates(List<LabelTemplate> labelTemplates) {
		LabelTemplate folderAvery5159LeftTemplate = DefaultLabelsTemplate.createFolderLeftAvery5159();
		LabelTemplate folderAvery5159RightTemplate = DefaultLabelsTemplate.createFolderRightAvery5159();
		LabelTemplate folderAvery5161LeftTemplate = DefaultLabelsTemplate.createFolderLeftAvery5161();
		LabelTemplate folderAvery5161RightTemplate = DefaultLabelsTemplate.createFolderRightAvery5161();
		LabelTemplate folderAvery5162LeftTemplate = DefaultLabelsTemplate.createFolderLeftAvery5162();
		LabelTemplate folderAvery5162RightTemplate = DefaultLabelsTemplate.createFolderRightAvery5162();
		LabelTemplate folderAvery5163LeftTemplate = DefaultLabelsTemplate.createFolderLeftAvery5163();
		LabelTemplate folderAvery5163RightTemplate = DefaultLabelsTemplate.createFolderRightAvery5163();
		labelTemplates.add(folderAvery5159LeftTemplate);
		labelTemplates.add(folderAvery5159RightTemplate);
		labelTemplates.add(folderAvery5161LeftTemplate);
		labelTemplates.add(folderAvery5161RightTemplate);
		labelTemplates.add(folderAvery5162LeftTemplate);
		labelTemplates.add(folderAvery5162RightTemplate);
		labelTemplates.add(folderAvery5163LeftTemplate);
		labelTemplates.add(folderAvery5163RightTemplate);

	}
}
