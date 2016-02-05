package com.constellio.app.modules.es.connectors.spi;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.model.entities.records.Record;

public abstract class Connector {

	protected ESSchemasRecordsServices es;
	protected ConnectorLogger logger;
	private Record instance;
	protected ConnectorEventObserver eventObserver;

	public abstract List<ConnectorJob> getJobs();

	protected abstract void initialize(Record instance);

	public abstract List<String> fetchTokens(String username);

	public abstract List<String> getConnectorDocumentTypes();

	public abstract void start();

	public abstract void stop();

	public abstract void afterJobs(List<ConnectorJob> jobs);

	public abstract void resume();

	public abstract List<String> getReportMetadatas(String reportMode);

	public abstract String getMainConnectorDocumentType();

	public abstract void onAllDocumentsDeleted();

	public Connector initialize(ConnectorLogger logger, Record instance, ConnectorEventObserver eventObserver,
			ESSchemasRecordsServices es) {
		this.logger = logger;
		this.instance = instance;
		this.eventObserver = eventObserver;
		this.es = es;
		initialize(instance);
		return this;
	}

	public ConnectorLogger getLogger() {
		return logger;
	}

	public ConnectorEventObserver getEventObserver() {
		return eventObserver;
	}

	public ESSchemasRecordsServices getEs() {
		return es;
	}

	public void setEs(ESSchemasRecordsServices es) {
		this.es = es;
	}

	public List<ConnectorField> getDefaultConnectorFields() {
		return new ArrayList<>();
	}
}
