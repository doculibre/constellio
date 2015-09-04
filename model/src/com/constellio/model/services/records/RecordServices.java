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

import java.util.List;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public interface RecordServices {

	public void add(Record record)
			throws RecordServicesException;

	public void add(Record record, User user)
			throws RecordServicesException;

	public void add(RecordWrapper wrapper)
			throws RecordServicesException;

	public void add(RecordWrapper wrapper, User user)
			throws RecordServicesException;

	public List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException;

	public void execute(Transaction transaction)
			throws RecordServicesException;

	public void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler)
			throws RecordServicesException;

	public Record toRecord(RecordDTO recordDTO, boolean fullyLoaded);

	public List<Record> toRecords(List<RecordDTO> recordDTOs, boolean fullyLoaded);

	public long documentsCount();

	public void update(RecordWrapper wrapper)
			throws RecordServicesException;

	public void update(Record record)
			throws RecordServicesException;

	public void update(Record record, RecordUpdateOptions options)
			throws RecordServicesException;

	public void update(RecordWrapper wrapper, User user)
			throws RecordServicesException;

	public void update(Record record, User user)
			throws RecordServicesException;

	public void update(Record record, RecordUpdateOptions options, User user)
			throws RecordServicesException;

	public void update(List<Record> records, User user)
			throws RecordServicesException;

	public Record getRecordByMetadata(Metadata metadata, String value);

	public Record getDocumentById(String id);

	public Record getDocumentById(String id, User user);

	public List<Record> getRecordsById(String collection, List<String> ids);

	public void validateRecordInTransaction(Record record, Transaction transaction)
			throws ValidationException;

	public void validateRecord(Record record)
			throws RecordServicesException.ValidationException;

	public Record newRecordWithSchema(MetadataSchema schema, String id);

	public Record newRecordWithSchema(MetadataSchema schema);

	public void refresh(Record... records);

	public void refresh(RecordWrapper... recordWrappers);

	public void refresh(List<?> records);

	public List<String> getRecordTitles(String collection, List<String> recordIds);

	public List<BatchProcess> updateAsync(Record record)
			throws RecordServicesException;

	public List<BatchProcess> updateAsync(Record record, RecordUpdateOptions options)
			throws RecordServicesException;

	public List<ModificationImpact> calculateImpactOfModification(Transaction transaction, TaxonomiesManager taxonomiesManager,
			SearchServices searchServices, MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction);

	public boolean isRestorable(Record record, User user);

	public void restore(Record record, User user);

	public boolean isPhysicallyDeletable(Record record, User user);

	public void physicallyDelete(Record record, User user);

	public boolean isLogicallyDeletable(Record record, User user);

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user);

	public boolean isPrincipalConceptLogicallyDeletableExcludingContent(Record record, User user);

	public boolean isPrincipalConceptLogicallyDeletableIncludingContent(Record record, User user);

	public void logicallyDelete(Record record, User user);

	public void logicallyDeletePrincipalConceptIncludingRecords(Record record, User user);

	public void logicallyDeletePrincipalConceptExcludingRecords(Record record, User user);

	public List<Record> getVisibleRecordsWithReferenceTo(Record record, User user);

	public boolean isReferencedByOtherRecords(Record record);

	public void flush();

	public void removeOldLocks();

	public void recalculate(RecordWrapper recordWrapper);

	public void recalculate(Record record);
}
