package com.constellio.app.modules.es.connectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.List;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConnectorDeleterJob extends ConnectorJob {
	final List<String> recordsToDelete;

	public ConnectorDeleterJob(Connector connector, List<String> recordsToDelete) {
		super(connector, "commonDeleterJob");
		this.recordsToDelete = recordsToDelete;
	}

	@Override
	public void execute(Connector connector) {
		LogicalSearchCondition condition = fromAllSchemasIn(
				connector.getEs().getCollection()).where(Schemas.IDENTIFIER).isIn(recordsToDelete);
		List<ConnectorDocument<?>> documents = connector.getEs().searchConnectorDocuments(new LogicalSearchQuery(condition));
		for (ConnectorDocument<?> document : documents) {
			connector.getEventObserver().deleteEvents(document);
		}
	}
}
