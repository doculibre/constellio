package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.Holder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.junit.Test;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsThirdSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsZeSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayRecordDTOUtilsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	@Test
	public void whenStoringSingleValueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata()
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());
		//... todo add other types

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), false)
//				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "asd");
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId());

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.referenceMetadata(), asList(record1.getId(), record2.getId()))
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record2.getId());

		recordServices.execute(new Transaction(record1, record2, record3));

		Holder<MetadataSchema> schemaHolder = new Holder<>(setup.zeDefaultSchema());

		ByteArrayRecordDTO dto1 = new ByteArrayRecordDTO(schemaHolder, record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = new ByteArrayRecordDTO(schemaHolder, record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = new ByteArrayRecordDTO(schemaHolder, record3.getRecordDTO());

		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto1.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(null);
		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record1.getId());
//		assertThat(dto2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo("asd");
		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto3.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList(record1.getId(), record2.getId()));
		assertThat(dto3.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record2.getId());
	}
}
