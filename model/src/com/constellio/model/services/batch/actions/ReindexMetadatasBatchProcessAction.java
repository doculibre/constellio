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
package com.constellio.model.services.batch.actions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ReindexMetadatasBatchProcessAction implements BatchProcessAction {

	final List<String> reindexedMetadataCodes;

	public ReindexMetadatasBatchProcessAction(List<String> reindexedMetadataCodes) {
		this.reindexedMetadataCodes = reindexedMetadataCodes;
	}

	public static ReindexMetadatasBatchProcessAction forMetadatas(List<Metadata> metadataToReindex) {
		List<String> codes = new ArrayList<>();
		for (Metadata metadata : metadataToReindex) {
			codes.add(metadata.getCode());
		}
		return new ReindexMetadatasBatchProcessAction(codes);
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes) {
		Transaction transaction = new Transaction();
		List<Metadata> reindexedMetadatas = schemaTypes.getMetadatas(reindexedMetadataCodes);
		transaction.getRecordUpdateOptions().forceReindexationOfMetadatas(new TransactionRecordsReindexation(reindexedMetadatas));
		transaction.setSkippingReferenceToLogicallyDeletedValidation(true);
		transaction.setSkippingRequiredValuesValidation(true);
		transaction.addUpdate(batch);
		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { reindexedMetadataCodes };
	}
}
