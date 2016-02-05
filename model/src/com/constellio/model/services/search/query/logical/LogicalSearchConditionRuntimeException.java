package com.constellio.model.services.search.query.logical;

import com.constellio.model.entities.schemas.DataStoreField;

@SuppressWarnings("serial")
public class LogicalSearchConditionRuntimeException extends RuntimeException {

	public LogicalSearchConditionRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogicalSearchConditionRuntimeException(String message) {
		super(message);
	}

	public LogicalSearchConditionRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class UnsupportedConditionForMetadata extends LogicalSearchConditionRuntimeException {

		public UnsupportedConditionForMetadata(DataStoreField dataStoreField) {
			super("Unsupported condition on field '" + dataStoreField.getDataStoreCode() + "' of type '" + dataStoreField
					.getType().name() + "'");
		}

	}

	public static class MetadatasRequired extends LogicalSearchConditionRuntimeException {

		public MetadatasRequired() {
			super("Must define a non-empty list of metadatas");
		}
	}

	public static class SchemaOrSchemaTypeOrCollectionRequired extends LogicalSearchConditionRuntimeException {

		public SchemaOrSchemaTypeOrCollectionRequired() {
			super("Must define a schema or a schema type");
		}
	}
}
