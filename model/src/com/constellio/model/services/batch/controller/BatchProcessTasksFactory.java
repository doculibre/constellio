package com.constellio.model.services.batch.controller;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class BatchProcessTasksFactory {

	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final UserServices userServices;
	private final TaskList taskList;

	public BatchProcessTasksFactory(RecordServices recordServices, SearchServices searchServices, UserServices userServices, TaskList taskList) {
		this.recordServices = recordServices;
		this.searchServices = searchServices;
		this.userServices = userServices;
		this.taskList = taskList;
	}

	public List<BatchProcessTask> createBatchProcessTasks(BatchProcess batchProcess, List<Record> records, List<String> errorList,
			int numberOfRecordsPerTask, MetadataSchemasManager schemasManager) {
		MetadataSchemaTypes metadataSchemaTypes = schemasManager.getSchemaTypes(batchProcess.getCollection());
		User user = null;
		try {
			user = userServices.getUserInCollection(batchProcess.getUsername(), batchProcess.getCollection());
		} catch(UserServicesRuntimeException e) {
			//OK
		}
		List<BatchProcessTask> tasks = new ArrayList<>();
		for (int i = 0; i < records.size(); i += numberOfRecordsPerTask) {
			List<Record> recordsForTask = records.subList(i, Math.min(records.size(), i + numberOfRecordsPerTask));
			BatchProcessTask reindexationTask = new BatchProcessTask(taskList, recordsForTask, batchProcess.getAction(),
					recordServices,
					metadataSchemaTypes, searchServices, user);
			tasks.add(reindexationTask);
		}
		return tasks;
	}

}
