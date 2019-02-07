package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.app.api.extensions.taxonomies.ValidateTaxonomyDeletableParams;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class TaxonomyPageExtension implements Serializable {

	public ExtensionBooleanResult canManageTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypesFor(
			GetTaxonomyManagementClassifiedTypesParams params) {
		return Collections.emptyList();
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFieldsFor(GetTaxonomyExtraFieldsParam params) {
		return Collections.emptyList();
	}

	public ExtensionBooleanResult canConsultTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult displayTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public void validateTaxonomyDeletable(ValidateTaxonomyDeletableParams validateTaxonomyDeletableParams) {
	}

}
