package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator2;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Comparator;

import static com.constellio.sdk.tests.TestUtils.onlyElementsOfClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataSchemaBuilderTest extends ConstellioTest {

	@Mock MetadataSchemaTypesBuilder typesBuilder;

	@Mock DataStoreTypesFactory typesFactory;

	MetadataSchemaTypeBuilder metadataSchemaTypeBuilder;

	MetadataSchemaBuilder defaultSchemaBuilder;
	MetadataSchemaBuilder customSchemaBuilder;

	MetadataSchema defaultSchema;
	MetadataSchema customSchema;

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock TaxonomiesManager taxonomiesManager;

	@Mock Comparator<Metadata> metadataComparator;

	@Before
	public void setUp() {
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		when(typesBuilder.getLanguages()).thenReturn(Arrays.asList(Language.French));
		when(typesBuilder.getClassProvider()).thenReturn(new DefaultClassProvider());
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, "zeUltimateCollection", "fr", Arrays.asList("fr"));
		metadataSchemaTypeBuilder = MetadataSchemaTypeBuilder
				.createNewSchemaType(zeCollectionInfo, "aSchemaType", typesBuilder);
		defaultSchemaBuilder = spy(metadataSchemaTypeBuilder.getDefaultSchema());
		customSchemaBuilder = metadataSchemaTypeBuilder.createCustomSchema("custom");
	}

	@Test
	public void whenNewSchemaCreatedThenHasIdMetadata()
			throws Exception {
		MetadataBuilder metadataBuilder = defaultSchemaBuilder.get("id");
		assertThat(metadataBuilder.isUndeletable()).isTrue();
		assertThat(metadataBuilder.isSystemReserved()).isTrue();
		assertThat(metadataBuilder.isUnmodifiable()).isTrue();
		assertThat(metadataBuilder.isMultivalue()).isFalse();
		assertThat(metadataBuilder.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(metadataBuilder.getLocalCode()).isEqualTo("id");
		assertThat(metadataBuilder.getCode()).isEqualTo("aSchemaType_default_id");
		assertThat(metadataBuilder.getEnabled()).isTrue();
		assertThat(metadataBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void whenNewSchemaCreatedThenHasSchemaMetadata()
			throws Exception {
		MetadataBuilder metadataBuilder = defaultSchemaBuilder.get("schema");
		assertThat(metadataBuilder.isUndeletable()).isTrue();
		assertThat(metadataBuilder.isSystemReserved()).isTrue();
		assertThat(metadataBuilder.isUnmodifiable()).isFalse();
		assertThat(metadataBuilder.isMultivalue()).isFalse();
		assertThat(metadataBuilder.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(metadataBuilder.getLocalCode()).isEqualTo("schema");
		assertThat(metadataBuilder.getCode()).isEqualTo("aSchemaType_default_schema");
		assertThat(metadataBuilder.getEnabled()).isTrue();
		assertThat(metadataBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void whenNewSchemaCreatedThenHasPreviousSystemIdentifierMetadata()
			throws Exception {
		MetadataBuilder metadataBuilder = defaultSchemaBuilder.get("legacyIdentifier");
		assertThat(metadataBuilder.isUndeletable()).isTrue();
		assertThat(metadataBuilder.isSystemReserved()).isTrue();
		assertThat(metadataBuilder.isUnmodifiable()).isTrue();
		assertThat(metadataBuilder.isMultivalue()).isFalse();
		assertThat(metadataBuilder.getType()).isSameAs(MetadataValueType.STRING);
		assertThat(metadataBuilder.getLocalCode()).isEqualTo("legacyIdentifier");
		assertThat(metadataBuilder.getCode()).isEqualTo("aSchemaType_default_legacyIdentifier");
		assertThat(metadataBuilder.getEnabled()).isTrue();
		assertThat(metadataBuilder.getDefaultRequirement()).isTrue();
	}

	@Test
	public void givenCodeOfDefaultSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(defaultSchema.getLocalCode()).isEqualTo("default");
	}

	@Test
	public void givenCodeOfDefaultSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(defaultSchemaBuilder.getLocalCode()).isEqualTo("default");
	}

	@Test
	public void givenCollectionOfDefaultSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(defaultSchema.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCollectionOfDefaultSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(defaultSchemaBuilder.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCompleteCodeOfDefaultSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(defaultSchema.getCode()).isEqualTo("aSchemaType_default");
	}

	@Test
	public void givenCompleteCodeOfDefaultSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(defaultSchemaBuilder.getCode()).isEqualTo("aSchemaType_default");
	}

	@Test
	public void givenCodeOfCustomSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(customSchema.getLocalCode()).isEqualTo("custom");
	}

	@Test
	public void givenCodeOfCustomSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(customSchemaBuilder.getLocalCode()).isEqualTo("custom");
	}

	@Test
	public void givenCollectionOfCustomSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(customSchema.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCollectionOfCustomSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(customSchemaBuilder.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCompleteCodeOfCustomSchemaWhenBuildingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(customSchema.getCode()).isEqualTo("aSchemaType_custom");
	}

	@Test
	public void givenCompleteCodeOfCustomSchemaWhenModifyingThenSetToCorrectValue()
			throws Exception {
		build();

		assertThat(customSchemaBuilder.getCode()).isEqualTo("aSchemaType_custom");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsNullAndLabelOfSchemaTypeIsDefinedWhenBuildingThenSetToSchemaTypeLabel() {
		defaultSchemaBuilder.addLabel(Language.French, null);
		metadataSchemaTypeBuilder.addLabel(Language.French, "Type");

		build();

		assertThat(defaultSchema.getLabel(Language.French)).isEqualTo("Type");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsNullAndLabelOfSchemaTypeIsNullWhenBuildingThenSetToSchemaTypeCode() {
		defaultSchemaBuilder.addLabel(Language.French, null);

		build();

		assertThat(defaultSchema.getLabel(Language.French)).isEqualTo("aSchemaType");
	}

	@Test
	public void givenLabelOfCustomSchemaIsNullWhenBuildingThenSetToSchemaCode() {
		customSchemaBuilder.addLabel(Language.French, null);

		build();

		assertThat(customSchema.getLabel(Language.French)).isEqualTo("custom");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsDefinedWhenBuildingThenSetToDefinedValue() {
		defaultSchemaBuilder.addLabel(Language.French, "aName");

		build();

		assertThat(defaultSchema.getLabel(Language.French)).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsDefinedWhenModifyingThenSetToDefinedValue() {
		defaultSchemaBuilder.addLabel(Language.French, "aName");

		buildAndModify();

		assertThat(defaultSchemaBuilder.getLabel(Language.French)).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfCustomSchemaIsDefinedWhenBuildingThenSetToDefinedValue() {
		defaultSchemaBuilder.addLabel(Language.French, "zeDefaultSchemaName");
		customSchemaBuilder.addLabel(Language.French, "aName");

		build();

		assertThat(customSchema.getLabel(Language.French)).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfCustomSchemaIsDefinedWhenModifyingThenSetToDefinedValue() {
		defaultSchemaBuilder.addLabel(Language.French, "zeDefaultSchemaName");
		customSchemaBuilder.addLabel(Language.French, "aName");

		buildAndModify();

		assertThat(customSchemaBuilder.getLabel(Language.French)).isEqualTo("aName");
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.InvalidAttribute.class)
	public void whenCreatingCustomSchemaWithNullCodeThenException() {
		customSchemaBuilder.create(null);
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.InvalidAttribute.class)
	public void whenCreatingCustomSchemaWithEmptyCodeThenException() {
		customSchemaBuilder.create("");
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaUndefinedWhenBuildingThenDeletable()
			throws Exception {
		build();

		assertThat(customSchema.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaUndefinedWhenModifyingThenDeletable()
			throws Exception {
		buildAndModify();

		assertThat(customSchemaBuilder.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaSetToTrueWhenBuildingThenUndeletable()
			throws Exception {
		customSchemaBuilder.setUndeletable(true);

		build();

		assertThat(customSchema.isUndeletable()).isTrue();
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaSetToTrueWhenModifyingThenUndeletable()
			throws Exception {
		customSchemaBuilder.setUndeletable(true);

		buildAndModify();

		assertThat(customSchemaBuilder.isUndeletable()).isTrue();
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaSetToFalseWhenBuildingThenDeletable()
			throws Exception {
		customSchemaBuilder.setUndeletable(false);

		build();

		assertThat(customSchema.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusOfCustomSchemaSetToFalseWhenModifyingThenDeletable()
			throws Exception {
		customSchemaBuilder.setUndeletable(false);

		buildAndModify();

		assertThat(customSchemaBuilder.isUndeletable()).isFalse();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenRecordValidatorsOfDefaultMetadataWhenBuildingThenHasDefinedValidators()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class).add(TestRecordValidator2.class);

		build();

		assertThat(defaultSchema.getValidators()).has(
				onlyElementsOfClass(TestRecordValidator1.class, TestRecordValidator2.class)).hasSize(2);
	}

	@Test
	public void givenRecordValidatorsOfDefaultMetadataWhenModifyingThenHasDefinedValidators()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class).add(TestRecordValidator2.class);

		buildAndModify();

		assertThat(defaultSchemaBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordValidator1.class.getName(), TestRecordValidator2.class.getName()).hasSize(2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenRecordValidatorsOfCustomMetadataWhenBuildingThenHasDefinedValidatorsAndDefaultValidators()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class);
		customSchemaBuilder.defineValidators().add(TestRecordValidator2.class);

		build();

		assertThat(defaultSchema.getValidators()).has(onlyElementsOfClass(TestRecordValidator1.class)).hasSize(1);
		assertThat(customSchema.getValidators())
				.has(onlyElementsOfClass(TestRecordValidator1.class, TestRecordValidator2.class)).hasSize(2);
	}

	@Test
	public void givenRecordValidatorsOfCustomMetadataWhenModifyingThenHasOnlyCustomValidators()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class);
		customSchemaBuilder.defineValidators().add(TestRecordValidator2.class);

		buildAndModify();

		assertThat(defaultSchemaBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordValidator1.class.getName()).hasSize(1);
		assertThat(customSchemaBuilder.defineValidators().implementationsClassname).containsOnly(
				TestRecordValidator2.class.getName()).hasSize(1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenRecordValidatorsOfCustomMetadataHasSameValidatorsThanTheDefaultSchemaWhenBuildingThenNoDuplication()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class);
		customSchemaBuilder.defineValidators().add(TestRecordValidator1.class);

		build();

		assertThat(customSchema.getValidators()).has(onlyElementsOfClass(TestRecordValidator1.class)).hasSize(1);
	}

	@Test
	public void givenRecordValidatorsOfCustomMetadataHasSameValidatorsThanTheDefaultSchemaWhenModifyingThenDuplicatedValidatorRemovedInCustom()
			throws Exception {
		defaultSchemaBuilder.defineValidators().add(TestRecordValidator1.class);
		customSchemaBuilder.defineValidators().add(TestRecordValidator1.class);

		buildAndModify();

		assertThat(customSchemaBuilder.defineValidators().implementationsClassname).isEmpty();
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.InvalidAttribute.class)
	public void givenInvalidCodeFormatWhenValidateThenThrowExcpetion()
			throws Exception {
		customSchemaBuilder.validateLocalCode("te_st");
	}

	@Test
	public void givenValidCodeFormatWhenValidateThenItIsValid()
			throws Exception {
		customSchemaBuilder.validateLocalCode("test");
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.InvalidAttribute.class)
	public void givenInvalidMetadataCodeFormatWhenValidateThenThrowExcpetion()
			throws Exception {
		customSchemaBuilder.validateLocalCode("tes_t");
	}

	private Metadata newMockedMetadataWithCode(String code) {
		Metadata secondMetadata = mock(Metadata.class);
		when(secondMetadata.getLocalCode()).thenReturn(code);
		return secondMetadata;
	}

	@Test
	public void whenAddingCustomSchemaThenInheritModifiedDefaultSchemaFlags()
			throws Exception {
		buildAndModify();
		defaultSchemaBuilder.create("zeMetadata").setMultivalue(true).setUndeletable(true).setEnabled(false)
				.setSystemReserved(true).setDefaultRequirement(true).addLabel(Language.French, "zeLabel")
				.setType(MetadataValueType.BOOLEAN)
				.setUnmodifiable(true);

		MetadataSchemaBuilder builder = MetadataSchemaBuilder.createSchema(defaultSchemaBuilder, "zeCustom2", true);
		MetadataBuilder metadataBuilder = builder.getMetadata("zeMetadata");
		assertThat(metadataBuilder.isMultivalue()).isTrue();
		assertThat(metadataBuilder.isUndeletable()).isTrue();
		assertThat(metadataBuilder.isSystemReserved()).isTrue();
		assertThat(metadataBuilder.isUnmodifiable()).isTrue();

	}

	@Test
	public void whenAddingCustomSchemaThenInheritModifiedDefaultSchemaUniqueFlags()
			throws Exception {
		buildAndModify();
		defaultSchemaBuilder.create("zeMetadata").setUniqueValue(true);

		MetadataSchemaBuilder builder = MetadataSchemaBuilder.createSchema(defaultSchemaBuilder, "zeCustom2", true);
		MetadataBuilder metadataBuilder = builder.getMetadata("zeMetadata");
		assertThat(metadataBuilder.isUniqueValue()).isTrue();

	}

	private void build() {
		defaultSchema = defaultSchemaBuilder.buildDefault(typesFactory, metadataSchemaTypeBuilder, typesBuilder, (short) 42, modelLayerFactory);
		customSchema = customSchemaBuilder.buildCustom(defaultSchema, metadataSchemaTypeBuilder, typesBuilder, (short) 42, typesFactory, modelLayerFactory);
	}

	private void buildAndModify() {
		MetadataSchema defaultSchema = defaultSchemaBuilder.buildDefault(typesFactory, metadataSchemaTypeBuilder, typesBuilder, (short) 42, modelLayerFactory);
		MetadataSchema customSchema = customSchemaBuilder.buildCustom(defaultSchema, metadataSchemaTypeBuilder, typesBuilder, (short) 42, typesFactory, modelLayerFactory);
		defaultSchemaBuilder = MetadataSchemaBuilder.modifyDefaultSchema(defaultSchema, metadataSchemaTypeBuilder);
		customSchemaBuilder = MetadataSchemaBuilder.modifySchema(customSchema, metadataSchemaTypeBuilder);
	}
}
