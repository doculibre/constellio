package com.constellio.app.extensions.api.cmis.params;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.server.CallContext;

import com.constellio.model.entities.records.Record;

public class BuildConstellioRecordFromCmisObjectParams {

	private Record record;

	private Properties properties;

	private CallContext context;

	public BuildConstellioRecordFromCmisObjectParams(Record record, Properties properties, CallContext context) {
		this.record = record;
		this.properties = properties;
		this.context = context;
	}

	public Record getRecord() {
		return record;
	}

	public Properties getProperties() {
		return properties;
	}

	public CallContext getContext() {
		return context;
	}
}
