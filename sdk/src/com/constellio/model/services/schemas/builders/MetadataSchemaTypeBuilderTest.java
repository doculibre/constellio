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

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataSchemaTypeBuilderTest extends ConstellioTest {

	static final String CODE_SCHEMA_TYPE = "codeSchemaType";
	static final MetadataValueType valueType = MetadataValueType.NUMBER;
	private static final String INEXISTENT_CODE = "inexistent_code";
	private static final String UNDERSCORE = "_";
	private static final String DEFAULT = "default";
	@Mock MetadataSchemaTypesBuilder typesBuilder;
	@Mock DataStoreTypesFactory typesFactory;

	String customSchemaCode = "zeCustomSchema";
	String metadataCode = "zeMetadata";
	String expectedCustomSchemaMetadataCompleteCode = CODE_SCHEMA_TYPE + UNDERSCORE + customSchemaCode + UNDERSCORE
			+ metadataCode;
	String expectedDefaultSchemaMetadataCompleteCode = CODE_SCHEMA_TYPE + UNDERSCORE + DEFAULT + UNDERSCORE + metadataCode;

	@Mock TaxonomiesManager taxonomiesManager;

	MetadataSchemaTypeBuilder schemaTypeBuilder;

	MetadataSchemaType schemaType;

	MetadataSchemaBuilder defaultSchemaBuilder;

	@Before
	public void setup() {
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		schemaTypeBuilder = MetadataSchemaTypeBuilder.createNewSchemaType("zeUltimateCollection", CODE_SCHEMA_TYPE, typesBuilder)
				.setLabel("aLabel");
	}

	@Test
	public void givenCodeWhenBuildingThenHasCorrectValue()
			throws Exception {
		build();

		assertThat(schemaType.getCode()).isEqualTo(CODE_SCHEMA_TYPE);
	}

	@Test
	public void givenCodeWhenModifyingThenHasCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCode()).isEqualTo(CODE_SCHEMA_TYPE);
	}

	@Test
	public void givenCollectionWhenBuildingThenHasCorrectValue()
			throws Exception {
		build();

		assertThat(schemaType.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenCollectionWhenModifyingThenHasCorrectValue()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCollection()).isEqualTo("zeUltimateCollection");
	}

	@Test
	public void givenLabelWhenBuildingThenHasCorrectValue()
			throws Exception {
		schemaTypeBuilder.setLabel("zeLabel");

		build();

		assertThat(schemaType.getLabel()).isEqualTo("zeLabel");
	}

	@Test
	public void givenLabelWhenModifyingThenHasCorrectValue()
			throws Exception {
		schemaTypeBuilder.setLabel("zeLabel");

		buildAndModify();

		assertThat(schemaTypeBuilder.getLabel()).isEqualTo("zeLabel");
	}

	@Test(expected = MetadataSchemaTypeBuilderRuntimeException.LabelNotDefined.class)
	public void givenLabelNotDefinedWhenBuildingThenException()
			throws Exception {
		schemaTypeBuilder.setLabel(null);

		build();
	}

	@Test(expected = MetadataSchemaTypeBuilderRuntimeException.LabelNotDefined.class)
	public void givenLabelEmptyWhenBuildingThenException()
			throws Exception {
		schemaTypeBuilder.setLabel("");

		build();
	}

	@Test
	public void givenUndeletableStatusNotDefinedWhenBuildingThenDeletable()
			throws Exception {
		build();

		assertThat(schemaType.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToFalseWhenBuildingThenDeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(false);

		build();

		assertThat(schemaType.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToFalseWhenModifyingThenDeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isUndeletable()).isFalse();
	}

	@Test
	public void givenUndeletableStatusDefinedToTrueWhenBuildingThenUndeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(true);

		build();

		assertThat(schemaType.isUndeletable()).isTrue();
	}

	@Test
	public void givenUndeletableStatusDefinedToTrueWhenModifyingThenUndeletable()
			throws Exception {
		schemaTypeBuilder.setUndeletable(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isUndeletable()).isTrue();
	}

	@Test
	public void givenSecurityStatusNotDefinedWhenBuildingThenSecurity()
			throws Exception {
		build();

		assertThat(schemaType.hasSecurity()).isTrue();
	}

	@Test
	public void givenSecurityStatusDefinedToFalseWhenBuildingThenNoSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(false);

		build();

		assertThat(schemaType.hasSecurity()).isFalse();
	}

	@Test
	public void givenSecurityStatusDefinedToFalseWhenModifyingThenNoSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(false);

		buildAndModify();

		assertThat(schemaTypeBuilder.isSecurity()).isFalse();
	}

	@Test
	public void givenSecurityStatusDefinedToTrueWhenBuildingThenSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(true);

		build();

		assertThat(schemaType.hasSecurity()).isTrue();
	}

	@Test
	public void givenSecurityStatusDefinedToTrueWhenModifyingThenSecurity()
			throws Exception {
		schemaTypeBuilder.setSecurity(true);

		buildAndModify();

		assertThat(schemaTypeBuilder.isSecurity()).isTrue();
	}

	@Test
	public void givenDefaultSchemaWithAMetadataWhenBuildingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		build();
		MetadataSchema schema = schemaType.getDefaultSchema();
		Metadata metadata = schema.getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedDefaultSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	@Test
	public void givenDefaultSchemaWithAMetadataWhenModifyingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		buildAndModify();
		MetadataBuilder metadata = schemaTypeBuilder.getDefaultSchema().getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedDefaultSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	public void givenNoCustomSchemasWhenModifyingThenCustomSchemasListIsEmpty()
			throws Exception {
		buildAndModify();

		assertThat(schemaTypeBuilder.getCustomSchemas()).isEmpty();
	}

	@Test
	public void givenCustomSchemaWithAMetadataWhenBuildingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode).create(metadataCode).setType(STRING);

		build();
		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenCustomSchemaWithAMetadataWhenModifyingThenDefaultSchemaBuilt()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode).create(metadataCode).setType(STRING);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedBeforeACustomSchemaWhenBuildingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);
		schemaTypeBuilder.createCustomSchema(customSchemaCode);

		build();

		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedBeforeACustomSchemaWhenModyfingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);
		schemaTypeBuilder.createCustomSchema(customSchemaCode);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedAfterACustomSchemaWhenBuildingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode);
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		build();

		validateSchemaTypeHasCustomSchemaWithMetadata();
	}

	@Test
	public void givenADefaultSchemaWithAMetadataCreatedAfterACustomSchemaWhenModyfingThenCustomSchemaInheritMetadata()
			throws Exception {
		schemaTypeBuilder.createCustomSchema(customSchemaCode);
		schemaTypeBuilder.getDefaultSchema().create(metadataCode).setType(STRING);

		buildAndModify();

		validateSchemaTypeBuilderHasCustomSchemaWithMetadata();
	}

	@Test(expected = MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema.class)
	public void givenInexistentCodeWhenGetCustomSchemaThenThrowExcpetion()
			throws Exception {

		buildAndModify();

		schemaTypeBuilder.getCustomSchema(INEXISTENT_CODE);
	}

	@Test
	public void whenCalculatingDependenciesOfASchemaWithTwoDifferentTypeReferencesThenBothAreReturned()
			throws Exception {
		MetadataSchemaBuilder schema = schemaTypeBuilder.getDefaultSchema();
		MetadataSchemaBuilder customSchema = schemaTypeBuilder.createCustomSchema("custom");

		MetadataBuilder defaultSchemaMetadata = schema.create("first").setType(STRING);
		MetadataBuilder customSchemaMetadata = customSchema.create("second").setType(STRING);
		MetadataBuilder customSchemaMetadataInheritingDefaultSchemaMetadata = customSchema.get("first");

		assertThat(schemaTypeBuilder.getAllMetadatas()).contains(defaultSchemaMetadata, customSchemaMetadata)
				.doesNotContain(customSchemaMetadataInheritingDefaultSchemaMetadata);
	}

	private void build() {
		schemaType = schemaTypeBuilder.build(typesFactory, taxonomiesManager);
	}

	private void buildAndModify() {
		MetadataSchemaType schemaType = schemaTypeBuilder.build(typesFactory, taxonomiesManager);
		schemaTypeBuilder = MetadataSchemaTypeBuilder.modifySchemaType(schemaType);
	}

	private void validateSchemaTypeHasCustomSchemaWithMetadata() {
		MetadataSchema customSchema = schemaType.getCustomSchema(customSchemaCode);
		Metadata metadata = customSchema.getMetadata(metadataCode);

		assertThat(metadata.getCode()).isEqualTo(expectedCustomSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}

	private void validateSchemaTypeBuilderHasCustomSchemaWithMetadata() {
		MetadataSchemaBuilder customSchemaBuilder = schemaTypeBuilder.getCustomSchema(customSchemaCode);
		MetadataBuilder metadata = customSchemaBuilder.getMetadata(metadataCode);

		assertThat(schemaTypeBuilder.getCustomSchemas()).hasSize(1);
		assertThat(metadata.getCode()).isEqualTo(expectedCustomSchemaMetadataCompleteCode);
		assertThat(metadata.getType()).isEqualTo(STRING);
	}
}
