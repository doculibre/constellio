package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class LocalCacheConfigsUtilsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup zeCollectionSchemas = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = zeCollectionSchemas.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = zeCollectionSchemas.new AnotherSchemaMetadatas();

	TestsSchemasSetup anotherCollectionSchemas = new TestsSchemasSetup("anotherCollection");
	TestsSchemasSetup.ZeSchemaMetadatas anotherCollectionZeSchema = zeCollectionSchemas.new ZeSchemaMetadatas();

	@Test
	public void givenNotYetConfiguredThenUseConfiguredMetadatas() throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers(),
				withCollection("anotherCollection").withAllTestUsers()
		);
		defineSchemasManager().using(zeCollectionSchemas.with((s) -> {
			s.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			s.getSchemaType(anotherSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			s.getSchemaType(zeSchema.typeCode()).createMetadata("stringMetadata").setUniqueValue(true).setType(STRING);
			s.getSchemaType(zeSchema.typeCode()).createMetadata("anotherStringMetadata").setType(STRING);
			s.getSchemaType(anotherSchema.typeCode()).createMetadata("stringMetadata").setUniqueValue(true).setType(STRING);
			s.getSchemaType(anotherSchema.typeCode()).createMetadata("anotherStringMetadata").setType(STRING);
		}));
		defineSchemasManager().using(anotherCollectionSchemas.with((s) -> {
			s.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			s.getSchemaType(anotherSchema.typeCode()).setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
			s.getSchemaType(zeSchema.typeCode()).createMetadata("stringMetadata").setUniqueValue(true).setType(STRING);
			s.getSchemaType(zeSchema.typeCode()).createMetadata("anotherStringMetadata").setType(STRING);
			s.getSchemaType(anotherSchema.typeCode()).createMetadata("stringMetadata").setUniqueValue(true).setType(STRING);
			s.getSchemaType(anotherSchema.typeCode()).createMetadata("anotherStringMetadata").setType(STRING);
		}));

		//LocalCacheConfigsServices services = new LocalCacheConfigsServices(getModelLayerFactory());
		//LocalCacheConfigs cacheConfigs = services.buildFull("");
	}
}
