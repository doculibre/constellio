package com.constellio.app.modules.es.extensions;

import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;

public class ESTaxonomyPageExtension extends TaxonomyPageExtension {

	private String collection;

	public ESTaxonomyPageExtension(String collection) {
		this.collection = collection;
	}

	@Override
	public ExtensionBooleanResult canManageTaxonomy(User user, Taxonomy taxonomy) {
		if (taxonomy.getCode().equals(ESTaxonomies.SMB_FOLDERS) || taxonomy.getCode().equals(ESTaxonomies.SHAREPOINT_FOLDERS)) {
			return ExtensionBooleanResult.FALSE;

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

}
