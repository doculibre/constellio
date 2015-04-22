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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.batch.actions.ReindexMetadatasBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class AddToBatchProcessImpactHandler implements RecordModificationImpactHandler {

	BatchProcessesManager manager;

	SearchServices searchServices;

	List<BatchProcess> createdBatchProcesses = new ArrayList<>();

	public AddToBatchProcessImpactHandler(BatchProcessesManager manager, SearchServices searchServices) {
		this.manager = manager;
		this.searchServices = searchServices;
	}

	@Override
	public void prepareToHandle(ModificationImpact modificationImpact) {
		LogicalSearchQuery query = new LogicalSearchQuery(modificationImpact.getLogicalSearchCondition());
		List<String> recordIds = searchServices.searchRecordIds(query);
		if (!recordIds.isEmpty()) {
			BatchProcessAction action = newBatchProcessAction(modificationImpact.getMetadataToReindex());
			String collection = modificationImpact.getMetadataToReindex().get(0).getCollection();
			BatchProcess batchProcess = this.manager.add(recordIds, collection, action);
			createdBatchProcesses.add(batchProcess);

		}
	}

	@Override
	public void handle() {
		for (BatchProcess batchProcess : createdBatchProcesses) {
			this.manager.markAsPending(batchProcess);
		}
	}

	@Override
	public void cancel() {
		for (BatchProcess batchProcess : createdBatchProcesses) {
			this.manager.cancelStandByBatchProcess(batchProcess);
		}
		this.createdBatchProcesses.clear();
	}

	BatchProcessAction newBatchProcessAction(List<Metadata> metadatas) {
		return ReindexMetadatasBatchProcessAction.forMetadatas(metadatas);
	}

	public List<BatchProcess> getAllCreatedBatchProcesses() {
		return createdBatchProcesses;
	}
}
