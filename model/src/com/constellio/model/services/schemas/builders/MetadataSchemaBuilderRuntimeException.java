package com.constellio.model.services.schemas.builders;

import com.constellio.model.utils.DependencyUtilsRuntimeException;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class MetadataSchemaBuilderRuntimeException extends RuntimeException {

	public MetadataSchemaBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemaBuilderRuntimeException(String message) {
		super(message);
	}

	public MetadataSchemaBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class NoSuchMetadata extends MetadataSchemaBuilderRuntimeException {

		String metadataCode;

		public NoSuchMetadata(String codeMetadata) {
			super("No such metadata : '" + codeMetadata + "'");
			this.metadataCode = codeMetadata;
		}

		public String getMetadataCode() {
			return metadataCode;
		}
	}

	public static class MetadataAlreadyExists extends MetadataSchemaBuilderRuntimeException {
		public MetadataAlreadyExists(String codeMetadata) {
			super("Metadata with code '" + codeMetadata + "' already exists");
		}
	}

	public static class CannotDeleteSchema extends MetadataSchemaBuilderRuntimeException {
		public CannotDeleteSchema(String code) {
			super("SchemaType: " + code + " is undeletable");
		}
	}

	public static class CyclicDependenciesInMetadata extends MetadataSchemaBuilderRuntimeException {

		List<String> metadataCodesWithCyclicDependency;

		public CyclicDependenciesInMetadata(DependencyUtilsRuntimeException.CyclicDependency c) {
			super(c.getMessage(), c);
			this.metadataCodesWithCyclicDependency = c.getCyclicDependencies();
		}

		public List<String> getMetadataCodesWithCyclicDependency() {
			return Collections.unmodifiableList(metadataCodesWithCyclicDependency);
		}
	}

	public static class CannotModifyAttributeOfInheritingMetadata extends MetadataSchemaBuilderRuntimeException {
		public CannotModifyAttributeOfInheritingMetadata(String metadataCode, String attribute) {
			super("Cannot modify '" + attribute + "' for metadata '" + metadataCode + "'");
		}
	}

	public static class InvalidAttribute extends MetadataSchemaBuilderRuntimeException {
		public InvalidAttribute(String attribute, String label) {
			super("Invalid '" + attribute + "' : '" + label + "'");
		}
	}

}
