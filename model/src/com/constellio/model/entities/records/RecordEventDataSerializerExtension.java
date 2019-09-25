package com.constellio.model.entities.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.events.EventDataSerializerExtension;
import com.constellio.data.io.EncodingService;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache.ByteArrayRecordDTOSerializerService;

import java.io.IOException;

public class RecordEventDataSerializerExtension implements EventDataSerializerExtension {

	ModelLayerFactory modelLayerFactory;
	ByteArrayRecordDTOSerializerService serializerService;
	RecordServices recordServices;
	EncodingService encodingService;

	public RecordEventDataSerializerExtension(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;

	}

	private void ensureInitialized() {
		if (serializerService == null) {
			this.serializerService = new ByteArrayRecordDTOSerializerService(modelLayerFactory);
			this.recordServices = modelLayerFactory.newRecordServices();
			this.encodingService = modelLayerFactory.getDataLayerFactory().getIOServicesFactory().newEncodingService();
		}
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

		if (record.isSummary()) {
			ensureInitialized();
			RecordDTO recordDTO = ((RecordImpl) record).getRecordDTO();
			if (recordDTO instanceof ByteArrayRecordDTOWithIntegerId) {
				byte[] serializedBytes = serializerService.serialize((ByteArrayRecordDTOWithIntegerId) recordDTO);
				String serializedBytesInBase64 = encodingService.encodeToBase64(serializedBytes);
				System.out.println("Sending &" + serializedBytesInBase64);
				return "&" + serializedBytesInBase64;
			} else {
				return "&&" + record.getId();
			}

		} else {
			return record.getId();
		}
	}

	@Override
	public Object deserialize(String data) {
		ensureInitialized();
		RecordServices recordServices = modelLayerFactory.newCachelessRecordServices();
		if (data.startsWith("&&")) {
			return recordServices.realtimeGetRecordSummaryById(data.substring(2));

		} else if (data.startsWith("&")) {
			System.out.println("Receiving " + data);
			String serializedBytesInBase64 = data.substring(1);
			byte[] serializedBytes = encodingService.decodeStringToBase64Bytes(serializedBytesInBase64);

			try {
				RecordDTO recordDTO = serializerService.deserialize(serializedBytes);
				return recordServices.toRecord(recordDTO, recordDTO.getLoadingMode() == RecordDTOMode.FULLY_LOADED);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		} else {
			return recordServices.realtimeGetRecordById(data);
		}
	}
}
