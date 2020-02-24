package com.constellio.app.modules.rm.services.logging;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.util.List;

public class DecommissioningLoggingService {
	ModelLayerFactory modelLayerFactory;

	public DecommissioningLoggingService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void logDecommissioning(DecommissioningList decommissioningList, User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Record record = decommissioningList.getWrappedRecord();
		Event event = schemasRecords.newEvent();

		switch (decommissioningList.getDecommissioningListType()) {
			case FOLDERS_TO_DEPOSIT:
				event.setType(EventType.FOLDER_DEPOSIT);
				break;
			case FOLDERS_TO_DESTROY:
				event.setType(EventType.FOLDER_DESTRUCTION);
				break;
			case FOLDERS_TO_TRANSFER:
				event.setType(EventType.FOLDER_RELOCATION);
				break;
			default: //FIXME
				return;
		}
		setDefaultMetadata(event, user);
		setRecordMetadata(event, record);

		executeTransaction(event.getWrappedRecord());
	}

	private void executeTransaction(Record record) {
		Transaction transaction = new Transaction();
		transaction.setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(1));
		transaction.add(record);
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			//TODO
			throw new RuntimeException(e.getMessage());
		}
	}

	private void setRecordMetadata(Event event, Record record) {
		event.setRecordId(record.getId());
		String principalPath = (String) record.get(Schemas.PRINCIPAL_PATH);
		event.setEventPrincipalPath(principalPath);
	}

	private void setDefaultMetadata(Event event, User user) {
		event.setUsername(user.getUsername());
		List<String> roles = user.getAllRoles();
		//event.setUserRoles(StringUtils.join(roles.toArray(), "; "));
		event.setCreatedOn(TimeProvider.getLocalDateTime());
	}

	public void logPdfAGeneration(Document document, User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		event.setType(EventType.PDF_A_GENERATION);
		event.setTitle(document.getTitle());

		setDefaultMetadata(event, user);
		setRecordMetadata(event, document.getWrappedRecord());

		executeTransaction(event.getWrappedRecord());
	}
}
