package com.constellio.app.modules.robots.model;

import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;

import java.util.List;

public interface ActionExecutor {

	Transaction execute(String robotId, ActionParameters actionParameters, AppLayerFactory appLayerFactory,
						List<Record> records, List<Record> processedRecords, boolean dryRun);
}
