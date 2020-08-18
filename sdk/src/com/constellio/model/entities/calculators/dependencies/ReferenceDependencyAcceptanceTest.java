package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ReferenceDependencyAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	SearchServices searchServices;
	ReindexingServices reindexingServices;

	@Before
	public void Setup() {
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
	}

	@Test
	public void ToAnIntegerReturnsAValueOfTypeInteger() throws Exception {
		defineSchemasManager().using(
				schemas.withAReferenceFromAnotherSchemaToZeSchema()
						.withAnIntegerMetadata()
		);


		ZeSchemaMetadatas zeDefaultSchemaMetadatas = schemas.new ZeSchemaMetadatas();
		AnotherSchemaMetadatas anotherSchemaMetadatas = schemas.new AnotherSchemaMetadatas();

		String referenceCode = anotherSchemaMetadatas.referenceFromAnotherSchemaToZeSchema().getCode();
		String referenceIntegerCode = zeDefaultSchemaMetadatas.integerMetadata().getCode();

		schemasManager.modify(zeCollection, typesBuilder ->
				typesBuilder.getSchema(anotherSchemaMetadatas.code())
						.create("calculatedValue")
						.setType(MetadataValueType.INTEGER)
						.defineDataEntry().asCalculated(IntegerReferenceDependencyPassThrough.class)
		);

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema zeDefaultSchema = types.getSchema(zeDefaultSchemaMetadatas.code());
		MetadataSchema anotherSchema = types.getSchema(anotherSchemaMetadatas.code());

		Record defaultSchemaRecord = recordServices.newRecordWithSchema(zeDefaultSchema, "DefaultSchemaRecord").set(zeDefaultSchemaMetadatas.integerMetadata(), 50);
		Record anotherSchemaRecord = recordServices.newRecordWithSchema(anotherSchema, "AnotherSchemaRecord").set(types.getMetadata(referenceCode), defaultSchemaRecord);

		recordServices.execute(new Transaction(defaultSchemaRecord, anotherSchemaRecord));

		assertThat((Integer) recordServices.getDocumentById("AnotherSchemaRecord").get(anotherSchema.getMetadata("calculatedValue")))
				.isEqualTo(recordServices.getDocumentById("DefaultSchemaRecord").get(zeDefaultSchemaMetadatas.integerMetadata()));
	}

	public static class IntegerReferenceDependencyPassThrough implements MetadataValueCalculator<Integer> {

		private final ReferenceDependency<Integer> dependency = ReferenceDependency.toAnInteger("referenceFromAnotherSchemaToZeSchema", "integerMetadata").whichIsRequired();

		@Override
		public Integer calculate(CalculatorParameters parameters) {
			return parameters.get(dependency);
		}

		@Override
		public Integer getDefaultValue() {
			return null;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Collections.singletonList(dependency);
		}
	}
}
