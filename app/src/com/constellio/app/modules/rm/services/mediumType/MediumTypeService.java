package com.constellio.app.modules.rm.services.mediumType;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.records.RecordServices;

import java.util.ArrayList;
import java.util.List;

public class MediumTypeService {

	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;
	private String collection;

	public MediumTypeService(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public List<MediumType> getActivatedOnContentMediumTypes() {
		List<MediumType> activatedOnContentMediumTypes = new ArrayList<>();

		List<MediumType> mediumTypes = rm.wrapMediumTypes(recordServices.getRecordsCaches().getCache(collection)
				.getAllValuesInUnmodifiableState(MediumType.SCHEMA_TYPE));
		for (MediumType mediumType : mediumTypes) {
			if (!mediumType.isAnalogical() && mediumType.isActivatedOnContent()) {
				activatedOnContentMediumTypes.add(mediumType);
			}
		}
		return activatedOnContentMediumTypes;
	}

}
