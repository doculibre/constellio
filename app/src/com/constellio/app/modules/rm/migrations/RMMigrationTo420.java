package com.constellio.app.modules.rm.migrations;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.all;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RMMigrationTo420 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "420";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor420(collection, migrationResourcesProvider, appLayerFactory).migrate();

		createSchema420Type(collection, appLayerFactory.getModelLayerFactory());

		deleteAllFacetsExceptOneThatWillBeModified(collection, appLayerFactory);
		add420Record(collection, appLayerFactory);
	}

	static void add420Record(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory()
				.newRecordServices();
		Facet recordCreated = rm.newFacetField()
				.setActive(true).setElementPerPage(5).setFacetType(FacetType.FIELD)
				.setFieldDataStoreCode(rm.folderAdministrativeUnit().getDataStoreCode()).setTitle("420 created");
		Transaction transaction = new Transaction();
		transaction.add(recordCreated);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	static void deleteAllFacetsExceptOneThatWillBeModified(String collection, AppLayerFactory appLayerFactory){
		SearchServices searchService = appLayerFactory.getModelLayerFactory().newSearchServices();
		SchemasRecordsServices schemas = new ESSchemasRecordsServices(collection, appLayerFactory);
		List<Record> allFacets = searchService.search(new LogicalSearchQuery(from(schemas.facetSchemaType()).returnAll()));
		if(allFacets.isEmpty()){
			return;
		}
		Facet firstFacet = schemas.wrapFacet(allFacets.get(0));
		RecordServices recordServices = appLayerFactory.getModelLayerFactory()
				.newRecordServices();
		Transaction transaction = new Transaction();
		transaction.add(firstFacet.setTitle("420"));
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		for(int i = 1; i < allFacets.size(); i++){
			Record currentFacet = allFacets.get(i);
			recordServices.logicallyDelete(currentFacet, null);
			recordServices.physicallyDelete(currentFacet, null);
		}
	}

	static void createSchema420Type(String collection, ModelLayerFactory modelLayerFactory) {

		MetadataSchemasManager schemaManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemaManager.modify(collection);
		types.createNewSchemaType("Schema420");
		try {
			schemaManager.saveUpdateSchemaTypes(types);
		} catch (OptimistickLocking optimistickLocking) {
			throw new java.lang.RuntimeException(optimistickLocking);
		}
		modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType("Schema420");
	}

	class SchemaAlterationFor420 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor420(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "420.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//nothing
		}

	}
}

