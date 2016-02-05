package com.constellio.app.modules.rm.extensions;

import static java.util.Arrays.asList;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

public class RMUserRecordExtension extends RecordExtension {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	public RMUserRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(User.SCHEMA_TYPE)) {
			handle(event.getRecord());
		}
	}

	@Override
	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(User.SCHEMA_TYPE)) {
			handle(event.getRecord());
		}
	}

	private void handle(Record record) {
		User user = rm.wrapUser(record);
		if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
			user.setUserRoles(asList(RMRoles.USER));
		}
	}

}
