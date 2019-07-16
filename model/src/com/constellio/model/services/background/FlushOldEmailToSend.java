package com.constellio.model.services.background;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class FlushOldEmailToSend implements Runnable {
	private static final double DAYS_TO_BE_OLD_EMAIL = 3.0;

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private CollectionsListManager collectionsListManager;

	public FlushOldEmailToSend(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	@Override
	public synchronized void run() {
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

			LogicalSearchCondition condition = getConditionForOldEmailsToSend(schemas);
			List<Record> oldEmailsToSend = getOldEmailsToSend(condition);
			physicallyDeleteOldEmailsToSend(oldEmailsToSend);
		}
	}

	private LogicalSearchCondition getConditionForOldEmailsToSend(SchemasRecordsServices schemas) {
		MetadataSchema schema = schemas.schema(EmailToSend.DEFAULT_SCHEMA);
		Metadata sendOn = schemas.emailToSend.sendOn();

		return from(schema).where(sendOn).isOlderThan(DAYS_TO_BE_OLD_EMAIL, MeasuringUnitTime.DAYS);
	}

	private List<Record> getOldEmailsToSend(LogicalSearchCondition condition) {
		return searchServices.search(LogicalSearchQuery.query(condition));
	}

	private void physicallyDeleteOldEmailsToSend(List<Record> oldEmailsToSend) {
		for (Record email : oldEmailsToSend) {
			recordServices.physicallyDeleteNoMatterTheStatus(email, User.GOD, new RecordPhysicalDeleteOptions());
		}
	}
}
