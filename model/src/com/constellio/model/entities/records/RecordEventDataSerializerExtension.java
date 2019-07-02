package com.constellio.model.entities.records;

import com.constellio.data.events.EventDataSerializerExtension;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;

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
		Record record = (Record) data;
		return (record.isSummary() ? "&" : "") + record.getId();
	}

	@Override
	public Object deserialize(String recordId) {

		RecordServices recordServices = modelLayerFactory.newCachelessRecordServices();
		if (recordId.startsWith("&")) {
			return recordServices.realtimeGetRecordSummaryById(recordId.substring(1));
		} else {
			return recordServices.realtimeGetRecordById(recordId);
		}
	}
}
