package com.constellio.app.modules.es.ui.pages.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.TargetParams;
import com.constellio.app.modules.es.ui.entities.MappingVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public class AddEditMappingPresenter extends MappingsPresenter<AddEditMappingView> {
	private String documentType;
	private String metadataCode;
	private boolean editMode;
	private MetadataSchemaVO schema;

	public AddEditMappingPresenter(AddEditMappingView view) {
		super(view);
	}

	public AddEditMappingPresenter forParams(String parameters) {
		String[] parts = parameters.split("/");
		instanceId = parts[0];
		documentType = parts[1];
		if (parts.length == 3) {
			metadataCode = parts[2];
			editMode = true;
		} else {
			editMode = false;
		}

		schema = new MetadataSchemaToVOBuilder()
				.build(schema(documentType + "_" + instanceId), VIEW_MODE.DISPLAY, view.getSessionContext());

		return this;
	}

	public MappingVO getMapping() {
		return editMode ? buildMappingVO() : new MappingVO();
	}

	public List<MetadataVO> getAvailableTargetMetadata() {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		Set<String> used = getUsedTargets();
		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : mappingService().getTargetMetadata(connectorInstance(), documentType)) {
			if (!used.contains(metadata.getLocalCode())) {
				result.add(builder.build(metadata, schema, view.getSessionContext()));
			}
		}
		return result;
	}

	public Collection<MetadataValueType> getApplicableTypes() {
		Set<MetadataValueType> result = new HashSet<>();
		for (ConnectorField field : mappingService().getConnectorFields(connectorInstance(), documentType)) {
			result.add(field.getType());
		}
		return result;
	}

	public List<ConnectorField> getApplicableSourceFields(MappingVO mapping) {
		List<ConnectorField> result = new ArrayList<>();
		for (ConnectorField field : mappingService().getConnectorFields(connectorInstance(), documentType)) {
			if (field.getType().equals(mapping.getMetadataType())) {
				result.add(field);
			}
		}
		return result;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public boolean canEditMetadata() {
		return !editMode;
	}

	public MetadataVO metadataCreationRequested(TargetParams target) {
		Metadata metadata = mappingService().createTargetMetadata(connectorInstance(), documentType, target);
		return new MetadataToVOBuilder().build(metadata, schema, view.getSessionContext());
	}

	public void metadataSelected(MappingVO mapping, MetadataVO metadata) {
		MetadataVO previous = mapping.getMetadata();
		mapping.setMetadata(metadata);
		metadataCode = metadata.getLocalCode();
		if (previous == null || !previous.getType().equals(metadata.getType())) {
			mapping.setFields(new ArrayList<ConnectorField>());
			view.resetSources();
		}
	}

	public boolean canAddFieldsTo(MappingVO mapping) {
		return mapping.getMetadata() != null;
	}

	public void fieldAdditionRequested(MappingVO mapping) {
		mapping.getFields().add(null);
		view.resetSources();
	}

	public void fieldRemovalRequested(MappingVO mapping, ConnectorField field) {
		mapping.getFields().remove(field);
		view.resetSources();
	}

	public boolean canSave(MappingVO mapping) {
		if (mapping.getMetadata() == null || mapping.getFields().isEmpty()) {
			return false;
		}
		for (ConnectorField field : mapping.getFields()) {
			if (field == null) {
				return false;
			}
		}
		return true;
	}

	public void saveButtonClicked(MappingVO mappingVO) {
		Map<String, List<String>> mapping = mappingService().getMapping(connectorInstance(), documentType);
		mapping.put(mappingVO.getMetadataLocalCode(), mappingVO.getFieldIds());
		ConnectorInstance instance = mappingService().setMapping(connectorInstance(), documentType, mapping);
		addOrUpdate(instance.getWrappedRecord());
		view.navigate().to(ESViews.class).displayConnectorMappings(instanceId);
	}

	public void cancelButtonClicked() {
		view.navigate().to(ESViews.class).displayConnectorMappings(instanceId);
	}

	private MappingVO buildMappingVO() {
		Map<String, ConnectorField> fields = getFieldMapById(documentType);
		List<ConnectorField> mappedFields = new ArrayList<>();
		for (String field : mappingService().getMapping(connectorInstance(), documentType).get(metadataCode)) {
			mappedFields.add(fields.get(field));
		}
		return new MappingVO(getMetadataVO(documentType + "_" + instanceId + "_" + metadataCode), mappedFields);
	}

	private Set<String> getUsedTargets() {
		Set<String> result = mappingService().getMapping(connectorInstance(), documentType).keySet();
		result.remove(metadataCode);
		return result;
	}
}
