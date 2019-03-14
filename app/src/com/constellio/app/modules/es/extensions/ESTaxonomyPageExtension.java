package com.constellio.app.modules.es.extensions;

import com.constellio.app.api.extensions.TaxonomyPageExtension;
import com.constellio.app.api.extensions.params.CanManageTaxonomyParams;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class ESTaxonomyPageExtension extends TaxonomyPageExtension {

	private String collection;

	public ESTaxonomyPageExtension(String collection) {
		this.collection = collection;
	}

	@Override
	public ExtensionBooleanResult canManageTaxonomy(CanManageTaxonomyParams canManageTaxonomyParams) {
		if (canManageTaxonomyParams.getTaxonomy().getCode().equals(ESTaxonomies.SMB_FOLDERS) || canManageTaxonomyParams.getTaxonomy().getCode().equals(ESTaxonomies.SHAREPOINT_FOLDERS)) {
			return ExtensionBooleanResult.FALSE;

		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

}
