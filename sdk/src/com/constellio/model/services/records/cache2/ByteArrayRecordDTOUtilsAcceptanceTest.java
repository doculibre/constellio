package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.Holder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayRecordDTOUtilsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	@Test
	public void whenStoringSingleValueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup.withABooleanMetadata().withAStringMetadata());
		//... todo add other types

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), false);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), null);

		recordServices.execute(new Transaction(record1, record2, record3));

		Holder<MetadataSchema> schemaHolder = new Holder<>(setup.zeDefaultSchema());

		ByteArrayRecordDTO dto1 = new ByteArrayRecordDTO(schemaHolder, record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = new ByteArrayRecordDTO(schemaHolder, record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = new ByteArrayRecordDTO(schemaHolder, record3.getRecordDTO());

		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);

	}
}
