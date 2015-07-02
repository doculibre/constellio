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
package com.constellio.model.extensions.events.records;

import com.constellio.data.utils.Factory;
import com.constellio.data.utils.FactoryWithCache;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordLogicalDeletionValidationEvent implements RecordEvent {

	User user;

	Record record;

	Factory<Boolean> referenceCount;

	public RecordLogicalDeletionValidationEvent(Record record, User user, Factory<Boolean> referenceCount) {
		this.record = record;
		this.user = user;
		this.referenceCount = new FactoryWithCache<>(referenceCount);
	}

	public Record getRecord() {
		return record;
	}

	public User getUser() {
		return user;
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public boolean isRecordReferenced() {
		return referenceCount.get();
	}
}
