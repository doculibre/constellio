package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
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
				MetadataBuilder zeRefText = zeType.getDefaultSchema().create("refText");
				MetadataBuilder pctRef = zeType.getDefaultSchema().create("pct").setType(NUMBER)
						.setIncreasedDependencyLevel(true).defineDataEntry().asJexlScript(
								"if (ref.copiedThirdSchemaTypeSum > 0) {number / ref.copiedThirdSchemaTypeSum} else {0}");

				MetadataBuilder anotherSchemaSum = anotherType.getDefaultSchema().create("sum")
						.defineDataEntry().asSum(zeRef, zeNumber);
				MetadataBuilder anotherSchemaSumX10 = anotherType.getDefaultSchema().create("sumX10").setType(NUMBER)
						.defineDataEntry().asJexlScript("sum * 10");
				MetadataBuilder copiedThirdSchemaTypeSum = anotherType.getDefaultSchema().create("copiedThirdSchemaTypeSum");
				MetadataBuilder anotherSchemaRef = anotherType.getDefaultSchema().create("ref").defineReferencesTo(thirdType);
				MetadataBuilder anotherSchemaText = anotherType.getDefaultSchema().create("text").setType(STRING);

				MetadataBuilder thirdSchemaSum = thirdType.getDefaultSchema().create("sum")
						.defineDataEntry().asSum(anotherSchemaRef, anotherSchemaSum);
				MetadataBuilder thirdSchemaSumX10 = thirdType.getDefaultSchema().create("sumX10")
						.defineDataEntry().asSum(anotherSchemaRef, anotherSchemaSumX10);

				copiedThirdSchemaTypeSum.setType(NUMBER).defineDataEntry().asCopied(anotherSchemaRef, thirdSchemaSum);
				zeRefText.setType(STRING).defineDataEntry().asCopied(zeRef, anotherSchemaText);

			}
		}));

	}

	@Test
	public void givenMetadatasCreatingCyclicDependenciesOverSchemaTypesThenDividedByLevels()
			throws Exception {

		//schemas.getTypes().get
		assertThat(getNetworkLinks()).containsOnly(
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sum", "anotherSchemaType_default_sum", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_ref", 1),
				tuple("aThirdSchemaType_default_sumX10", "anotherSchemaType_default_sumX10", 1),
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "aThirdSchemaType_default_sum", 1),
				tuple("anotherSchemaType_default_copiedThirdSchemaTypeSum", "anotherSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_ref", 1),
				tuple("anotherSchemaType_default_sum", "zeSchemaType_default_number", 1),
				tuple("anotherSchemaType_default_sumX10", "anotherSchemaType_default_sum", 1),
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_ref", 2),
				tuple("zeSchemaType_default_pct", "zeSchemaType_default_number", 2),
				tuple("zeSchemaType_default_pct", "anotherSchemaType_default_copiedThirdSchemaTypeSum", 2),
				tuple("zeSchemaType_default_refText", "anotherSchemaType_default_text", 0),
				tuple("zeSchemaType_default_refText", "zeSchemaType_default_ref", 0)
		);

	}

	private List<Tuple> getNetworkLinks() {

		List<Tuple> tuples = new ArrayList();

		for (MetadataNetworkLink link : schemas.getTypes().getMetadataNetwork().getLinks()) {

			if (!link.getToMetadata().isGlobal()
					&& !link.getFromMetadata().isGlobal()
					&& !link.getFromMetadata().getCode().startsWith("user_")
					&& !link.getFromMetadata().getCode().startsWith("user_")) {
				Tuple tuple = new Tuple();
				tuple.addData(link.getFromMetadata().getCode());
				tuple.addData(link.getToMetadata().getCode());
				tuple.addData(link.getLevel());
				tuples.add(tuple);
			}

		}

		return tuples;
	}

	@Test
	public void givenAgregatedSumMetadataWhenCreateInputRecordThenSum()
			throws Exception {

	}
}
