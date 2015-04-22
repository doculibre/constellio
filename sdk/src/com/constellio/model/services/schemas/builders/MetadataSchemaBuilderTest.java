/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.schemas.builders;

import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.onlyElementsOfClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator2;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtilsRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataSchemaBuilderTest extends ConstellioTest {

	@Mock MetadataSchemaTypesBuilder typesBuilder;

	@Mock DataStoreTypesFactory typesFactory;

	MetadataSchemaTypeBuilder metadataSchemaTypeBuilder;

	MetadataSchemaBuilder defaultSchemaBuilder;
	MetadataSchemaBuilder customSchemaBuilder;

	MetadataSchema defaultSchema;
	MetadataSchema customSchema;

	@Mock TaxonomiesManager taxonomiesManager;

	@Mock Comparator<Metadata> metadataComparator;

	@Before
	public void setUp() {
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		metadataSchemaTypeBuilder = MetadataSchemaTypeBuilder
				.createNewSchemaType("zeUltimateCollection", "aSchemaType", typesBuilder);
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
		defaultSchemaBuilder.setLabel(null);
		metadataSchemaTypeBuilder.setLabel("Type");

		build();

		assertThat(defaultSchema.getLabel()).isEqualTo("Type");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsNullAndLabelOfSchemaTypeIsNullWhenBuildingThenSetToSchemaTypeCode() {
		defaultSchemaBuilder.setLabel(null);

		build();

		assertThat(defaultSchema.getLabel()).isEqualTo("aSchemaType");
	}

	@Test
	public void givenLabelOfCustomSchemaIsNullWhenBuildingThenSetToSchemaCode() {
		customSchemaBuilder.setLabel(null);

		build();

		assertThat(customSchema.getLabel()).isEqualTo("custom");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsDefinedWhenBuildingThenSetToDefinedValue() {
		defaultSchemaBuilder.setLabel("aName");

		build();

		assertThat(defaultSchema.getLabel()).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfDefaultSchemaIsDefinedWhenModifyingThenSetToDefinedValue() {
		defaultSchemaBuilder.setLabel("aName");

		buildAndModify();

		assertThat(defaultSchemaBuilder.getLabel()).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfCustomSchemaIsDefinedWhenBuildingThenSetToDefinedValue() {
		defaultSchemaBuilder.setLabel("zeDefaultSchemaName");
		customSchemaBuilder.setLabel("aName");

		build();

		assertThat(customSchema.getLabel()).isEqualTo("aName");
	}

	@Test
	public void givenLabelOfCustomSchemaIsDefinedWhenModifyingThenSetToDefinedValue() {
		defaultSchemaBuilder.setLabel("zeDefaultSchemaName");
		customSchemaBuilder.setLabel("aName");

		buildAndModify();

		assertThat(customSchemaBuilder.getLabel()).isEqualTo("aName");
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

	@SuppressWarnings("unchecked")
	@Test
	public void whenBuildingThenCalculateAutomaticMetadataDependenciesAndOrderThemUsingDependencyUtils() {
		DependencyUtils<String> dependencyUtils = mock(DependencyUtils.class);
		SchemaUtils schemaUtils = mock(SchemaUtils.class);
		Map<String, Set<String>> dependencies = mock(Map.class);

		Metadata firstMetadata = newMockedMetadataWithCode("m1");
		Metadata secondMetadata = newMockedMetadataWithCode("m2");
		List<Metadata> metadatas = asList(firstMetadata, secondMetadata);

		doReturn(metadatas).when(defaultSchemaBuilder).buildMetadatas(typesFactory, taxonomiesManager);
		doReturn(dependencyUtils).when(defaultSchemaBuilder).newDependencyUtils();
		doReturn(schemaUtils).when(defaultSchemaBuilder).newSchemaUtils();

		when(schemaUtils.calculatedMetadataDependencies(metadatas)).thenReturn(dependencies);
		when(dependencyUtils.sortByDependency(dependencies, null))
				.thenReturn(asList("m2", "m1"));

		MetadataSchema schema = defaultSchemaBuilder.buildDefault(typesFactory, taxonomiesManager);

		assertThat(schema.getAutomaticMetadatas()).containsExactly(secondMetadata, firstMetadata);

	}

	private Metadata newMockedMetadataWithCode(String code) {
		Metadata secondMetadata = mock(Metadata.class);
		when(secondMetadata.getLocalCode()).thenReturn(code);
		return secondMetadata;
	}

	@SuppressWarnings("unchecked")
	@Test(expected = MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata.class)
	public void givenCyclicDependenciesWhenBuildingThenException() {
		Metadata firstMetadata = newMockedMetadataWithCode("m1");
		Metadata secondMetadata = newMockedMetadataWithCode("m2");

		Map<String, Set<String>> dependencies = mock(Map.class);
		List<Metadata> metadatas = asList(firstMetadata, secondMetadata);
		doReturn(metadatas).when(defaultSchemaBuilder).buildMetadatas(typesFactory, taxonomiesManager);

		SchemaUtils schemaUtils = mock(SchemaUtils.class);
		DependencyUtils<String> dependencyUtils = mock(DependencyUtils.class);
		doReturn(dependencyUtils).when(defaultSchemaBuilder).newDependencyUtils();
		doReturn(schemaUtils).when(defaultSchemaBuilder).newSchemaUtils();

		when(schemaUtils.calculatedMetadataDependencies(metadatas)).thenReturn(dependencies);
		when(dependencyUtils.sortByDependency(dependencies, null))
				.thenThrow(DependencyUtilsRuntimeException.CyclicDependency.class);

		defaultSchemaBuilder.buildDefault(typesFactory, taxonomiesManager);

	}

	@Test
	public void whenAddingCustomSchemaThenInheritModifiedDefaultSchemaFlags()
			throws Exception {
		buildAndModify();
		defaultSchemaBuilder.create("zeMetadata").setMultivalue(true).setUndeletable(true).setEnabled(false)
				.setSystemReserved(true).setDefaultRequirement(true).setLabel("zeLabel").setType(MetadataValueType.BOOLEAN)
				.setUnmodifiable(true);

		MetadataSchemaBuilder builder = MetadataSchemaBuilder.createSchema(defaultSchemaBuilder, "zeCustom2");
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

		MetadataSchemaBuilder builder = MetadataSchemaBuilder.createSchema(defaultSchemaBuilder, "zeCustom2");
		MetadataBuilder metadataBuilder = builder.getMetadata("zeMetadata");
		assertThat(metadataBuilder.isUniqueValue()).isTrue();

	}

	private void build() {
		defaultSchema = defaultSchemaBuilder.buildDefault(typesFactory, taxonomiesManager);
		customSchema = customSchemaBuilder.buildCustom(defaultSchema, typesFactory, taxonomiesManager);
	}

	private void buildAndModify() {
		MetadataSchema defaultSchema = defaultSchemaBuilder.buildDefault(typesFactory, taxonomiesManager);
		MetadataSchema customSchema = customSchemaBuilder.buildCustom(defaultSchema, typesFactory, taxonomiesManager);
		defaultSchemaBuilder = MetadataSchemaBuilder.modifyDefaultSchema(defaultSchema, metadataSchemaTypeBuilder);
		customSchemaBuilder = MetadataSchemaBuilder.modifySchema(customSchema, metadataSchemaTypeBuilder);
	}
}
