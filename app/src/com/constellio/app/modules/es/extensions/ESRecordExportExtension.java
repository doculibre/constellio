package com.constellio.app.modules.es.extensions;


import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ESRecordExportExtension extends RecordExportExtension {

	@Override
	public List<String> getUnwantedTaxonomiesForExportation() {
		return asList(ESTaxonomies.ALL_EN_TAXONOMIES);
	}
}
