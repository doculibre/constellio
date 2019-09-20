package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.SIPExtension;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPIsTaxonomySupportedParams;
import com.constellio.app.api.extensions.params.ExportCollectionInfosSIPParams;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

import java.io.IOException;

public class RMSIPExtension extends SIPExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMSIPExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void exportCollectionInfosSIP(ExportCollectionInfosSIPParams params) throws IOException {
		params.getCollectionInfosWriter().exportRecordsInSchemaTypesDivision(UniformSubdivision.SCHEMA_TYPE);
		params.getCollectionInfosWriter().exportRecordsInSchemaTypesDivision(RetentionRule.SCHEMA_TYPE);
		params.getCollectionInfosWriter().exportRecordsInSchemaTypesDivision(DecommissioningList.SCHEMA_TYPE);
		params.getCollectionInfosWriter().exportRecordsInSchemaTypesDivision(Cart.SCHEMA_TYPE);
	}

	@Override
	public ExtensionBooleanResult isExportedTaxonomyInSIPCollectionInfos(
			ExportCollectionInfosSIPIsTaxonomySupportedParams params) {
		return ExtensionBooleanResult.falseIf(RMTaxonomies.STORAGES.equals(params.getTaxonomy().getCode()));
	}
}
