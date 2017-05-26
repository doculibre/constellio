package com.constellio.model.services.contents;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;

public class UserSerializedContentFactory extends ContentFactory {

	UserServices userServices;
	RecordServices recordServices;
	SchemasRecordsServices schemasRecordsServices;
	String collection;

	public UserSerializedContentFactory(String collection, ModelLayerFactory modelLayerFactory) {
		this.userServices = modelLayerFactory.newUserServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
		this.collection = collection;
	}

	@Override
	protected String deserializeUser(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		} else {
			User user = userServices.getUserInCollection(value, collection);
			return user == null ? null : user.getId();
		}
	}

	@Override
	protected String serializeUser(String value) {
		if (value == null) {
			return null;

		} else {
			Record record = recordServices.getDocumentById(value);
			if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
				return schemasRecordsServices.wrapUser(record).getUsername();
			} else {
				return null;
			}
		}

	}
}
