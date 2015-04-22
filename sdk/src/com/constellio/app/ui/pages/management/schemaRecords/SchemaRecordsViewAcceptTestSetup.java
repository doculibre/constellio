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
package com.constellio.app.ui.pages.management.schemaRecords;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class SchemaRecordsViewAcceptTestSetup extends SchemasSetup {

	public static final String ZE_SCHEMA = "zeSchema";
	ZeSchema zeSchema = new ZeSchema();

	public SchemaRecordsViewAcceptTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaTypeBuilder zeSchemaType = typesBuilder.createNewSchemaType(ZE_SCHEMA);
	}

	public class ZeSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get(ZE_SCHEMA);
		}

		@Override
		public String code() {
			return ZE_SCHEMA + "_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

	}

	public RecordsOfZeSchema givenSomeRecordsOfZeSchema(RecordServices recordServices) {
		try {
			return new RecordsOfZeSchema(recordServices);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public class RecordsOfZeSchema {

		public final Record record1;
		public final Record record2;
		public final Record record3;
		public final Record record4;

		List<Record> records = new ArrayList<>();
		private RecordServices recordServices;

		private RecordsOfZeSchema(RecordServices recordServices)
				throws RecordServicesException {
			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			records.add(record1 = createRecord(transaction, "record1"));
			records.add(record2 = createRecord(transaction, "record2"));
			records.add(record3 = createRecord(transaction, "record3"));
			records.add(record4 = createRecord(transaction, "record4"));

			recordServices.execute(transaction);
		}

		private Record createRecord(Transaction transaction, String id) {
			Record record = new TestRecord(zeSchema, id);
			record.set(zeSchema.title(), id);
			transaction.addUpdate(record);
			return record;
		}

	}

}