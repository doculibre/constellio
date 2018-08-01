package com.constellio.model.services.background;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class TemporaryRecordsDeletionBackgroundAction implements Runnable {

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;

	public TemporaryRecordsDeletionBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	@Override
	public synchronized void run() {

		List<String> collections = collectionsListManager.getCollectionsExcludingSystem();
		for (String collection : collections) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(schemas.temporaryRecordSchemaType())
					.where(schemas.temporaryRecord().getMetadata(TemporaryRecord.DESTRUCTION_DATE))
					.isLessThan(LocalDateTime.now()));
			List<Record> recordsToDelete = searchServices.search(query);
			for (Record record : recordsToDelete) {
				recordServices.physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());
			}
		}
	}
}
