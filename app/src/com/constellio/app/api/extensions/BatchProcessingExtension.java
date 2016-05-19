package com.constellio.app.api.extensions;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.ui.Field;

public abstract class BatchProcessingExtension implements Serializable {

	public ExtensionBooleanResult isMetadataDisplayedWhenModified(IsMetadataDisplayedWhenModifiedParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isMetadataModifiable(IsMetadataModifiableParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public void addCustomLabel(AddCustomLabelsParams params) {
	}

	public Field buildMetadataField(MetadataVO metadataVO, RecordVO recordVO) {
		return null;
	}

	public boolean hasMetadataSpecificAssociatedField(MetadataVO metadataVO) {
		return false;
	}

	public static class IsMetadataDisplayedWhenModifiedParams {
		Metadata metadata;

		public IsMetadataDisplayedWhenModifiedParams(Metadata metadata) {
			this.metadata = metadata;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public boolean isSchemaType(String schemaType) {
			return metadata.getSchemaCode().startsWith(schemaType + "_");
		}
	}

	public static class IsMetadataModifiableParams {
		Metadata metadata;
		Map<String, Object> alreadyModifiedMetadatas;

		public IsMetadataModifiableParams(Metadata metadata, Map<String, Object> alreadyModifiedMetadatas) {
			this.metadata = metadata;
			this.alreadyModifiedMetadatas = alreadyModifiedMetadatas;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public boolean isSchemaType(String schemaType) {
			return metadata.getSchemaCode().startsWith(schemaType + "_");
		}
	}

	public static class AddCustomLabelsParams {
		MetadataSchema schema;
		Provider<String, String> resourceProvider;
		Map<String, String> customLabels;
		Locale locale;

		public AddCustomLabelsParams(MetadataSchema schema, Locale locale,
				Provider<String, String> resourceProvider, Map<String, String> customLabels) {
			this.locale = locale;
			this.schema = schema;
			this.resourceProvider = resourceProvider;
			this.customLabels = customLabels;
		}

		public void setCustomLabelUsingResourceKey(String codeOrLocalCode, String key) {
			String value = getResource(key);
			setCustomLabelToValue(codeOrLocalCode, value);
		}

		public void setCustomPrefixLabelWithKey(String codeOrLocalCode, String key) {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(codeOrLocalCode);
			String code = schema.getCode() + "_" + localCode;
			String metadataLabel = schema.getMetadata(code).getLabel(Language.withCode(locale.getLanguage()));
			String value = getResource(key);
			customLabels.put(code, metadataLabel + " " + value);
		}

		public void setCustomLabelToValue(String codeOrLocalCode, String value) {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(codeOrLocalCode);
			String code = schema.getCode() + "_" + localCode;

			customLabels.put(code, value);
		}

		public String getResource(String key) {
			return resourceProvider.get(key);
		}

		public boolean isSchemaType(String schemaType) {
			return schema.getCode().startsWith(schemaType + "_");
		}
	}
}
