package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.Metadata;

@SuppressWarnings("serial")
public class MetadataBuilderRuntimeException extends RuntimeException {

	public MetadataBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataBuilderRuntimeException(String message) {
		super(message);
	}

	public MetadataBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class MetadataCannotBeUniqueAndMultivalue extends MetadataSchemaBuilderRuntimeException {
		public MetadataCannotBeUniqueAndMultivalue(String code) {
			super("Metadata '" + code + " cannot be unique and multivalue");
		}
	}

	public static class InvalidAttribute extends MetadataBuilderRuntimeException {
		public InvalidAttribute(String metadata, String attribute) {
			super("Invalid attribute '" + attribute + "' on metadata '" + metadata + "'");
		}

		public InvalidAttribute(String metadata, String attribute, Exception e) {
			super("Invalid attribute '" + attribute + "' on metadata '" + metadata + "'", e);
		}
	}

	public static class EssentialMetadataCannotBeDisabled extends MetadataBuilderRuntimeException {
		public EssentialMetadataCannotBeDisabled(String metadata) {
			super("Essential metadata cannot be disabled");
		}
	}

	public static class MultilingualMetadatasNotSupportedWithPermanentSummaryCache extends MetadataBuilderRuntimeException {
		public MultilingualMetadatasNotSupportedWithPermanentSummaryCache(Metadata metadata) {
			super("Multilingua metadatas not supported with permanent summary cache : " + metadata.getCode());
		}
	}

	public static class MetadataEnteredManuallyCannotBeTransient extends MetadataBuilderRuntimeException {
		public MetadataEnteredManuallyCannotBeTransient(String metadata) {
			super("Metadata entered manually cannot be transient : " + metadata);
		}
	}

	public static class ReferenceCannotBeTransient extends MetadataBuilderRuntimeException {
		public ReferenceCannotBeTransient(String metadata) {
			super("Reference metadata cannot be transient : " + metadata);
		}
	}

	public static class EssentialMetadataInSummaryCannotBeDisabled extends MetadataBuilderRuntimeException {
		public EssentialMetadataInSummaryCannotBeDisabled(String metadata) {
			super("Metadata '" + metadata + "' cannot be disabled, since it is essential in summary");
		}
	}

	public static class AllowedReferencesOnlyUsableOnReferenceTypeMetadata extends MetadataBuilderRuntimeException {
		public AllowedReferencesOnlyUsableOnReferenceTypeMetadata(String metadata) {
			super("Cannot set allowed references to metadata '" + metadata + "', since it doesn't have a reference type");
		}
	}

	public static class CannotCreateMultivalueReferenceToPrincipalTaxonomy extends MetadataBuilderRuntimeException {
		public CannotCreateMultivalueReferenceToPrincipalTaxonomy(String metadata) {
			super("Cannot create multivalue reference to principal taxonomy. Metadata '" + metadata);
		}
	}

	public static class ClassNotFound extends MetadataBuilderRuntimeException {
		public ClassNotFound(String className) {
			super("Class not found : '" + className + "'");
		}
	}

	public static class CannotInstanciateClass extends MetadataBuilderRuntimeException {
		public CannotInstanciateClass(String className, Exception e) {
			super("Can not instanciate class: '" + className + "'", e);
		}
	}

	public static class InvalidClass extends MetadataBuilderRuntimeException {
		public InvalidClass(String interfaceName, Exception e) {
			super("Class doesn't implement the interface: '" + interfaceName + "'", e);
		}
	}

	public static class EnumClassMustImplementEnumWithSmallCode extends MetadataBuilderRuntimeException {

		public EnumClassMustImplementEnumWithSmallCode(Class<? extends Enum<?>> enumClass) {
			super("Enum class '" + enumClass + "'  must implement EnumWithSmallCode");
		}
	}
}
