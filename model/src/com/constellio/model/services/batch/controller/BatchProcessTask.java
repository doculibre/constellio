/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.batch.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.OptimisticLocking;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;

@SuppressWarnings("serial")
public class BatchProcessTask extends RecursiveTask<List<String>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessTask.class);

	private final List<String> recordIds;
	private final RecordServices recordServices;
	private final MetadataSchemaTypes metadataSchemaTypes;
	private final BatchProcessAction action;
	private final SearchServices searchServices;
	private final TaskList taskList;

	public BatchProcessTask(TaskList taskList, List<String> recordIds, BatchProcessAction action, RecordServices recordServices,
			MetadataSchemaTypes metadataSchemaTypes, SearchServices searchServices) {
		this.recordIds = recordIds;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.action = action;
		this.searchServices = searchServices;
		this.taskList = taskList;
	}

	@Override
	protected List<String> compute() {
		List<String> errors = new ArrayList<>();
		List<Record> records = recordServices.getRecordsById(metadataSchemaTypes.getCollection(), recordIds);
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
			transaction = action.execute(batch, metadataSchemaTypes);

		} catch (Throwable t) {
			t.printStackTrace();
			LOGGER.error("Error while executing batch process action", t);
			addRecordsIdsToErrorList(batch, errors);
		}

		if (transaction != null) {
			RecordModificationImpactHandler handler = createSubTaskImpactHandler();
			transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
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
		return new CreateSubTaskModificationImpactHandler(searchServices, recordServices, metadataSchemaTypes, taskList);
	}

	private void addRecordsIdsToErrorList(List<Record> batch, List<String> errors) {
		for (Record record : batch) {
			errors.add(record.getId());
		}
	}

}
