package com.constellio.app.entities.modules;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class MigrationTools {

	String collection;
	MigrationResourcesProvider migrationResourcesProvider;
	AppLayerFactory appLayerFactory;

	public MigrationTools(String collection,
			MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;
	}

	public String getCollection() {
		return collection;
	}

	public MigrationResourcesProvider getMigrationResourcesProvider() {
		return migrationResourcesProvider;
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	public void reindex(LogicalSearchCondition condition) {

		BatchProcessesManager batchProcessesManager = appLayerFactory.getModelLayerFactory().getBatchProcessesManager();
		BatchProcessAction action = ReindexMetadatasBatchProcessAction.allMetadatas();
		Map<String, Object> params = new HashMap<>();
		batchProcessesManager.addPendingBatchProcess(condition, action, $("reindexing", params));

	}
}
