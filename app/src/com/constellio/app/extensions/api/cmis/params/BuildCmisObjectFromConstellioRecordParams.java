package com.constellio.app.extensions.api.cmis.params;

import com.constellio.app.api.cmis.builders.object.PropertiesBuilder;
import com.constellio.model.entities.records.Record;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;

public class BuildCmisObjectFromConstellioRecordParams {

	private ObjectDataImpl result;
	private PropertiesBuilder propertiesBuilder;

	private Record record;

	public BuildCmisObjectFromConstellioRecordParams(ObjectDataImpl result, PropertiesBuilder propertiesBuilder,
													 Record record) {
		this.record = record;
		this.result = result;
		this.propertiesBuilder = propertiesBuilder;
	}

	public Record getRecord() {
		return record;
	}

	public ObjectDataImpl getResult() {
		return result;
	}

	public PropertiesBuilder getPropertiesBuilder() {
		return propertiesBuilder;
	}
}
