package com.constellio.app.ui.pages.management.schemas;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

import java.util.ArrayList;
import java.util.List;

public class SchemaViewAcceptTestSetup extends SchemasSetup {

	public static final String ZE_SCHEMA = "zeSchemaType";
	ZeSchema zeSchema = new ZeSchema();

	public SchemaViewAcceptTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaTypeBuilder zeSchemaType = typesBuilder.createNewSchemaTypeWithSecurity(ZE_SCHEMA);
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

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
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