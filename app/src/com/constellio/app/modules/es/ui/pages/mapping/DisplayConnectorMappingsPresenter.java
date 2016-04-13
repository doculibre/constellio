package com.constellio.app.modules.es.ui.pages.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.MappingParams;
import com.constellio.app.modules.es.ui.entities.DocumentType;
import com.constellio.app.modules.es.ui.entities.MappingVO;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.Language;

public class DisplayConnectorMappingsPresenter extends MappingsPresenter<DisplayConnectorMappingsView> {

	public DisplayConnectorMappingsPresenter(DisplayConnectorMappingsView view) {
		super(view);
	}

	public DisplayConnectorMappingsPresenter forParams(String parameters) {
		instanceId = parameters;
		return this;
	}

	public List<DocumentType> getDocumentTypes() {
		List<DocumentType> result = new ArrayList<>();
		for (String code : mappingService().getDocumentTypes(connectorInstance())) {

			Language language = Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage());
			String label = schema(code + "_default").getLabel(language);
			result.add(new DocumentType(code, label));
		}
		return result;
	}

	public List<MappingVO> getMappings(String documentType) {
		Map<String, ConnectorField> fields = getFieldMapById(documentType);
		ArrayList<MappingVO> result = new ArrayList<>();
		for (Entry<String, List<String>> mapping : mappingService().getMapping(connectorInstance(), documentType).entrySet()) {
			MetadataVO metadata = getMetadataVO(documentType + "_" + instanceId + "_" + mapping.getKey());
			List<ConnectorField> mappedFields = new ArrayList<>();
			for (String field : mapping.getValue()) {
				mappedFields.add(fields.get(field));
			}
			result.add(new MappingVO(metadata, mappedFields));
		}
		return result;
	}

	public void backButtonClicked() {
		view.navigateTo().displayConnectorInstance(instanceId);
	}

	public void addMappingRequested(String documentType) {
		view.navigateTo().addConnectorMapping(instanceId, documentType);
	}

	public void editMappingRequested(String documentType, MappingVO mapping) {
		view.navigateTo().editConnectorMapping(instanceId, documentType, mapping.getMetadata().getLocalCode());
	}

	public void deleteMappingRequested(String documentType, MappingVO mappingVO) {
		Map<String, List<String>> mapping = mappingService().getMapping(connectorInstance(), documentType);
		mapping.remove(mappingVO.getMetadataLocalCode());
		saveMapping(documentType, mapping);
	}

	public boolean canQuickConfig(String documentType) {
		return mappingService().canQuickConfig(connectorInstance(), documentType);
	}

	public void quickConfigRequested(String documentType) {
		view.displayQuickConfig(documentType);
	}

	public List<MappingParams> getDefaultQuickConfig(String documentType) {
		return mappingService().getDefaultMappingParams(connectorInstance(), documentType);
	}

	public void quickConfigRequested(String documentType, List<MappingParams> mappingConfig) {
		mappingService().setMapping(connectorInstance(), documentType, mappingConfig);
		view.reload();
	}

	private void saveMapping(String documentType, Map<String, List<String>> mapping) {
		mappingService().setMapping(connectorInstance(), documentType, mapping);
		view.reload();
	}
}
