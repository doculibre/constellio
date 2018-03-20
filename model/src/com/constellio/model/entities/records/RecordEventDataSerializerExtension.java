package com.constellio.model.entities.records;

import com.constellio.data.events.EventDataSerializerExtension;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;

public class RecordEventDataSerializerExtension implements EventDataSerializerExtension {

	ModelLayerFactory modelLayerFactory;

	public RecordEventDataSerializerExtension(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public String getId() {
		return "record";
	}

	@Override
	public Class<?> getSupportedDataClass() {
		return RecordImpl.class;
	}

	@Override
	public String serialize(Object data) {
		return ((Record) data).getId();
	}

	@Override
	public Object deserialize(String recordId) {
		return modelLayerFactory.newRecordServices().realtimeGetRecordById(recordId);
	}
}
