package com.constellio.model.services.records;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseRecordServices implements RecordServices {

	protected ModelLayerFactory modelLayerFactory;

	MetadataSchemasManager metadataSchemasManager;

	protected BaseRecordServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public final void add(Supplier<Record> record)
			throws RecordServicesException {
		add(record, null);
	}

	public final void add(Supplier<Record> record, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction().setUser(user);
		transaction.addUpdate(record.get());
		execute(transaction);
	}

	//TODO Make this method final
	public void update(Supplier<Record> record)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), null);
	}

	public final void update(Supplier<Record> record, RecordUpdateOptions options)
			throws RecordServicesException {
		update(record, options, null);
	}

	public final void update(Supplier<Record> record, User user)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), user);
	}

	public final void update(Supplier<Record> record, RecordUpdateOptions options, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setUser(user);
		transaction.addUpdate(record.get());
		transaction.setOptions(options);
		execute(transaction);
	}

	public final <T extends Supplier<Record>> void update(List<T> records, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		for (Supplier<Record> record : records) {
			transaction.add(record.get());
		}
		transaction.setUser(user);
		execute(transaction);
	}

	public final void refresh(Supplier<Record>... records) {
		refresh(Arrays.asList(records));
	}

	public final void refreshUsingCache(Supplier<Record>... records) {
		refreshUsingCache(Arrays.asList(records));
	}

	public final List<BatchProcess> updateAsync(Supplier<Record> record)
			throws RecordServicesException {
		return updateAsync(record, new RecordUpdateOptions());
	}

	public final List<BatchProcess> updateAsync(Supplier<Record> record, RecordUpdateOptions options)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.update(record.get());
		transaction.setOptions(options);
		return executeHandlingImpactsAsync(transaction);
	}

	public final Record getDocumentById(String id, User user, boolean callExtensions) {
		Record record = getDocumentById(id, callExtensions);
		if (!metadataSchemasManager.getSchemaTypeOf(record).hasSecurity()
			|| modelLayerFactory.newAuthorizationsServices().canRead(user, record)) {
			return record;
		} else {
			throw new RecordServicesRuntimeException.UserCannotReadDocument(id, user.getUsername());
		}
	}

	@Override
	public void validateTransaction(Transaction transaction)
			throws ValidationException {
		ValidationErrors errors = new ValidationErrors();
		for (Record record : transaction.getModifiedRecords()) {
			try {
				validateRecordInTransaction(record, transaction);
			} catch (ValidationException e) {
				errors.addAll(e.getErrors().getValidationErrors());
			}

		}
		if (!errors.isEmpty()) {
			throw new ValidationException(transaction, errors);
		}

	}
}
