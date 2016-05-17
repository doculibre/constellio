package com.constellio.app.api.extensions;

import java.io.Serializable;
import java.util.Map;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.schemas.Metadata;

public abstract class BatchProcessingExtension implements Serializable {

	public ExtensionBooleanResult isMetadataDisplayedWhenModified(IsMetadataDisplayedWhenModifiedParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isMetadataModifiable(IsMetadataModifiableParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
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
}
