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

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class BatchProcessTasksFactory {

	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final TaskList taskList;

	public BatchProcessTasksFactory(RecordServices recordServices, SearchServices searchServices, TaskList taskList) {
		this.recordServices = recordServices;
		this.searchServices = searchServices;
		this.taskList = taskList;
	}

	public List<BatchProcessTask> createBatchProcessTasks(BatchProcess batchProcess, List<String> records, List<String> errorList,
			int numberOfRecordsPerTask, MetadataSchemasManager schemasManager) {
		MetadataSchemaTypes metadataSchemaTypes = schemasManager.getSchemaTypes(batchProcess.getCollection());
		List<BatchProcessTask> tasks = new ArrayList<>();
		for (int i = 0; i < records.size(); i += numberOfRecordsPerTask) {
			List<String> recordsForTask = records.subList(i, Math.min(records.size(), i + numberOfRecordsPerTask));
			BatchProcessTask reindexationTask = new BatchProcessTask(taskList, recordsForTask, batchProcess.getAction(),
					recordServices,
					metadataSchemaTypes, searchServices);
			tasks.add(reindexationTask);
		}
		return tasks;
	}

}
