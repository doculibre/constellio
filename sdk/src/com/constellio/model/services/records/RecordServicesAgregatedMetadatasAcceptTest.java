package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedMetadatasAcceptTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();

	@Before
	public void setUp()
			throws Exception {

		defineSchemasManager().using(schemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeType = schemaTypes.getSchemaType(zeSchema.typeCode());
				MetadataSchemaTypeBuilder anotherType = schemaTypes.getSchemaType(anotherSchema.typeCode());
				MetadataSchemaTypeBuilder thirdType = schemaTypes.getSchemaType(thirdSchema.typeCode());

				MetadataBuilder zeNumber = zeType.getDefaultSchema().create("number").setType(NUMBER);
				MetadataBuilder zeRef = zeType.getDefaultSchema().create("ref").defineReferencesTo(anotherType);

				MetadataBuilder anotherSchemaSum = anotherType.getDefaultSchema()
						.create("sum").defineDataEntry().asSum(zeRef, zeNumber);
				anotherType.getDefaultSchema().create("sumX10").defineDataEntry().asJexlScript("sum * 10");
				MetadataBuilder anotherSchemaRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);

				MetadataBuilder thirdSchemaSum = anotherType.getDefaultSchema()
						.create("sum").defineDataEntry().asSum(anotherSchemaRef, anotherSchemaSum);
				thirdType.getDefaultSchema().create("zeSumX10").defineDataEntry().asJexlScript("zeSum * 10");
			}
		}));

	}

	@Test
	public void givenAgregatedSumMetadataWhenCreateInputRecordThenSum()
			throws Exception {

	}
}
