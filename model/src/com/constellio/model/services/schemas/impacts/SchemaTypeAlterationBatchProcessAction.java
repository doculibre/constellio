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
package com.constellio.model.services.schemas.impacts;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class SchemaTypeAlterationBatchProcessAction implements BatchProcessAction {

	List<String> reindexedMetadataForSearch = new ArrayList<>();
	List<String> convertedToSingleValue = new ArrayList<>();
	List<String> convertedToMultiValue = new ArrayList<>();

	public SchemaTypeAlterationBatchProcessAction(List<String> reindexedMetadataForSearch, List<String> convertedToSingleValue,
			List<String> convertedToMultiValue) {
		this.convertedToSingleValue = convertedToSingleValue;
		this.convertedToMultiValue = convertedToMultiValue;
		this.reindexedMetadataForSearch = reindexedMetadataForSearch;
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes) {
		Transaction transaction = new Transaction();
		transaction.getRecordUpdateOptions().forceReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.getRecordUpdateOptions().setFullRewrite(true);

		for (Record record : batch) {
			MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
			for (Metadata metadata : schema.getMetadatas()) {
				if (convertedToMultiValue.contains(metadata.getLocalCode())) {
					Metadata singleValueMetadata = Schemas.dummySingleValueMetadata(metadata);
					Object value = record.get(singleValueMetadata);
					if (value != null) {
						record.removeAllFieldsStartingWith(metadata.getLocalCode() + "_");
						record.set(metadata, asList(value));
					}

				} else if (convertedToSingleValue.contains(metadata.getLocalCode())) {
					Metadata multiValueMetadata = Schemas.dummyMultiValueMetadata(metadata);
					List<Object> values = record.getList(multiValueMetadata);
					if (!values.isEmpty()) {
						record.removeAllFieldsStartingWith(metadata.getLocalCode() + "_");
						record.set(metadata, values.get(0));

					}
				}
				if (reindexedMetadataForSearch.contains(metadata.getLocalCode())) {
					record.markAsModified(metadata);

				}
			}

			transaction.add(record);
		}

		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[] { reindexedMetadataForSearch, convertedToSingleValue, convertedToMultiValue };
	}
}
