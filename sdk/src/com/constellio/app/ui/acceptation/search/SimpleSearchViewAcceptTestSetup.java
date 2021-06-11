package com.constellio.app.ui.acceptation.search;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class SimpleSearchViewAcceptTestSetup extends SchemasSetup {
	public final static int DEFAULT_NUM_RECORDS = 100;
	public static final String DOCUMENT = "fakeDocument";

	public static enum ZEnum implements EnumWithSmallCode {
		ALPHA("A"), BETA("B"), GAMMA("C");
		private String code;

		private ZEnum(String code) {
			this.code = code;
		}

		@Override
		public String getCode() {
			return code;
		}
	}

	public SimpleSearchViewAcceptTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaTypeBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(DOCUMENT);
		builder.getDefaultSchema().create("bodyText").addLabel(Language.French, "Body text").setType(MetadataValueType.TEXT)
				.setSearchable(true);
		builder.getDefaultSchema().create("number").addLabel(Language.French, "Number").setType(MetadataValueType.NUMBER)
				.setSearchable(true);
		builder.getDefaultSchema().create("someFacet").addLabel(Language.French, "Some facet").setType(MetadataValueType.NUMBER);
		builder.getDefaultSchema().create("anotherFacet").addLabel(Language.French, "Another facet")
				.setType(MetadataValueType.STRING);
		builder.getDefaultSchema().create("date").setType(MetadataValueType.DATE);
		builder.getDefaultSchema().create("zenum").setType(MetadataValueType.ENUM).defineAsEnum(ZEnum.class);
	}

	public List<Record> givenRecords(RecordServices recordServices) {
		Transaction transaction = new Transaction();
		List<Record> records = new ArrayList<>();

		for (int i = 0; i < DEFAULT_NUM_RECORDS; i++) {
			records.add(createDocument(i));
		}

		transaction.addUpdate(records);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return records;
	}

	public class Document implements SchemaShortcuts {
		@Override
		public String code() {
			return DOCUMENT + "_default";
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

		public Metadata text() {
			return getMetadata(code() + "_bodyText");
		}

		public Metadata number() {
			return getMetadata(code() + "_number");
		}

		public Metadata someFacet() {
			return getMetadata(code() + "_someFacet");
		}

		public Metadata anotherFacet() {
			return getMetadata(code() + "_anotherFacet");
		}

		public Metadata date() {
			return getMetadata(code() + "_date");
		}

		public Metadata zenum() {
			return getMetadata(code() + "_zenum");
		}
	}

	private Record createDocument(int id) {
		Document document = new Document();
		Record record = new TestRecord(document, "DOC" + id);
		record.set(document.title(), "Document " + id);
		record.set(document.text(), "This is some amazing text for document number " + id);
		record.set(document.number(), (double) id);
		record.set(document.someFacet(), (double) (id % 10));
		record.set(document.anotherFacet(), String.valueOf(id % 10));
		record.set(document.date(), new LocalDate());
		record.set(document.zenum(), ZEnum.ALPHA);
		return record;
	}
}
