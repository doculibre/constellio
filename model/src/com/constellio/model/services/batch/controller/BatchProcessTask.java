package com.constellio.model.services.batch.controller;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.*;
import com.constellio.model.services.records.RecordServicesException.OptimisticLocking;
import com.constellio.model.services.search.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

@SuppressWarnings("serial")
public class BatchProcessTask extends RecursiveTask<List<String>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessTask.class);

	private final List<Record> records;
	private final RecordServices recordServices;
	private final MetadataSchemaTypes metadataSchemaTypes;
	private final BatchProcessAction action;
	private final SearchServices searchServices;
	private final TaskList taskList;
	private final User user;

	public BatchProcessTask(TaskList taskList, List<Record> records, BatchProcessAction action, RecordServices recordServices,
							MetadataSchemaTypes metadataSchemaTypes, SearchServices searchServices, User user) {
		this.records = records;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.action = action;
		this.searchServices = searchServices;
		this.taskList = taskList;
		this.user = user;

		List<String> ids = new RecordUtils().toIdList(records);
		Set<String> idsSet = new HashSet<>(ids);
		if (ids.size() != idsSet.size()) {
			throw new ImpossibleRuntimeException("Duplicate ids!");
		}
	}

	@Override
	protected List<String> compute() {
		List<String> errors = new ArrayList<>();
		try {
			execute(records, errors);
			taskList.onTaskFinished();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Could not execute action", e);
		}
		return errors;
	}

	void execute(List<Record> batch, List<String> errors) {
		Transaction transaction = null;
		try {
			transaction = action.execute(batch, metadataSchemaTypes, new RecordProvider(recordServices));

		} catch (Throwable t) {
			t.printStackTrace();
			LOGGER.error("Error while executing batch process action", t);
			addRecordsIdsToErrorList(batch, errors);
		}

		if (transaction != null) {
			RecordModificationImpactHandler handler = createSubTaskImpactHandler();
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
			transaction.setUser(user);
			try {
				recordServices.executeWithImpactHandler(transaction, handler);

			} catch (OptimisticLocking e) {
				//e.printStackTrace();
				LOGGER.info("Optimistic locking, retrying transaction ...");
				//LOGGER.info("Optimistic locking, retrying transaction ...\n" + LoggerUtils.toString(e.getTransactionDTO()));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
				List<String> recordIds = new RecordUtils().toIdList(records);
				List<Record> newBatch = recordServices.getRecordsById(batch.get(0).getCollection(), recordIds);
				execute(newBatch, errors);

			} catch (RecordServicesRuntimeException | RecordServicesException t) {
				t.printStackTrace();
				LOGGER.error("Error while executing transaction", t);
				addRecordsIdsToErrorList(batch, errors);
			}
		}

	}

	RecordModificationImpactHandler createSubTaskImpactHandler() {
		return new CreateSubTaskModificationImpactHandler(searchServices, recordServices, metadataSchemaTypes, taskList, user);
	}

	private void addRecordsIdsToErrorList(List<Record> batch, List<String> errors) {
		for (Record record : batch) {
			errors.add(record.getId());
		}
	}

}
