package com.constellio.model.services.taxonomies;

import java.util.List;

@SuppressWarnings("serial")
public class TaxonomiesManagerRuntimeException extends RuntimeException {

	public TaxonomiesManagerRuntimeException(String message) {
		super(message);
	}

	public static class TaxonomyRelationsHaveCyclicDependency extends TaxonomiesManagerRuntimeException {

		public TaxonomyRelationsHaveCyclicDependency(List<String> metadataRelations) {
			super("Cyclic dependcy in metadata relations : " + metadataRelations);
		}
	}

	public static class InvalidTaxonomyCode extends TaxonomiesManagerRuntimeException {

		public InvalidTaxonomyCode(String code) {
			super("Taxonomy code '" + code + "' is invalid. Must only be composed of alphabetical characters.");
		}
	}

	public static class TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound extends TaxonomiesManagerRuntimeException {

		public TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound(String code, String collection) {
			super("Enable taxonomy code '" + code + "' not found in collection: " + collection);
		}
	}

	public static class TaxonomyRelationsRequired extends TaxonomiesManagerRuntimeException {

		public TaxonomyRelationsRequired(List<String> metadataRelations) {
			super("Metadata relations required");
		}
	}

	public static class TaxonomyAlreadyExists extends TaxonomiesManagerRuntimeException {

		public TaxonomyAlreadyExists(String code) {
			super("Taxonomy " + code + " already exists");
		}
	}

	public static class TaxonomySchemaTypesHaveRecords extends TaxonomiesManagerRuntimeException {

		public TaxonomySchemaTypesHaveRecords(String schemaType) {
			super("Cannot create/enable/disable taxonomy because there is(are) record(s) using metadataSchemaType "
					+ schemaType + " in taxonomy");
		}
	}

	public static class TaxonomyMustBeAddedBeforeSettingItHasPrincipal extends TaxonomiesManagerRuntimeException {

		public TaxonomyMustBeAddedBeforeSettingItHasPrincipal() {
			super("Taxonomy must be added before setting it has principal");
		}
	}

	public static class PrincipalTaxonomyIsAlreadyDefined extends TaxonomiesManagerRuntimeException {

		public PrincipalTaxonomyIsAlreadyDefined() {
			super("Cannot specify the principal taxonomy, since it is already defined");
		}
	}

	public static class PrincipalTaxonomyCannotBeDisabled extends TaxonomiesManagerRuntimeException {

		public PrincipalTaxonomyCannotBeDisabled() {
			super("Principal taxonomy cannot be disabled");
		}
	}

	public static class TaxonomySchemaIsReferencedInMultivalueReference extends TaxonomiesManagerRuntimeException {

		public TaxonomySchemaIsReferencedInMultivalueReference() {
			super("Cannot specify the principal taxonomy, since it is used by a multivalue reference");
		}
	}
}
