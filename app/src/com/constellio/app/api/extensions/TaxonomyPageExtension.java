package com.constellio.app.api.extensions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.constellio.app.api.extensions.taxonomies.GetTaxonomyExtraFieldsParam;
import com.constellio.app.api.extensions.taxonomies.GetTaxonomyManagementClassifiedTypesParams;
import com.constellio.app.api.extensions.taxonomies.TaxonomyExtraField;
import com.constellio.app.api.extensions.taxonomies.TaxonomyManagementClassifiedType;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class TaxonomyPageExtension implements Serializable {

	public ExtensionBooleanResult canManageTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public List<TaxonomyManagementClassifiedType> getClassifiedTypesFor(GetTaxonomyManagementClassifiedTypesParams params) {
		return Collections.emptyList();
	}

	public List<TaxonomyExtraField> getTaxonomyExtraFieldsFor(GetTaxonomyExtraFieldsParam params) {
		return Collections.emptyList();
	}

	public ExtensionBooleanResult displayTaxonomy(User user, Taxonomy taxonomy) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
