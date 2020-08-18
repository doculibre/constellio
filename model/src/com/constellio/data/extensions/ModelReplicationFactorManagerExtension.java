package com.constellio.data.extensions;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCaches;

public class ModelReplicationFactorManagerExtension extends ReplicationFactorManagerExtension {

	private RecordServices recordServices;

	public ModelReplicationFactorManagerExtension(ModelLayerFactory modelLayerFactory) {
		recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void onTransactionsReplayed(TransactionsReplayedParams params) {
		for (TransactionReplayed transaction : params.getReplayedTransactions()) {
			RecordsCaches recordsCaches = recordServices.getRecordsCaches();

			if (transaction.getVersion() == null) {
				Record record = recordsCaches.getRecord(transaction.getRecordDtoId());
				if (record != null) {
					recordsCaches.getCache(record.getCollection()).removeFromAllCaches(record.getId());
				}
			} else {
				if (recordServices.getRecordsCaches().isCached(transaction.getRecordDtoId())) {
					Record record = recordServices.realtimeGetRecordById(transaction.getRecordDtoId(), transaction.getVersion());
					// FIXME change insertion reason
					recordsCaches.getCache(record.getCollection()).insert(record, InsertionReason.WAS_MODIFIED);
				}
			}
		}
	}

}
