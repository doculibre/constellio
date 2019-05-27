package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.CalculatorUsingM2;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyNumberCalculator2;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyReferenceNumberCalculator;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.*;
import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MetadataSchemaTypesBuilderTest extends ConstellioTest {

	DataStoreTypesFactory typesFactory;

	MetadataSchemaTypeBuilder zeType;
	MetadataSchemaBuilder zeTypeDefaultSchema;
	MetadataSchemaBuilder zeTypeCustomSchema;
	MetadataSchemaTypeBuilder anotherType;
	MetadataSchemaBuilder anotherTypeDefaultSchema;
	MetadataSchemaBuilder anotherTypeCustomSchema;
	MetadataSchemaTypeBuilder aThirdType;
	MetadataSchemaBuilder aThirdTypeDefaultSchema;
	MetadataSchemaBuilder aThirdTypeCustomSchema;
	MetadataSchemaTypesBuilder typesBuilder;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock ModelLayerFactory modelLayerFactory;

	@Before
	public void setUp() {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		typesFactory = new FakeDataStoreTypeFactory();

		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, "zeUltimateCollection", "fr", Arrays.asList("fr"));
		typesBuilder = getMetadataSchemaTypesBuilder(zeCollectionInfo);

		zeType = typesBuilder.createNewSchemaType("zeType");
		zeTypeDefaultSchema = zeType.getDefaultSchema();
		zeTypeCustomSchema = zeType.createCustomSchema("custom");

		anotherType = typesBuilder.createNewSchemaType("anotherType");
		anotherTypeDefaultSchema = anotherType.getDefaultSchema();
		anotherTypeCustomSchema = anotherType.createCustomSchema("custom");

		aThirdType = typesBuilder.createNewSchemaType("aThirdType");
		aThirdTypeDefaultSchema = aThirdType.getDefaultSchema();
		aThirdTypeCustomSchema = aThirdType.createCustomSchema("custom");
	}

	@NotNull
	private MetadataSchemaTypesBuilder getMetadataSchemaTypesBuilder(CollectionInfo zeCollectionInfo) {
		return MetadataSchemaTypesBuilder.createWithVersion(zeCollectionInfo, 0, new DefaultClassProvider(),
				Arrays.asList(Language.French));
	}

	@Test
	public void whenBuildingThenSetCollection()
			throws Exception {

		assertThat(typesBuilder.build(typesFactory, modelLayerFactory).getCollection()).isEqualTo("zeUltimateCollection");

	}

	@Test
	public void whenModifyThenSetCollection()
			throws Exception {

		MetadataSchemaTypes types = typesBuilder.build(typesFactory, modelLayerFactory);
		assertThat(MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider()).getCollection())
				.isEqualTo("zeUltimateCollection");

	}

	@Test
	public void givenTwoCopiedMetadatasAndOneManualEntryMetadataWhenGetAllCopiedMetadatasThenTwoMetadatasAreReturned()
			throws Exception {
		givenZeCustomSchemaMetadata(MetadataValueType.STRING);
		givenCopiedMetadata();
		givenCopiedMetadata();

		Set<MetadataBuilder> copiedMetadatas = typesBuilder.getAllCopiedMetadatas();

		assertThat(copiedMetadatas).hasSize(2);
	}

	@Test
	public void givenTwoCalculatedMetadatasAndOneManualEntryMetadataWhenGetAllCalculatedMetadatasThenTwoMetadatasAreReturned()
			throws Exception {
		givenZeCustomSchemaMetadata(MetadataValueType.STRING);
		givenCalculatedMetadata("zeMetadataNumer1");
		givenCalculatedMetadata("zeMetadataNumer2");

		int numberOfMetadatas = 0;
		for (MetadataBuilder metadata : typesBuilder.getAllCalculatedMetadatas()) {
			if (metadata.getLocalCode().startsWith("zeMetadataNumer")) {
				numberOfMetadatas++;
			}
		}
		assertThat(numberOfMetadatas).isEqualTo(2);
	}

	@Test
	public void givenFourMetadatasPlusTwoAumtomaticCreatedMetadatasdWhenGetAllMetadatasThenSixMetadatasAreReturned()
			throws Exception {
		givenZeDefaultSchemaMetadata("zeMetadataNumer1", MetadataValueType.STRING);
		givenZeCustomSchemaMetadata("zeMetadataNumer2", MetadataValueType.STRING);
		givenAnotherDefaultSchemaMetadata("zeMetadataNumer3", MetadataValueType.STRING);
		givenAnotherCustomSchemaMetadata("zeMetadataNumer4", MetadataValueType.STRING);

		int numberOfMetadatas = 0;
		for (MetadataBuilder metadata : typesBuilder.getAllMetadatas()) {
			if (metadata.getLocalCode().startsWith("zeMetadataNumer")) {
				numberOfMetadatas++;
			}
		}

		assertThat(numberOfMetadatas).isEqualTo(6);
	}

	@Test
	public void givenNewSchemaTypeAndMetadata() throws Exception {
		MetadataSchemaTypeBuilder type1 = typesBuilder.createNewSchemaType("type1");
		modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		modelLayerFactory.getMetadataSchemasManager().getSchemaTypes("type1");

		MetadataSchemaTypeBuilder type2 = typesBuilder.createNewSchemaType("type2");
		modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		short id2 = typesBuilder.getSchemaType(type2.getCode()).getId();
	}

	// Copied metadata validation tests
	@Test
	public void givenSingleValueMetadataWithCopiedEntryBasedOnSingleValueReferenceAndSingleValueCopiedMetadataWhenBuildingThenOK()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	// Copied metadata validation tests
	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyACustomMetadata.class)
	public void givenSingleValueMetadataWithCopiedEntryBasedOnSingleValueReferenceInCustomSchemaAndSingleValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherCustomSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyMultiValueInSingleValueMetadata.class)
	public void givenSingleValueMetadataWithCopiedEntryBasedOnMultiValueReferenceAndSingleValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeCustomSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeCustomSchemaMetadata(REFERENCE).setMultivalue(true);
		MetadataBuilder anotherMetadata = givenAnotherCustomSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyMultiValueInSingleValueMetadata.class)
	public void givenSingleValueMetadataWithCopiedEntryBasedOnSingleValueReferenceAndMultiValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(true);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test
	public void givenMultiValueMetadataWithCopiedEntryBasedOnSingleValueReferenceAndMultiValueCopiedMetadataWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(true);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test
	public void givenMultiValueMetadataWithCopiedEntryBasedOnMultiValueReferenceAndMultiValueCopiedMetadataWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(true);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(true);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyMultiValueInSingleValueMetadata.class)
	public void givenSingleValueMetadataWithCopiedEntryBasedOnMultiValueReferenceAndMultiValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(true);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(true);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test
	public void givenMultiValueMetadataWithCopiedEntryBasedOnMultiValueReferenceAndSingleValueCopiedMetadataWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(true);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopySingleValueInMultiValueMetadata.class)
	public void givenMultiValueMetadataWithCopiedEntryBasedOnSingleValueReferenceAndSingleValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyADifferentTypeInMetadata.class)
	public void givenTypeTextMetadataWithCopiedEntryAndATypeDateValueCopiedMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(DATE_TIME).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenTypeTextMetadataWithCopiedEntryAndATypeTextValueCopiedMetadataWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	// Calculated metadata validation tests
	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCalculateDifferentValueTypeInValueMetadata.class)
	public void givenTextValueMetadataWithCalculatedEntryAndATextTypeLocalDependencyAndNumberValueCalculatedWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenTextValueMetadataWithCalculatedEntryAndATextTypeLocalDependencyAndTextValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculator.class);

		assertThat(metadataWithCalculatedEntry.getType()).isEqualTo(MetadataValueType.STRING);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	@Test
	public void givenTextValueMetadataWithCalculatedEntryAndANumberTypeLocalDependencyAndTextValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculatorUsingNumber.class);

		assertThat(metadataWithCalculatedEntry.getType()).isEqualTo(MetadataValueType.STRING);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCalculateDifferentValueTypeInValueMetadata.class)
	public void givenNumberValueMetadataWithCalculatedEntryAndATextTypeLocalDependencyAndTextValueCalculatedWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenNumberValueMetadataWithCalculatedEntryAndTwoNumberTypeLocalDependenciesAndNumberValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependency1", NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependency2", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyNumberCalculator2.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CalculatorDependencyHasInvalidValueType.class)
	public void givenNumberValueMetadataWithCalculatedEntryAndANumberAndATextTypeLocalDependenciesAndNumberValueCalculatedWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependency1", NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependency2", STRING).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyNumberCalculator2.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenNumberValueMetadataWithCalculatedEntryWithAReferenceToANumberTypeMetadataAndNumberValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		MetadataBuilder dependencyRef = givenZeDefaultSchemaMetadata("dependencyRef", REFERENCE).setMultivalue(false);
		givenAnotherDefaultSchemaMetadata("dependencyMeta", NUMBER).setMultivalue(false);
		dependencyRef.defineReferences().add(anotherTypeDefaultSchema);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotUseACustomMetadataForCalculation.class)
	public void givenNumberValueMetadataWithCalculatedEntryWithAReferenceToANumberTypeCustomMetadataAndNumberValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		MetadataBuilder dependencyRef = givenZeDefaultSchemaMetadata("dependencyRef", REFERENCE).setMultivalue(false);
		givenAnotherCustomSchemaMetadata("dependencyMeta", NUMBER).setMultivalue(false);
		dependencyRef.defineReferences().add(anotherTypeCustomSchema);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CalculatorDependencyHasInvalidValueType.class)
	public void givenNumberValueMetadataWithCalculatedEntryWithAReferenceToANotNumberTypeMetadataAndNumberValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		MetadataBuilder dependencyRef = givenZeDefaultSchemaMetadata("dependencyRef", REFERENCE).setMultivalue(false);
		givenAnotherDefaultSchemaMetadata("dependencyMeta", STRING).setMultivalue(false);
		dependencyRef.defineReferences().add(anotherTypeDefaultSchema);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.NoAllowedReferences.class)
	public void givenMetadataWithCalculatedEntryWithoutAllowedReferencesWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependencyRef", REFERENCE).setMultivalue(false);
		givenAnotherDefaultSchemaMetadata("dependencyMeta", STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.NoAllowedReferences.class)
	public void givenMetadataWithCalculatedEntryWithoutAReferenceTypeMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		givenZeDefaultSchemaMetadata("dependencyRef", STRING).setMultivalue(false);
		givenAnotherDefaultSchemaMetadata("dependencyMeta", STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.InvalidDependencyMetadata.class)
	public void givenMetadataWithCalculatedEntryWithAReferenceToAnInexistentMetadataWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(NUMBER).setMultivalue(false);
		MetadataBuilder dependencyRef = givenZeDefaultSchemaMetadata("dependencyRef", REFERENCE).setMultivalue(false);
		dependencyRef.defineReferences().add(anotherTypeDefaultSchema);
		givenZeDefaultSchemaMetadata("localDependencyMeta", NUMBER).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(DummyReferenceNumberCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}


	@Test
	public void givenMultiValueMetadataWithCalculatedEntryAndAMultivalueLocalDependencyAndListCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(true);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextListCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenMultiValueMetadataWithCalculatedEntryAndASingleValueLocalDependencyAndListCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(false);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextListCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCalculateASingleValueInAMultiValueMetadata.class)
	public void givenMultiValueMetadataWithCalculatedEntryAndAMultiValueLocalDependencyAndSingleValueCalculatedWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(true);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(true);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenSingleValueMetadataWithCalculatedEntryAndAMultiValueLocalDependencyAndSingleValueCalculatedWhenBuildingThenOk()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(true);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCalculateAMultiValueInASingleValueMetadata.class)
	public void givenSingleValueMetadataWithCalculatedEntryAndAMultiValueLocalDependencyAndMultiValueCalculatedWhenBuildingThenException()
			throws Exception {

		MetadataBuilder metadataWithCalculatedEntry = givenZeDefaultSchemaMetadata(STRING).setMultivalue(false);
		givenZeDefaultSchemaMetadata("other", STRING).setMultivalue(true);
		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextListCalculator.class);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenThatTwoSchemasHasMutualDependenciesWithReferencesNotUsedByAutomaticMetadataThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder anotherSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata.defineReferences().set(anotherType);
		anotherSchemaMetadata.defineReferences().set(zeType);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CyclicDependenciesInSchemas.class)
	public void givenThatTwoSchemasHasMutualDependenciesWithReferencesUsedByAutomaticMetadataThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder anotherSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE)
				.markAsDependencyOfAutomaticMetadata();

		zeSchemaMetadata.defineReferences().set(anotherType);
		anotherSchemaMetadata.defineReferences().set(zeType);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenThatThreeSchemasHasMutualDependenciesToTypesWithReferencesNotUsedByAutomaticMetadataThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder secondSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata.defineReferences().set(anotherType);
		secondSchemaMetadata.defineReferences().set(aThirdType);
		thirdSchemaMetadata.defineReferences().set(zeType);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CyclicDependenciesInSchemas.class)
	public void givenThatThreeSchemasHasMutualDependenciesToTypesWithReferencesUsedByAutomaticMetadataThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder secondSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE)
				.markAsDependencyOfAutomaticMetadata();

		zeSchemaMetadata.defineReferences().set(anotherType);
		secondSchemaMetadata.defineReferences().set(aThirdType);
		thirdSchemaMetadata.defineReferences().set(zeType);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenThatThreeSchemasHasMutualDependenciesToSchemasNotUsedByAutomaticMetadataWithReferencesThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder secondSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata.defineReferences().add(anotherTypeDefaultSchema);
		secondSchemaMetadata.defineReferences().add(aThirdTypeCustomSchema);
		thirdSchemaMetadata.defineReferences().add(zeTypeCustomSchema);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CyclicDependenciesInSchemas.class)
	public void givenThatThreeSchemasHasMutualDependenciesToSchemasUsedByAutomaticMetadataWithReferencesThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder secondSchemaMetadata = givenAnotherDefaultSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE)
				.markAsDependencyOfAutomaticMetadata();

		zeSchemaMetadata.defineReferences().add(anotherTypeDefaultSchema);
		secondSchemaMetadata.defineReferences().add(aThirdTypeCustomSchema);
		thirdSchemaMetadata.defineReferences().add(zeTypeCustomSchema);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void givenThatCustomSchemasHasMutualDependenciesToSchemasNotUsedByAutomaticMetadataWithReferencesThenCanBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeCustomSchemaMetadata(REFERENCE);
		MetadataBuilder secondSchemaMetadata = givenAnotherCustomSchemaMetadata(REFERENCE);
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata.defineReferences().add(anotherTypeDefaultSchema);
		secondSchemaMetadata.defineReferences().add(aThirdTypeCustomSchema);
		thirdSchemaMetadata.defineReferences().add(zeTypeCustomSchema);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CyclicDependenciesInSchemas.class)
	public void givenThatCustomSchemasHasMutualDependenciesToSchemasUsedByAutomaticMetadataWithReferencesThenCannotBuild()
			throws Exception {

		MetadataBuilder zeSchemaMetadata = givenZeCustomSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder secondSchemaMetadata = givenAnotherCustomSchemaMetadata(REFERENCE).markAsDependencyOfAutomaticMetadata();
		MetadataBuilder thirdSchemaMetadata = givenThirdTypeDefaultSchemaMetadata(REFERENCE)
				.markAsDependencyOfAutomaticMetadata();

		zeSchemaMetadata.defineReferences().add(anotherTypeDefaultSchema);
		secondSchemaMetadata.defineReferences().add(aThirdTypeCustomSchema);
		thirdSchemaMetadata.defineReferences().add(zeTypeCustomSchema);

		typesBuilder.build(typesFactory, modelLayerFactory);
	}

	@Test
	public void whenCalculatingDependenciesOfMetadatasReferencingDifferentTypesThenBothTypesReturned()
			throws Exception {
		MetadataBuilder zeSchemaMetadata1 = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder anotherSchemaMetadata1 = givenZeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata1.defineReferences().set(anotherType);
		anotherSchemaMetadata1.defineReferences().set(aThirdType);

		assertThat(typesBuilder.getSchemaDependenciesOf(zeType)).containsOnly(anotherType.getCode(), aThirdType.getCode());
	}

	@Test
	public void whenCalculatingDependenciesOfMetadatasReferencingSchemasOfDifferentTypesThenBothTypesReturned()
			throws Exception {
		MetadataBuilder zeSchemaMetadata1 = givenZeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata1.defineReferences().add(anotherTypeCustomSchema);

		assertThat(typesBuilder.getSchemaDependenciesOf(zeType)).containsOnly(anotherType.getCode());
	}

	@Test
	public void whenCalculatingDependenciesOfASchemaWithAReferenceToTwoDifferentTypeThenBothAreReturned()
			throws Exception {
		MetadataBuilder zeSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder zeOtherSchemaMetadata = givenZeDefaultSchemaMetadata(REFERENCE);

		zeSchemaMetadata.defineReferences().add(anotherTypeCustomSchema);
		zeOtherSchemaMetadata.defineReferences().set(aThirdType);

		assertThat(typesBuilder.getSchemaDependenciesOf(zeType)).containsOnly(anotherType.getCode(), aThirdType.getCode());
	}

	@Test
	public void whenBuildingSchemaThenOrderAutomaticMetadatasBasedOnTheirDependencies()
			throws Exception {

		MetadataBuilder anotherSchemaMetadata = givenAnotherDefaultSchemaMetadata(STRING);

		MetadataBuilder zeSchemaMetadataRef = givenZeDefaultSchemaMetadata(REFERENCE);
		zeSchemaMetadataRef.defineReferences().set(anotherType);
		givenZeDefaultSchemaMetadata("m1", STRING).defineDataEntry().asCalculated(CalculatorUsingM2.class);
		givenZeDefaultSchemaMetadata("m2", STRING).defineDataEntry().asCopied(zeSchemaMetadataRef, anotherSchemaMetadata);

		List<Metadata> metadatas = zeTypeDefaultSchema.buildDefault(typesFactory, typesBuilder.getSchemaType(zeTypeDefaultSchema.getTypeCode())
				, typesBuilder, modelLayerFactory).getAutomaticMetadatas();

		assertThat(metadatas).extracting("localCode")
				.isEqualTo(
						asList("allReferences", "allRemovedAuths", "attachedAncestors", "autocomplete",
								"m2", "path", "tokens", "m1", "pathParts", "principalpath",
								"tokensHierarchy"));

	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata.class)
	public void whenBuildingSchemaWithCyclicDependenciesThenThrowException()
			throws Exception {

		MetadataBuilder zeSchemaMetadataRef = givenZeDefaultSchemaMetadata(REFERENCE);
		givenZeDefaultSchemaMetadata("m1", STRING).defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.CalculatorUsingM2.class);
		givenZeDefaultSchemaMetadata("m2", STRING).defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.CalculatorUsingM1.class);

		zeSchemaMetadataRef.defineReferences().set(anotherType);

		zeTypeDefaultSchema.buildDefault(typesFactory, typesBuilder.getSchemaType(zeTypeDefaultSchema.getTypeCode())
				, typesBuilder, modelLayerFactory).getAutomaticMetadatas();
	}

	@Test
	public void whenCalculatingTypesDependencyMapThenCombineAllDependenciesOfAllTypes()
			throws Exception {
		MetadataBuilder zeSchemaMetadata1 = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder zeSchemaMetadata2 = givenZeCustomSchemaMetadata(REFERENCE);
		MetadataBuilder anotherSchemaMetadata1 = givenAnotherCustomSchemaMetadata(REFERENCE);

		zeSchemaMetadata1.defineReferences().add(anotherTypeCustomSchema);
		zeSchemaMetadata2.defineReferences().add(aThirdTypeDefaultSchema);
		anotherSchemaMetadata1.defineReferences().add(aThirdTypeDefaultSchema);

		Set<String> expectedZeTypeDependencies = asSet(anotherType.getCode(), aThirdType.getCode());
		Set<String> expectedAnotherTypeDependencies = asSet(aThirdType.getCode());

		assertThat(typesBuilder.getTypesDependencies()).hasSize(2).containsEntry(zeType.getCode(), expectedZeTypeDependencies)
				.containsEntry(anotherType.getCode(), expectedAnotherTypeDependencies);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.CannotCopyUsingACustomMetadata.class)
	public void givenMetadataWithCopiedEntryUsingAReferenceOnCustomSchemasThenException()
			throws Exception {

		MetadataBuilder metadataWithCopiedEntry = givenZeDefaultSchemaMetadata(STRING);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeDefaultSchemaMetadata(REFERENCE);
		MetadataBuilder anotherMetadata = givenAnotherDefaultSchemaMetadata(STRING);

		metadataWithReferenceToAnotherSchema.defineReferences().add(anotherTypeCustomSchema);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);

		typesBuilder.build(typesFactory, modelLayerFactory);

	}

	private void givenCopiedMetadata() {
		MetadataBuilder metadataWithCopiedEntry = givenZeCustomSchemaMetadata(STRING).setMultivalue(false);
		MetadataBuilder metadataWithReferenceToAnotherSchema = givenZeCustomSchemaMetadata(REFERENCE).setMultivalue(false);
		MetadataBuilder anotherMetadata = givenAnotherCustomSchemaMetadata(STRING).setMultivalue(false);

		metadataWithReferenceToAnotherSchema.defineReferences().set(anotherType);
		metadataWithCopiedEntry.defineDataEntry().asCopied(metadataWithReferenceToAnotherSchema, anotherMetadata);
	}

	private void givenCalculatedMetadata(String code) {
		MetadataBuilder metadataWithCalculatedEntry = givenZeCustomSchemaMetadata(code, STRING).setMultivalue(false);

		metadataWithCalculatedEntry.defineDataEntry().asCalculated(
				com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderTestCalculatorUtils.DummyTextCalculator.class);
	}

	private MetadataBuilder givenZeDefaultSchemaMetadata(String code, MetadataValueType type) {
		return zeTypeDefaultSchema.create(code).setType(type);
	}

	private MetadataBuilder givenZeDefaultSchemaMetadata(MetadataValueType type) {
		return zeTypeDefaultSchema.create(aString()).setType(type);
	}

	private MetadataBuilder givenZeCustomSchemaMetadata(MetadataValueType type) {
		return zeTypeCustomSchema.create(aString()).setType(type);
	}

	private MetadataBuilder givenZeCustomSchemaMetadata(String code, MetadataValueType type) {
		return zeTypeCustomSchema.create(code).setType(type);
	}

	private MetadataBuilder givenAnotherDefaultSchemaMetadata(String code, MetadataValueType type) {
		return anotherTypeDefaultSchema.create(code).setType(type);
	}

	private MetadataBuilder givenAnotherCustomSchemaMetadata(String code, MetadataValueType type) {
		return anotherTypeCustomSchema.create(code).setType(type);
	}

	private MetadataBuilder givenAnotherDefaultSchemaMetadata(MetadataValueType type) {
		return anotherTypeDefaultSchema.create(aString()).setType(type);
	}

	private MetadataBuilder givenAnotherCustomSchemaMetadata(MetadataValueType type) {
		return anotherTypeCustomSchema.create(aString()).setType(type);
	}

	private MetadataBuilder givenThirdTypeDefaultSchemaMetadata(MetadataValueType type) {
		return anotherTypeDefaultSchema.create(aString()).setType(type);
	}

	private MetadataBuilder givenThirdTypeCustomSchemaMetadata(MetadataValueType type) {
		return anotherTypeCustomSchema.create(aString()).setType(type);
	}
}
