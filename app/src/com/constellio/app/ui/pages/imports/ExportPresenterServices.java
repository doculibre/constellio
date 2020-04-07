package com.constellio.app.ui.pages.imports;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.RecordExportOptions;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class ExportPresenterServices {

	String collection;

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;

	SearchServices searchServices;

	public ExportPresenterServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}


	public RecordExportOptions buildOptionsForExportingAdministrativeUnitsAndItsContent(boolean isSameCollection,
																						List<String> unitIds,
																						boolean includeAuthorizations) {
		RecordExportOptions options = new RecordExportOptions();
		options.setForSameSystem(isSameCollection);

		List<String> paths = new ArrayList<>();
		for (String unit : unitIds) {
			paths.add((String) ((List) recordServices.getDocumentById(unit).get(Schemas.PATH)).get(0));
		}

		MetadataSchemaType decommissioningListSchemaType = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchemaType(DecommissioningList.SCHEMA_TYPE);


		SearchResponseIterator<Record> recordsIterator = searchServices.recordsIterator(
				fromAllSchemasIn(collection)
						.where(Schemas.PATH).isStartingWithTextFromAny(paths)
						.orWhere(decommissioningListSchemaType.getDefaultSchema().get(DecommissioningList.ADMINISTRATIVE_UNIT)).isIn(unitIds)
		);

		options.setRecordsToExportIterator(recordsIterator);
		options.setIncludeAuthorizations(includeAuthorizations);
		return options;
	}
}
