package com.constellio.app.modules.es.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.MetadataValueType;

public class MappingVO implements Serializable {
	public static final String METADATA_LABEL = "metadataLabel";
	public static final String FIELD_LABELS = "fieldLabels";

	private MetadataVO metadata;
	private List<ConnectorField> fields;

	public MappingVO() {
		fields = new ArrayList<>();
	}

	public MappingVO(MetadataVO metadata, List<ConnectorField> fields) {
		this.metadata = metadata;
		this.fields = fields;
	}

	public MetadataVO getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataVO metadata) {
		this.metadata = metadata;
	}

	public String getMetadataLabel() {
		return metadata != null ? metadata.getLabel() : null;
	}

	public String getMetadataLocalCode() {
		return metadata != null ? metadata.getLocalCode() : null;
	}

	public MetadataValueType getMetadataType() {
		return metadata != null ? metadata.getType() : null;
	}

	public List<ConnectorField> getFields() {
		return fields;
	}

	public void setFields(List<ConnectorField> fields) {
		this.fields = fields;
	}

	public String getFieldLabels() {
		List<String> labels = new ArrayList<>();
		for (ConnectorField field : fields) {
			labels.add(field.getLabel());
		}
		return StringUtils.join(labels, ", ");
	}

	public List<String> getFieldIds() {
		List<String> result = new ArrayList<>();
		for (ConnectorField field : fields) {
			result.add(field.getId());
		}
		return result;
	}

	public List<FieldMapper> getFieldMappers() {
		List<FieldMapper> result = new ArrayList<>();
		for (int i = 0; i < fields.size(); i++) {
			result.add(new FieldMapper(i));
		}
		return result;
	}

	public class FieldMapper implements Serializable {
		private final int index;

		public FieldMapper(int index) {
			this.index = index;
		}

		public ConnectorField getField() {
			return fields.get(index);
		}

		public void setField(ConnectorField field) {
			fields.set(index, field);
		}

		public String getFieldId() {
			ConnectorField field = getField();
			return field != null ? field.getId() : null;
		}
	}
}
