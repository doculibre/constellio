package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.CanConsultTaxonomyParams;
import com.constellio.app.api.extensions.params.CanManageTaxonomyParams;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.api.extensions.taxonomies.ValidateTaxonomyDeletableParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class TaxonomyPageExtension implements Serializable {

	public ExtensionBooleanResult canManageTaxonomy(CanManageTaxonomyParams canManageTaxonomyParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypesFor(
			GetTaxonomyManagementClassifiedTypesParams params) {
		return Collections.emptyList();
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFieldsFor(GetTaxonomyExtraFieldsParam params) {
		return Collections.emptyList();
	}

	public ExtensionBooleanResult canConsultTaxonomy(CanConsultTaxonomyParams canConsultTaxonomyParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult displayTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public String getSortMetadataCode(Taxonomy taxonomy) {
		return Schemas.CODE.getLocalCode();
	}

	public void validateTaxonomyDeletable(ValidateTaxonomyDeletableParams validateTaxonomyDeletableParams) {
	}

}
