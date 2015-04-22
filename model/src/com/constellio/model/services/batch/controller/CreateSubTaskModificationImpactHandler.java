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
import java.util.Iterator;
import java.util.List;

import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class CreateSubTaskModificationImpactHandler implements RecordModificationImpactHandler {

	List<ModificationImpact> impacts = new ArrayList<>();

	SearchServices searchServices;

	RecordServices recordServices;

	MetadataSchemaTypes metadataSchemaTypes;

	TaskList taskList;

	public CreateSubTaskModificationImpactHandler(SearchServices searchServices, RecordServices recordServices,
			MetadataSchemaTypes metadataSchemaTypes, TaskList taskList) {
		this.searchServices = searchServices;
		this.recordServices = recordServices;
		this.metadataSchemaTypes = metadataSchemaTypes;
		this.taskList = taskList;
	}

	@Override
	public void prepareToHandle(ModificationImpact modificationImpact) {
		impacts.add(modificationImpact);

	}

	@Override
	public void handle() {
		for (ModificationImpact modificationImpact : impacts) {
			handleModificationImpact(modificationImpact);
		}
	}

	void handleModificationImpact(ModificationImpact modificationImpact) {
		List<String> metadatas = newSchemaUtils().toMetadataCodes(modificationImpact.getMetadataToReindex());

		Iterator<List<String>> batchIterator = getBatchOfIdsIterator(modificationImpact);
		while (batchIterator.hasNext()) {
			createSubTask(batchIterator.next(), metadatas);
		}
	}

	void createSubTask(List<String> subRecordIds, List<String> metadatas) {
		BatchProcessAction action = new ReindexMetadatasBatchProcessAction(metadatas);
		BatchProcessTask task = new BatchProcessTask(taskList, subRecordIds, action, recordServices,
				metadataSchemaTypes, searchServices);
		taskList.addSubTask(task);
	}

	Iterator<List<String>> getBatchOfIdsIterator(ModificationImpact modificationImpact) {
		LogicalSearchCondition condition = modificationImpact.getLogicalSearchCondition();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		Iterator<String> iterator = searchServices.optimizesRecordsIdsIterator(query, 10000);
		return new BatchBuilderIterator<>(iterator, 1000);
	}

	@Override
	public void cancel() {

	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}
}
