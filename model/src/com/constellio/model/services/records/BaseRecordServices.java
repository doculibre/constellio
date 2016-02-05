package com.constellio.model.services.records;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class BaseRecordServices implements RecordServices {

	ModelLayerFactory modelLayerFactory;

	protected BaseRecordServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public final void add(Record record)
			throws RecordServicesException {
		add(record, null);
	}

	public final void add(Record record, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction().setUser(user);
		transaction.addUpdate(record);
		execute(transaction);
	}

	public final void add(RecordWrapper wrapper)
			throws RecordServicesException {
		add(wrapper.getWrappedRecord(), null);
	}

	public final void add(RecordWrapper wrapper, User user)
			throws RecordServicesException {
		add(wrapper.getWrappedRecord(), user);
	}

	public final void update(RecordWrapper wrapper)
			throws RecordServicesException {
		update(wrapper, null);
	}

	//TODO Make this method final
	public void update(Record record)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), null);
	}

	public final void update(Record record, RecordUpdateOptions options)
			throws RecordServicesException {
		update(record, options, null);
	}

	public final void update(RecordWrapper wrapper, User user)
			throws RecordServicesException {
		update(wrapper.getWrappedRecord(), user);
	}

	public final void update(Record record, User user)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), user);
	}

	public final void update(Record record, RecordUpdateOptions options, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setUser(user);
		transaction.addUpdate(record);
		transaction.setOptions(options);
		execute(transaction);
	}

	public final void update(List<Record> records, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction(records);
		transaction.setUser(user);
		execute(transaction);
	}

	public final void refresh(Record... records) {
		refresh(Arrays.asList(records));
	}

	public final void refresh(RecordWrapper... recordWrappers) {
		for (RecordWrapper wrapper : recordWrappers) {
			if (wrapper != null && wrapper.getWrappedRecord() != null) {
				refresh(wrapper.getWrappedRecord());
			}
		}
	}

	public final List<BatchProcess> updateAsync(Record record)
			throws RecordServicesException {
		return updateAsync(record, new RecordUpdateOptions());
	}

	public final List<BatchProcess> updateAsync(Record record, RecordUpdateOptions options)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.update(record);
		transaction.setOptions(options);
		return executeHandlingImpactsAsync(transaction);
	}

	public final Record getDocumentById(String id, User user) {
		Record record = getDocumentById(id);
		if (modelLayerFactory.newAuthorizationsServices().canRead(user, record)) {
			return record;
		} else {
			throw new RecordServicesRuntimeException.UserCannotReadDocument(id, user.getUsername());
		}
	}

}
